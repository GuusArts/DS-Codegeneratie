package nl.kik.commons.gids.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.dto.Graph;
import nl.kik.commons.dto.RDFObject;
import nl.kik.commons.gids.dto.Address;
import nl.kik.commons.gids.dto.CareOffice;
import nl.kik.commons.gids.dto.Changeable;
import nl.kik.commons.gids.dto.Concessionaire;
import nl.kik.commons.gids.dto.DeliveryMethod;
import nl.kik.commons.gids.dto.GidsAttribute;
import nl.kik.commons.gids.dto.GidsObject;
import nl.kik.commons.gids.dto.GraphOrRemote;
import nl.kik.commons.gids.dto.HasAddress;
import nl.kik.commons.gids.dto.HasAgb;
import nl.kik.commons.gids.dto.HasKvk;
import nl.kik.commons.gids.dto.HasName;
import nl.kik.commons.gids.dto.Location;
import nl.kik.commons.gids.dto.Organisation;
import nl.kik.commons.gids.dto.Region;
import nl.kik.commons.gids.dto.Source;
import nl.kik.commons.service.AbstractRDFService;
import nl.kik.commons.service.RDFService;

@Service
@Slf4j
public class GidsService extends AbstractRDFService<GraphOrRemote> {
	private static final String GIDS = "gids";

	public static class Vocabulary {
		//// Classes
		public static Resource Organisation = Vocabulary.resource("Organisation");
		public static Resource Location = Vocabulary.resource("Location");
		public static Resource CareOffice = Vocabulary.resource("CareOffice");
		public static Resource Region = Vocabulary.resource("Region");
		public static Resource Concessionaire = Vocabulary.resource("Concessionaire");
		public static Resource Address = Vocabulary.resource("Address");

		//// Object properties
		public static Property address = Vocabulary.property("address");
		public static Property concessionaire = Vocabulary.property("concessionaire");
		public static Property deliveryMethod = Vocabulary.property("deliveryMethod");
		public static Property location = Vocabulary.property("location");
		public static Property office = Vocabulary.property("office");
		public static Property region = Vocabulary.property("region");

		//// Data properties
		public static Property agb = Vocabulary.property("agb");
		public static Property kvk = Vocabulary.property("kvk");
		public static Property careProviderName = Vocabulary.property("careProviderName");
		public static Property code = Vocabulary.property("code");
		public static Property houseLetter = Vocabulary.property("houseLetter");
		public static Property houseNumber = Vocabulary.property("houseNumber");
		public static Property lastModified = Vocabulary.property("lastModified");
		public static Property name = Vocabulary.property("name");
		public static Property number = Vocabulary.property("number");
		public static Property postalcode = Vocabulary.property("postalcode");
		public static Property province = Vocabulary.property("province");
		public static Property street = Vocabulary.property("street");
		public static Property town = Vocabulary.property("town");
		public static Property tradeName = Vocabulary.property("tradeName");
		public static Property source = Vocabulary.property("source");
		public static Property from = Vocabulary.property("from");
		public static Property to = Vocabulary.property("to");

		//// Enums
		public static Resource DeliveryMethod = Vocabulary.resource("DeliveryMethod");
		public static Resource ODB = Vocabulary.resource("ODB");
		public static Resource Datastation = Vocabulary.resource("Datastation");
		public static Resource KIKStarter = Vocabulary.resource("KIKStarter");

		public static Resource Source = Vocabulary.resource("Source");
		public static Resource LRZA = Vocabulary.resource("LRZA");
		public static Resource TABELBEHEER = Vocabulary.resource("TABELBEHEER");
		public static Resource KIK_STARTER = Vocabulary.resource("KIK_STARTER");

		public static final String uri = "https://kik-v.nl/ontology/starter/gids#";

		protected static final Property property(final String local) {
			return ResourceFactory.createProperty(Vocabulary.uri, local);
		}

		protected static final Resource resource(final String local) {
			return ResourceFactory.createResource(Vocabulary.uri + local);
		}
	}

	private static Map<Resource, Class<? extends RDFObject>> objectTypes = Map.ofEntries( //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Organisation, Organisation.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Location, Location.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.CareOffice, CareOffice.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Region, Region.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Concessionaire, Concessionaire.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Address, Address.class) //
	);

	private static Map<DeliveryMethod, Resource> deliveryMethods = Map.of(//
			DeliveryMethod.Datastation, Vocabulary.Datastation, //
			DeliveryMethod.KIKStarter, Vocabulary.KIKStarter, //
			DeliveryMethod.ODB, Vocabulary.ODB //
	);
	private static Map<Resource, DeliveryMethod> reverseDeliveryMethods = RDFService.reverse(deliveryMethods);

	private static Map<Source, Resource> sources = Map.of(//
			Source.LRZA, Vocabulary.LRZA, //
			Source.TABELBEHEER, Vocabulary.TABELBEHEER, //
			Source.KIK_STARTER, Vocabulary.KIK_STARTER //
	);
	private static Map<Resource, Source> reverseSources = RDFService.reverse(sources);

	@Override
	protected void saveDetails(Graph<? extends Model> g, Resource resource, RDFObject object) {
		if (object instanceof Organisation) {
			saveOrganisation(g, resource, (Organisation) object);
		} else if (object instanceof Location) {
			saveLocation(g, resource, (Location) object);
		} else if (object instanceof CareOffice) {
			saveCareOffice(g, resource, (CareOffice) object);
		} else if (object instanceof Region) {
			saveRegion(g, resource, (Region) object);
		} else if (object instanceof Concessionaire) {
			saveConcessionaire(g, resource, (Concessionaire) object);
		} else if (object instanceof Address) {
			saveAddress(g, resource, (Address) object);
		} else
			throw new IllegalArgumentException("Cannot save Gids objects of type " + object.getClass().getSimpleName());
	}

	protected void saveGidsObject(final Graph<? extends Model> g, final Resource resource, final GidsObject object) {
		saveRDFObject(g, resource, object);
	}

	protected void saveAddress(Graph<? extends Model> g, Resource resource, Address object) {
		g.beginWrite();
		try {
			saveGidsObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Address);
			addProperty(g, resource, Vocabulary.houseNumber, object.getHouseNumber());
			addProperty(g, resource, Vocabulary.houseLetter, object.getHouseLetter());
			addProperty(g, resource, Vocabulary.town, object.getTown());
			addProperty(g, resource, Vocabulary.province, object.getProvince());
			addProperty(g, resource, Vocabulary.postalcode, object.getPostalcode());
			addProperty(g, resource, Vocabulary.street, object.getStreet());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected Statement addProperty(Graph<? extends Model> g, Resource resource, Property property, Object value) {
		if (value instanceof GidsAttribute<?>) {
			GidsAttribute<?> attribute = (GidsAttribute<?>) value;
			if (attribute.getValues() != null) {
				attribute.getValues().entries().forEach(e -> {
					Statement s = super.addProperty(g, resource, property, e.getValue().getRight());
					if (s != null) {
						final Resource rs = g.getModel().getAnyReifiedStatement(s);
						g.getModel().add(rs, Vocabulary.source, sources.get(e.getKey()));
						super.addProperty(g, rs, Vocabulary.from, e.getValue().getLeft());
						super.addProperty(g, rs, Vocabulary.to, e.getValue().getMiddle());
					}
				});
			}
			return null;
		} else {
			return super.addProperty(g, resource, property, value);
		}
	}

	protected <T> void addEnum(Graph<? extends Model> g, Resource resource, Property property,
			GidsAttribute<T> attribute, Map<T, Resource> map) {
		if (attribute != null && attribute.getValues() != null) {
			attribute.getValues().entries().forEach(e -> {
				Resource r = map.get(e.getValue().getRight());
				if (r != null) {
					Statement s = g.getModel().createStatement(resource, property, r);
					g.getModel().add(s);
					final Resource rs = g.getModel().getAnyReifiedStatement(s);
					g.getModel().add(rs, Vocabulary.source, sources.get(e.getKey()));
					super.addProperty(g, rs, Vocabulary.from, e.getValue().getLeft());
					super.addProperty(g, rs, Vocabulary.to, e.getValue().getMiddle());
				}
			});
		}
	}

	protected void addObject(Graph<? extends Model> g, Resource resource, Property property,
			GidsAttribute<? extends RDFObject> attribute, boolean deep) {
		if (attribute != null) {
			if (attribute.getValues() != null) {
				attribute.getValues().entries().forEach(e -> {
					if (!deep && e.getValue().getRight().getId() == null) {
						throw new RuntimeException(
								"Trying to save reference to unsaved object " + e.getValue().getRight());
					}
					Statement s = super.addObject(g, resource, property, e.getValue().getRight());
					if (s != null) {
						final Resource rs = g.getModel().getAnyReifiedStatement(s);
						g.getModel().add(rs, Vocabulary.source, sources.get(e.getKey()));
						super.addProperty(g, rs, Vocabulary.from, e.getValue().getLeft());
						super.addProperty(g, rs, Vocabulary.to, e.getValue().getMiddle());
					}
				});
			}
		}
	}

	protected void addObjects(Graph<? extends Model> g, Resource resource, Property property,
			Collection<? extends GidsAttribute<? extends RDFObject>> list, boolean deep) {
		for (GidsAttribute<? extends RDFObject> object : CollectionUtils.emptyIfNull(list)) {
			addObject(g, resource, property, object, deep);
		}
	}

	protected void saveConcessionaire(Graph<? extends Model> g, Resource resource, Concessionaire object) {
		g.beginWrite();
		try {
			saveGidsObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Concessionaire);
			addName(g, resource, object);
			g.commit();
		} finally {
			g.end();
		}
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addName(Graph<? extends Model> g, Resource resource, HasName object) {
		addProperty(g, resource, Vocabulary.name, object.getName());
	}

	protected void saveRegion(Graph<? extends Model> g, Resource resource, Region object) {
		g.beginWrite();
		try {
			saveGidsObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Region);
			addProperty(g, resource, Vocabulary.code, object.getCode());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveCareOffice(Graph<? extends Model> g, Resource resource, CareOffice object) {
		g.beginWrite();
		try {
			saveGidsObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.CareOffice);
			addProperty(g, resource, Vocabulary.code, object.getCode());
			addObject(g, resource, Vocabulary.region, object.getRegion(), false);
			addObject(g, resource, Vocabulary.concessionaire, object.getConcessionaire(), false);
			addName(g, resource, object);
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveLocation(Graph<? extends Model> g, Resource resource, Location object) {
		g.beginWrite();
		try {
			saveGidsObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Location);
			addName(g, resource, object);
			addProperty(g, resource, Vocabulary.number, object.getNumber());
			addAgb(g, resource, object);
			addAddress(g, resource, object, true);
			g.commit();
		} finally {
			g.end();
		}
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addAgb(Graph<? extends Model> g, Resource resource, HasAgb object) {
		addProperty(g, resource, Vocabulary.agb, object.getAgb());
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addKvk(Graph<? extends Model> g, Resource resource, HasKvk object) {
		addProperty(g, resource, Vocabulary.kvk, object.getKvk());
	}

	protected void saveOrganisation(Graph<? extends Model> g, Resource resource, Organisation object) {
		g.beginWrite();
		try {
			saveGidsObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Organisation);
			addAddress(g, resource, object, true);
			addObject(g, resource, Vocabulary.office, object.getOffice(), false);
			addName(g, resource, object);
			addProperty(g, resource, Vocabulary.tradeName, object.getTradeName());
			addProperty(g, resource, Vocabulary.careProviderName, object.getCareProviderName());
			addLastModified(g, resource, object);
			addAgb(g, resource, object);
			addKvk(g, resource, object);
			addObjects(g, resource, Vocabulary.location, object.getLocation(), true);
			addEnum(g, resource, Vocabulary.deliveryMethod, object.getDeliveryMethod(), deliveryMethods);
			g.commit();
		} finally {
			g.end();
		}
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addLastModified(Graph<? extends Model> g, Resource resource, Changeable object) {
		addProperty(g, resource, Vocabulary.lastModified, object.getLastModified());
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addAddress(Graph<? extends Model> g, Resource resource, HasAddress object, boolean deep) {
		addObject(g, resource, Vocabulary.address, object.getAddress(), deep);
	}

	@Override
	protected @NotNull String getPrefix() {
		return GIDS;
	}

	protected void delete(Graph<? extends Model> g, RDFObject object, boolean purge) {
		g.beginWrite();
		try {
			Resource resource = g.getModel().createResource(object.getId());
			g.getModel().listStatements(resource, null, (RDFNode) null).forEach(s -> {
				g.getModel().listReifiedStatements(s).forEach(r -> {
					g.getModel().removeAll(r, null, null);
				});
				g.getModel().removeAllReifications(s);
			});
			deleteDetails(g, resource, object, purge);
			if (purge) {
				g.getModel().removeAll(null, null, resource);
				g.getModel().listStatements(null, null, resource).forEach(s -> {
					g.getModel().listReifiedStatements(s).forEach(r -> {
						g.getModel().removeAll(r, null, null);
					});
					g.getModel().removeAllReifications(s);
				});
			}
			g.commit();
		} finally {
			g.end();
		}
	}

	@Override
	protected void deleteDetails(Graph<? extends Model> g, Resource resource, RDFObject object, boolean purge) {
		if (object instanceof Organisation) {
			Organisation o = (Organisation) object;
			delete(g, o.getAddress(), purge);
			CollectionUtils.emptyIfNull(o.getLocation()).forEach(l -> delete(g, l, purge));
		}
	}

	protected void delete(Graph<? extends Model> g, GidsAttribute<? extends RDFObject> child, boolean purge) {
		if (child != null && child.getValues() != null) {
			child.getValues().entries().forEach(e -> {
				delete(g, e.getValue().getRight(), purge);
			});
		}
	}

	@Override
	protected Map<Resource, Class<? extends RDFObject>> getObjectTypes() {
		return objectTypes;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <U extends RDFObject> U getObject(GraphOrRemote graph, MultiValuedMap<Property, RDFNode> properties,
			Resource resource, Class<U> t) {
		if (GidsObject.class.isAssignableFrom(t))
			return (U) getGidsObject(graph, properties, resource, (Class<GidsObject>) t);
		throw new IllegalArgumentException("Cannot load RDF objects of type " + t.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	private <U extends GidsObject> U getGidsObject(GraphOrRemote graph, MultiValuedMap<Property, RDFNode> properties,
			Resource resource, Class<GidsObject> t) {
		MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> sources = getSources(
				graph, resource);
		if (Address.class.isAssignableFrom(t))
			return (U) getAddress(graph, properties, sources, resource, Address.builder()).build();
		if (CareOffice.class.isAssignableFrom(t))
			return (U) getCareOffice(graph, properties, sources, resource, CareOffice.builder()).build();
		if (Concessionaire.class.isAssignableFrom(t))
			return (U) getConcessionaire(graph, properties, sources, resource, Concessionaire.builder()).build();
		if (Location.class.isAssignableFrom(t))
			return (U) getLocation(graph, properties, sources, resource, Location.builder()).build();
		if (Organisation.class.isAssignableFrom(t))
			return (U) getOrganisation(graph, properties, sources, resource, Organisation.builder()).build();
		if (Region.class.isAssignableFrom(t))
			return (U) getRegion(graph, properties, sources, resource, Region.builder()).build();
		throw new IllegalArgumentException("Cannot load Gids objects of type " + t.getSimpleName());
	}

	private MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> getSources(
			GraphOrRemote graph, Resource resource) {
		Query q = new SelectBuilder() //
				.setDistinct(true) //
				.addVar("?so") //
				.addVar("?f") //
				.addVar("?t") //
				.addVar("?p") //
				.addVar("?o") //
				.addWhere("?st", RDF.type, RDF.Statement) //
				.addWhere("?st", RDF.subject, resource) //
				.addWhere("?st", RDF.predicate, "?p") //
				.addWhere("?st", RDF.object, "?o") //
				.addWhere("?st", Vocabulary.source, "?so") //
				.addOptional("?st", Vocabulary.from, "?f") //
				.addOptional("?st", Vocabulary.to, "?t") //
				.build();
		return search(graph, q) //
				.collect(HashSetValuedHashMap::new, (b,
						s) -> b.put(Pair.of(new PropertyImpl(s.get("?p").asResource().getURI()), s.get("?o")), Triple
								.of(getDateTime(s.get("?f")), getDateTime(s.get("?t")), s.get("?so").asResource())),
						HashSetValuedHashMap::putAll); //
	}

	private <B extends GidsObject.GidsObjectBuilder<?, ?>> B getGidsObject(GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return getRDFObject(graph, properties, resource, builder) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends Address.AddressBuilder<?, ?>> B getAddress(GraphOrRemote graph,
			MultiValuedMap<Property, RDFNode> properties,
			MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> sources,
			Resource resource, B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.houseNumber(getAlternatives(graph, resource, properties, sources, Vocabulary.houseNumber,
						RDFService::getString)) //
				.houseLetter(getAlternatives(graph, resource, properties, sources, Vocabulary.houseLetter,
						RDFService::getString)) //
				.town(getAlternatives(graph, resource, properties, sources, Vocabulary.town, RDFService::getString)) //
				.province(getAlternatives(graph, resource, properties, sources, Vocabulary.province,
						RDFService::getString)) //
				.postalcode(getAlternatives(graph, resource, properties, sources, Vocabulary.postalcode,
						RDFService::getString)) //
				.street(getAlternatives(graph, resource, properties, sources, Vocabulary.street, RDFService::getString)) //
		;
	}

	private <T> GidsAttribute<T> getAlternatives(GraphOrRemote graph, Resource resource,
			MultiValuedMap<Property, RDFNode> properties,
			MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> sources, Property p,
			Function<RDFNode, T> mapper) {
		Collection<RDFNode> all = properties.get(p);
		var builder = GidsAttribute.<T>builder();
		for (RDFNode n : all) {
			try {
				T v = mapper.apply(n);
				sources.get(Pair.of(p, n)).stream() //
						.map(t -> Triple.of(t.getLeft(), t.getMiddle(), reverseSources.get(t.getRight()))) //
						.filter(t -> t.getRight() != null) //
						.forEach(s -> builder.alternative(s.getRight(), s.getLeft(), s.getMiddle(), v));
			} catch (final Exception e) {
			}
		}
		return builder.build();
	}

	private <T> List<GidsAttribute<T>> getAlternativesList(GraphOrRemote graph, Resource resource,
			MultiValuedMap<Property, RDFNode> properties,
			MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> sources, Property p,
			Function<RDFNode, T> mapper) {
		Collection<RDFNode> all = properties.get(p);
		List<GidsAttribute<T>> result = new ArrayList<>();
		for (RDFNode n : all) {
			try {
				T v = mapper.apply(n);
				sources.get(Pair.of(p, n)).stream() //
						.map(t -> Triple.of(t.getLeft(), t.getMiddle(), reverseSources.get(t.getRight()))) //
						.filter(t -> t.getRight() != null) //
						.map(s -> GidsAttribute.<T>builder() //
								.alternative(s.getRight(), s.getLeft(), s.getMiddle(), v) //
								.build()) //
						.forEach(result::add);
			} catch (final Exception e) {
			}
		}
		return result.isEmpty() ? null : result;
	}

	@SuppressWarnings("unchecked")
	private <B extends CareOffice.CareOfficeBuilder<?, ?>> B getCareOffice(GraphOrRemote graph,
			MultiValuedMap<Property, RDFNode> properties,
			MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> sources,
			Resource resource, B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.code(getAlternatives(graph, resource, properties, sources, Vocabulary.code, RDFService::getString)) //
				.name(getAlternatives(graph, resource, properties, sources, Vocabulary.name, RDFService::getString)) //
				.region(getAlternatives(graph, resource, properties, sources, Vocabulary.region,
						n -> getObject(graph, n, Region.class))) //
				.concessionaire(getAlternatives(graph, resource, properties, sources, Vocabulary.concessionaire,
						n -> getObject(graph, n, Concessionaire.class))) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends Concessionaire.ConcessionaireBuilder<?, ?>> B getConcessionaire(GraphOrRemote graph,
			MultiValuedMap<Property, RDFNode> properties,
			MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> sources,
			Resource resource, B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.name(getAlternatives(graph, resource, properties, sources, Vocabulary.name, RDFService::getString)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends Location.LocationBuilder<?, ?>> B getLocation(GraphOrRemote graph,
			MultiValuedMap<Property, RDFNode> properties,
			MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> sources,
			Resource resource, B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.name(getAlternatives(graph, resource, properties, sources, Vocabulary.name, RDFService::getString)) //
				.number(getAlternatives(graph, resource, properties, sources, Vocabulary.number, RDFService::getString)) //
				.agb(getAlternatives(graph, resource, properties, sources, Vocabulary.agb, RDFService::getString)) //
				.address(getAlternatives(graph, resource, properties, sources, Vocabulary.address,
						n -> getObject(graph, n, Address.class))) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends Organisation.OrganisationBuilder<?, ?>> B getOrganisation(GraphOrRemote graph,
			MultiValuedMap<Property, RDFNode> properties,
			MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> sources,
			Resource resource, B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.address(getAlternatives(graph, resource, properties, sources, Vocabulary.address,
						n -> getObject(graph, n, Address.class))) //
				.office(getAlternatives(graph, resource, properties, sources, Vocabulary.office,
						n -> getObject(graph, n, CareOffice.class))) //
				.name(getAlternatives(graph, resource, properties, sources, Vocabulary.name, RDFService::getString)) //
				.tradeName(getAlternatives(graph, resource, properties, sources, Vocabulary.tradeName,
						RDFService::getString)) //
				.careProviderName(getAlternatives(graph, resource, properties, sources, Vocabulary.careProviderName,
						RDFService::getString)) //
				.lastModified(getAlternatives(graph, resource, properties, sources, Vocabulary.lastModified,
						RDFService::getDateTime)) //
				.agb(getAlternatives(graph, resource, properties, sources, Vocabulary.agb, RDFService::getString)) //
				.kvk(getAlternatives(graph, resource, properties, sources, Vocabulary.kvk, RDFService::getString)) //
				.location(getAlternativesList(graph, resource, properties, sources, Vocabulary.location,
						n -> getObject(graph, n, Location.class))) //
				.deliveryMethod(getAlternatives(graph, resource, properties, sources, Vocabulary.deliveryMethod,
						n -> n.isResource() ? getEnum(Collections.singletonList(n.asResource()), reverseDeliveryMethods)
								: null)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends Region.RegionBuilder<?, ?>> B getRegion(GraphOrRemote graph,
			MultiValuedMap<Property, RDFNode> properties,
			MultiValuedMap<Pair<Property, RDFNode>, Triple<ZonedDateTime, ZonedDateTime, Resource>> sources,
			Resource resource, B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.code(getAlternatives(graph, resource, properties, sources, Vocabulary.code, RDFService::getString)) //
		;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected MultiValuedMap<Property, RDFNode> getProperties(GraphOrRemote g, final Resource r) {
		if (g.isGraph()) {
			return RDFService.getProperties(g.getGraph(), r);
		}
		if (g.isRemote()) {
			return (MultiValuedMap) search(g, r, null, null, s -> s) //
					.collect(HashSetValuedHashMap::new, (b, s) -> b.put(s.getPredicate(), s.getObject()),
							HashSetValuedHashMap::putAll); //

		}
		throw new IllegalArgumentException(); // Should never be reachable
	}

	public <U extends RDFObject> Optional<U> lookupById(Graph<? extends Model> graph, @NotNull String id) {
		return lookupById(new GraphOrRemote(graph), id);
	}

	public <U extends RDFObject> Optional<U> lookupById(String remote, @NotNull String id) {
		return lookupById(new GraphOrRemote(remote), id);
	}

	@SuppressWarnings("unchecked")
	public <U extends GidsObject> List<U> query(GraphOrRemote graph, Query q, Class<U> clazz) {
		if (q == null || !q.isSelectType()) {
			throw new IllegalArgumentException("Exactly one SELECT query must be given");
		}
		if (q.getProjectVars().size() != 1) {
			throw new IllegalArgumentException("Query must project onto exactly one value (the rtequested resource)");
		}
		String variableName = "?" + q.getProjectVars().iterator().next().getVarName();
		log.trace("Exceuting with variable {} query {}", variableName, q);
		AtomicInteger loaded = new AtomicInteger();
		return (List<U>) search(graph, q) //
				.map(s -> {
					try {
						Resource v = s.getResource(variableName);
						log.trace(" - {} -> {}", s, v);
						return v;
					} catch (final Exception ex) {
						log.trace(" - Failed extracting from {}", s);
						return null;
					}
				}) //
				.filter(Objects::nonNull) //
				.map(r -> getObject(graph, r, GidsObject.class)) //
				.filter(Objects::nonNull) //
				.filter(o -> clazz == null || clazz.isInstance(o)) //
				.peek(o -> {
					if (loaded.incrementAndGet() % 1000 == 0) {
						log.trace("Loaded {}", loaded);
					}
				}) //
				.collect(Collectors.toList());
	}

	public <U extends GidsObject> List<U> query(Graph<? extends Model> graph, Query q, Class<U> clazz) {
		return query(new GraphOrRemote(graph), q, clazz);
	}

	public <U extends GidsObject> List<U> query(String remote, Query q, Class<U> clazz) {
		return query(remote, null, q, clazz);
	}

	public <U extends GidsObject> List<U> query(String remote, String auth, Query q, Class<U> clazz) {
		return query(new GraphOrRemote(remote, auth), q, clazz);
	}

	/**
	 * @param r
	 * @return
	 */
	public static <U> Stream<U> search(GraphOrRemote graph, final Resource r, final Property p, final Resource o,
			final Function<Statement, U> extract) {
		log.trace("Search for ({}, {}, {})", r, p, o);
		if (graph.isGraph()) {
			return search(graph.getGraph(), r, p, o, extract);
		}
		if (graph.isRemote()) {
			Query q = getSelectQuery(r, p, o);
			return search(graph, q) //
					.map(s -> {
						try {
							final U v = extract.apply(new StatementImpl(s.get("?s").asResource(),
									new PropertyImpl(s.get("?p").asResource().getURI()), s.get("?o")));
							log.trace(" - {} -> {}", s, v);
							return v;
						} catch (final Exception ex) {
							log.trace(" - Failed extracting from {}", s);
							return null;
						}
					}) //
					.filter(Objects::nonNull) //
			;
		}
		throw new IllegalArgumentException(); // Should never be reachable
	}

	protected static Stream<QuerySolution> search(GraphOrRemote graph, Query q) {
		try (QueryExecution qe = getQueryExecution(graph, q)) {
			ResultSet rs = qe.execSelect();
			return StreamSupport
					.stream(Spliterators.spliteratorUnknownSize(rs,
							Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE), false) //
					.filter(Objects::nonNull) //
					.collect(Collectors.toList()) //
					.stream();
		}
	}

	/**
	 * @param graph
	 * @param q
	 * @return
	 */
	protected static QueryExecution getQueryExecution(GraphOrRemote graph, Query q) {
		if (graph.isGraph()) {
			return QueryExecutionFactory.create(q, graph.getGraph().getModel());
		}
		if (graph.isRemote()) {
			return QueryExecutionFactory.sparqlService(getQueryURL(graph.getRemote()), q,
					getHttpClient(graph.getAuth()));
		}
		throw new IllegalArgumentException(); // Cannot happen
	}

	/**
	 * @param r
	 * @param p
	 * @param o
	 * @return
	 */
	protected static Query getSelectQuery(final Resource r, final Property p, final Resource o) {
		var builder = new SelectBuilder();
		var e = builder.getExprFactory();
		builder = builder //
				.addPrefix("", Vocabulary.uri) //
				.addVar("?s") //
				.addVar("?p") //
				.addVar("?o") //
				.setDistinct(true) //
		;
		if (r != null) {
			builder = builder //
					.addBind(e.asExpr(r), "?s");
		}
		if (p != null) {
			builder = builder //
					.addBind(e.asExpr(p), "?p");
		}
		if (o != null) {
			builder = builder //
					.addBind(e.asExpr(o), "?o");
		}
		Query q = builder //
				.addWhere("?s", "?p", "?o") //
				.build();
		log.trace("Generated query {}", q);
		return q;
	}

	protected static RDFConnection getConnection(final String url, String auth) {
		return RDFConnectionFuseki.create() //
				.httpClient(getHttpClient(auth)) //
				.destination(url) //
				.build();
	}

	/**
	 * @param url
	 * @return
	 */
	protected static String getQueryURL(final String url) {
		if (url == null || !(url.endsWith("/gids") || url.endsWith("/gids/"))) {
			throw new IllegalArgumentException("Please provice an endpoint to the base service");
		}
		return url.replaceFirst("/*$", "") + "/sparql";
	}

	/**
	 * @param url
	 * @return
	 */
	protected static String getUploadURL(final String url) {
		if (url == null || !(url.endsWith("/gids") || url.endsWith("/gids/"))) {
			throw new IllegalArgumentException("Please provice an endpoint to the base service");
		}
		return url.replaceFirst("/gids/*$", "/upload/gids/data");
	}

	/**
	 * @param auth
	 * @return
	 */
	protected static HttpClient getHttpClient(String auth) {
		HttpClient httpclient = null;
		if (auth != null) {
			httpclient = HttpClients.custom() //
					.setDefaultHeaders(Collections.singleton(new BasicHeader("Authorization", "Bearer " + auth))) //
					.build();
		}
		return httpclient;
	}

	public void save(String remote, String auth, RDFObject object) {
		Model model = ModelFactory.createDefaultModel();
		Graph<Model> graph = Graph.create(model);
		super.save(graph, object);
		try (RDFConnection connection = getConnection(getUploadURL(remote), auth)) {
			connection.begin(TxnType.WRITE);
			try {
				connection.load(model);
				connection.commit();
			} catch (Throwable t) {
				connection.abort();
				throw t;
			} finally {
				connection.end();
			}
		}
	}

}
