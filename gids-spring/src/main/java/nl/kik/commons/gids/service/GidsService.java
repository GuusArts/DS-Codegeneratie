package nl.kik.commons.gids.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collector;
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
import org.apache.jena.arq.querybuilder.UpdateBuilder;
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
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateAction;
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
import nl.kik.commons.gids.dto.HasNames;
import nl.kik.commons.gids.dto.HasSbi;
import nl.kik.commons.gids.dto.Location;
import nl.kik.commons.gids.dto.Organisation;
import nl.kik.commons.gids.dto.Region;
import nl.kik.commons.gids.dto.Source;
import nl.kik.commons.service.AbstractRDFService;
import nl.kik.commons.service.RDFService;

@Service
@Slf4j
public class GidsService extends AbstractRDFService<GraphOrRemote> {
	public static class Vocabulary {
		//// Classes
		public static final Resource Organisation = Vocabulary.resource("Organisation");
		public static final Resource Location = Vocabulary.resource("Location");
		public static final Resource CareOffice = Vocabulary.resource("CareOffice");
		public static final Resource Region = Vocabulary.resource("Region");
		public static final Resource Concessionaire = Vocabulary.resource("Concessionaire");
		public static final Resource Address = Vocabulary.resource("Address");

		public static final Resource Root = Vocabulary.resource("Root");

		//// Object properties
		public static final Property address = Vocabulary.property("address");
		public static final Property concessionaire = Vocabulary.property("concessionaire");
		public static final Property deliveryMethod = Vocabulary.property("deliveryMethod");
		public static final Property location = Vocabulary.property("location");
		public static final Property office = Vocabulary.property("office");
		public static final Property region = Vocabulary.property("region");

		//// Data properties
		public static final Property agb = Vocabulary.property("agb");
		public static final Property sbi = Vocabulary.property("sbi");
		public static final Property kvk = Vocabulary.property("kvk");
		public static final Property code = Vocabulary.property("code");
		public static final Property houseLetter = Vocabulary.property("houseLetter");
		public static final Property houseNumber = Vocabulary.property("houseNumber");
		public static final Property lastModified = Vocabulary.property("lastModified");
		public static final Property name = Vocabulary.property("name");
		public static final Property primaryName = Vocabulary.property("primaryName");
		public static final Property number = Vocabulary.property("number");
		public static final Property postalcode = Vocabulary.property("postalcode");
		public static final Property province = Vocabulary.property("province");
		public static final Property street = Vocabulary.property("street");
		public static final Property town = Vocabulary.property("town");

		public static final Property root = Vocabulary.property("root");
		public static final Property source = Vocabulary.property("source");
		public static final Property from = Vocabulary.property("from");
		public static final Property to = Vocabulary.property("to");

		//// Enums
		public static final Resource DeliveryMethod = Vocabulary.resource("DeliveryMethod");
		public static final Resource ODB = Vocabulary.resource("ODB");
		public static final Resource Datastation = Vocabulary.resource("Datastation");
		public static final Resource KIKStarter = Vocabulary.resource("KIKStarter");

		public static final Resource Source = Vocabulary.resource("Source");
		public static final Resource LRZA = Vocabulary.resource("LRZA");
		public static final Resource TABELBEHEER = Vocabulary.resource("TABELBEHEER");
		public static final Resource KIK_STARTER = Vocabulary.resource("KIK_STARTER");
		public static final Resource NONE = Vocabulary.resource("NONE");

		public static final String uri = "https://kik-v.nl/ontology/starter/gids#";

		protected static final Property property(final String local) {
			return ResourceFactory.createProperty(Vocabulary.uri, local);
		}

		protected static final Resource resource(final String local) {
			return ResourceFactory.createResource(Vocabulary.uri + local);
		}
	}

	private static final String GIDS = "gids";

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

	private static Map<Resource, DeliveryMethod> reverseDeliveryMethods = RDFService
			.reverse(GidsService.deliveryMethods);
	private static Map<Source, Resource> sources = Map.of(//
			Source.LRZA, Vocabulary.LRZA, //
			Source.TABELBEHEER, Vocabulary.TABELBEHEER, //
			Source.KIK_STARTER, Vocabulary.KIK_STARTER, //
			Source.NONE, Vocabulary.NONE//
	);

	private static Map<Resource, Source> reverseSources = RDFService.reverse(GidsService.sources);

	protected static RDFConnection getConnection(final String url, final String auth) {
		return RDFConnectionFuseki.create() //
				.httpClient(GidsService.getHttpClient(auth)) //
				.destination(url) //
				.build();
	}

	/**
	 * @param auth
	 * @return
	 */
	protected static HttpClient getHttpClient(final String auth) {
		HttpClient httpclient = null;
		if (auth != null) {
			httpclient = HttpClients.custom() //
					.setDefaultHeaders(Collections.singleton(new BasicHeader("Authorization", "Bearer " + auth))) //
					.build();
		}
		return httpclient;
	}

	/**
	 * @param graph
	 * @param q
	 * @return
	 */
	protected static QueryExecution getQueryExecution(final GraphOrRemote graph, final Query q) {
		if (graph.isCache())
			throw new IllegalArgumentException("Cannot query cache");
		if (graph.isGraph())
			return QueryExecutionFactory.create(q, graph.getGraph().getModel());
		if (graph.isRemote())
			return QueryExecutionFactory.sparqlService(GidsService.getQueryURL(graph.getRemote()), q,
					GidsService.getHttpClient(graph.getAuth()));
		throw new IllegalArgumentException(); // Cannot happen
	}

	/**
	 * @param url
	 * @return
	 */
	protected static String getQueryURL(final String url) {
		if (url == null || !(url.endsWith("/gids") || url.endsWith("/gids/")))
			throw new IllegalArgumentException("Please provice an endpoint to the base service");
		return url.replaceFirst("/*$", "") + "/sparql";
	}

	/**
	 * @param r
	 * @param p
	 * @param o
	 * @return
	 */
	protected static Query getSelectQuery(final Resource r, final Property p, final Resource o) {
		var builder = new SelectBuilder();
		final var e = builder.getExprFactory();
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
		final Query q = builder //
				.addWhere("?s", "?p", "?o") //
				.build();
		GidsService.log.trace("Generated query {}", q);
		return q;
	}

	/**
	 * @param url
	 * @return
	 */
	protected static String getUploadURL(final String url) {
		if (url == null || !(url.endsWith("/gids") || url.endsWith("/gids/")))
			throw new IllegalArgumentException("Please provice an endpoint to the base service");
		return url.replaceFirst("/gids/*$", "/upload/gids/data");
	}

	protected static Stream<QuerySolution> search(final GraphOrRemote graph, final Query q) {
		try (QueryExecution qe = GidsService.getQueryExecution(graph, q)) {
			final ResultSet rs = qe.execSelect();
			return StreamSupport
					.stream(Spliterators.spliteratorUnknownSize(rs,
							Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE), false) //
					.filter(Objects::nonNull) //
					.collect(Collectors.toList()) //
					.stream();
		}
	}

	/**
	 * @param r
	 * @return
	 */
	public static <U> Stream<U> search(final GraphOrRemote graph, final Resource r, final Property p, final Resource o,
			final Function<Statement, U> extract) {
		if (graph.isCache())
			throw new IllegalArgumentException("Cannot search cache");
		GidsService.log.trace("Search for ({}, {}, {})", r, p, o);
		if (graph.isGraph())
			return RDFService.search(graph.getGraph(), r, p, o, extract);
		if (graph.isRemote()) {
			final Query q = GidsService.getSelectQuery(r, p, o);
			return GidsService.search(graph, q) //
					.map(s -> {
						try {
							final U v = extract.apply(new StatementImpl(s.get("?s").asResource(),
									new PropertyImpl(s.get("?p").asResource().getURI()), s.get("?o")));
							GidsService.log.trace(" - {} -> {}", s, v);
							return v;
						} catch (final Exception ex) {
							GidsService.log.trace(" - Failed extracting from {}", s);
							return null;
						}
					}) //
					.filter(Objects::nonNull) //
			;
		}
		throw new IllegalArgumentException(); // Should never be reachable
	}

	private static String unique(final String value, final String... blocks) {
		if (blocks == null)
			return value;
		for (final String block : blocks) {
			if (Objects.equals(block, value))
				return value + "1";
		}
		return value;
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addAddress(final Graph<? extends Model> g, final Resource resource, final HasAddress object,
			final boolean deep) {
		addObject(g, resource, Vocabulary.address, object.getAddress(), deep);
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addAgb(final Graph<? extends Model> g, final Resource resource, final HasAgb object) {
		addAllProperties(g, resource, Vocabulary.agb, object.getAgb());
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addSbi(final Graph<? extends Model> g, final Resource resource, final HasSbi object) {
		addAllProperties(g, resource, Vocabulary.sbi, object.getSbi());
	}

	private <U extends GidsObject> GidsAttribute<U> addAlternatives(final GraphOrRemote graph,
			final Collection<Triple<LocalDate, LocalDate, Resource>> sources, final U object) {
		final var builder = GidsAttribute.<U>builder();
		sources.stream().map(t -> Triple.of(t.getLeft(), t.getMiddle(), GidsService.reverseSources.get(t.getRight()))) //
				.filter(t -> t.getRight() != null) //
				.forEach(s -> builder.alternative(s.getRight(), s.getLeft(), s.getMiddle(), object));
		return builder.build();
	}

	private <U extends GidsObject> GidsAttribute<U> addAlternatives(final GraphOrRemote graph, final U object) {
		return addAlternatives(graph, getRootSources(graph, graph.getResource(object.getId())), object);
	}

	protected <T> void addEnum(final Graph<? extends Model> g, final Resource resource, final Property property,
			final GidsAttribute<T> attribute, final Map<T, Resource> map) {
		if (attribute != null && attribute.getValues() != null) {
			attribute.getValues().entries().forEach(e -> {
				final Resource r = map.get(e.getValue().getRight());
				if (r != null) {
					final Statement s = g.getModel().createStatement(resource, property, r);
					g.getModel().add(s);
					final Resource rs = g.getModel().createReifiedStatement(s);
					g.getModel().add(rs, Vocabulary.source, GidsService.sources.get(e.getKey()));
					super.addProperty(g, rs, Vocabulary.from, e.getValue().getLeft());
					super.addProperty(g, rs, Vocabulary.to, e.getValue().getMiddle());
				}
			});
		}
	}

	@Override
	protected Statement addProperty(final Graph<? extends Model> g, final Resource resource, final Property property,
			final Object value) {
		if (value instanceof GidsAttribute<?>) {
			GidsAttribute<?> attribute = (GidsAttribute<?>) value;
			if (attribute.getValues() != null) {
				attribute.getValues().entries().forEach(e -> {
					final Statement s = super.addProperty(g, resource, property, e.getValue().getRight());
					if (s != null) {
						final Resource rs = g.getModel().createReifiedStatement(s);
						g.getModel().add(rs, Vocabulary.source, GidsService.sources.get(e.getKey()));
						super.addProperty(g, rs, Vocabulary.from, e.getValue().getLeft());
						super.addProperty(g, rs, Vocabulary.to, e.getValue().getMiddle());
					}
				});
			}
			return null;
		}
		return super.addProperty(g, resource, property, value);
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addKvk(final Graph<? extends Model> g, final Resource resource, final HasKvk object) {
		addProperty(g, resource, Vocabulary.kvk, object.getKvk());
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addLastModified(final Graph<? extends Model> g, final Resource resource, final Changeable object) {
		addProperty(g, resource, Vocabulary.lastModified, object.getLastModified());
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addName(final Graph<? extends Model> g, final Resource resource, final HasName object) {
		addProperty(g, resource, Vocabulary.name, object.getName());
	}

	protected void addName(final Graph<? extends Model> g, final Resource resource, final HasNames object) {
		addProperty(g, resource, Vocabulary.primaryName, object.getPrimaryName());
		addAllProperties(g, resource, Vocabulary.name, object.getName());
	}

	protected void addObject(final Graph<? extends Model> g, final Resource resource, final Property property,
			final GidsAttribute<? extends RDFObject> attribute, final boolean deep) {
		if (attribute != null) {
			if (attribute.getValues() != null) {
				attribute.getValues().entries().forEach(e -> {
					if (!deep && e.getValue().getRight().getId() == null)
						throw new RuntimeException(
								"Trying to save reference to unsaved object " + e.getValue().getRight());
					final Statement s = super.addObject(g, resource, property, e.getValue().getRight());
					if (s != null) {
						final Resource rs = g.getModel().createReifiedStatement(s);
						g.getModel().add(rs, Vocabulary.source, GidsService.sources.get(e.getKey()));
						super.addProperty(g, rs, Vocabulary.from, e.getValue().getLeft());
						super.addProperty(g, rs, Vocabulary.to, e.getValue().getMiddle());
					}
				});
			}
		}
	}

	protected void addObjects(final Graph<? extends Model> g, final Resource resource, final Property property,
			final Collection<? extends GidsAttribute<? extends RDFObject>> list, final boolean deep) {
		for (final GidsAttribute<? extends RDFObject> object : CollectionUtils.emptyIfNull(list)) {
			addObject(g, resource, property, object, deep);
		}
	}

	/**
	 * @param r
	 * @param p
	 * @param o
	 * @param resourceList
	 * @param variableName
	 * @param q
	 * @return
	 */
	protected SelectBuilder createCache(final String r, final String p, final String o,
			final SelectBuilder resourceList) {
		final SelectBuilder cb = new SelectBuilder() //
				.addVar(r).addVar(p).addVar(o) //
				.addSubQuery(resourceList) //
				.addWhere(r, p, o) //
		;
		return cb;
	}

	/**
	 * @param <U>
	 * @param q
	 * @param clazz
	 * @param variableName
	 * @param r
	 * @return
	 */
	protected <U extends GidsObject> SelectBuilder createCompleteResourceList(final Query q, final Class<U> clazz,
			final String variableName, final String r) {
		final SelectBuilder ob = new SelectBuilder() // A
				.addVar(new ExprVar(variableName.substring(1)), r) //
		;
		final Query qq = q.cloneQuery();
		qq.getPrefixMapping().clearNsPrefixMap();
		q.getPrefixMapping().getNsPrefixMap().entrySet().forEach(e -> {
			ob.addPrefix(e.getKey(), e.getValue()); // This is safe because we never add our own prefixes
		});
		ob.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));

		if (clazz != null) {
			if (Address.class.isAssignableFrom(clazz)) {
				ob.addWhere(variableName, RDF.type, Vocabulary.Address);
			}
			if (CareOffice.class.isAssignableFrom(clazz)) {
				ob.addWhere(variableName, RDF.type, Vocabulary.CareOffice);
			}
			if (Concessionaire.class.isAssignableFrom(clazz)) {
				ob.addWhere(variableName, RDF.type, Vocabulary.Concessionaire);
			}
			if (Location.class.isAssignableFrom(clazz)) {
				ob.addWhere(variableName, RDF.type, Vocabulary.Location);
			}
			if (Organisation.class.isAssignableFrom(clazz)) {
				ob.addWhere(variableName, RDF.type, Vocabulary.Organisation);
			}
			if (Region.class.isAssignableFrom(clazz)) {
				ob.addWhere(variableName, RDF.type, Vocabulary.Region);
			}
		}

		SelectBuilder b = new SelectBuilder() //
				.addVar(r) //
				.setDistinct(true) //
				.addSubQuery(ob) //
		;

//			if (clazz == null || clazz.isAssignableFrom(Address.class)) {
//			}
		if (clazz == null || clazz.isAssignableFrom(CareOffice.class)) {
			final var rb = new SelectBuilder() //
					.addVar(r) //
					.addWhere(variableName, Vocabulary.region, r) //
			;
			rb.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));
			b.addUnion(rb);

			final var cb = new SelectBuilder() //
					.addVar(r) //
					.addWhere(variableName, Vocabulary.concessionaire, r) //
			;
			cb.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));
			b.addUnion(cb);
		}
//		if (clazz == null || clazz.isAssignableFrom(Concessionaire.class)) {
//
//		}
		if (clazz == null || clazz.isAssignableFrom(Location.class)) {
			final var ab = new SelectBuilder() //
					.addVar(r) //
					.addWhere(variableName, Vocabulary.address, r) //
			;
			ab.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));
			b = b.addUnion(ab);
		}
		if (clazz == null || clazz.isAssignableFrom(Organisation.class)) {
			final var ab = new SelectBuilder() //
					.addVar(r) //
					.addWhere(variableName, Vocabulary.address, r) //
			;
			ab.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));
			b = b.addUnion(ab);

			final String c = GidsService.unique("?c", variableName);

			final var cb = new SelectBuilder() //
					.addVar(r) //
					.addWhere(variableName, Vocabulary.office, r) //
			;
			cb.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));
			b = b.addUnion(cb);
			final var crb = new SelectBuilder() //
					.addVar(r) //
					.addWhere(variableName, Vocabulary.office, c) //
					.addWhere(c, Vocabulary.region, r) //
			;
			crb.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));
			b = b.addUnion(crb);
			final var ccb = new SelectBuilder() //
					.addVar(r) //
					.addWhere(variableName, Vocabulary.office, c) //
					.addWhere(c, Vocabulary.concessionaire, r) //
			;
			ccb.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));
			b = b.addUnion(ccb);

			final String a = GidsService.unique("?a", variableName);

			final var lb = new SelectBuilder() //
					.addVar(r) //
					.addWhere(variableName, Vocabulary.location, r) //
			;
			lb.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));
			b = b.addUnion(lb);
			final var lab = new SelectBuilder() //
					.addVar(r) //
					.addWhere(variableName, Vocabulary.location, a) //
					.addWhere(a, Vocabulary.address, r) //
			;
			lab.getWhereHandler().getClause().addElement(new ElementSubQuery(qq));
			b = b.addUnion(lab);
		}
//			if (clazz == null || clazz.isAssignableFrom(Region.class)) {
//
//			}

		b = b.addUnion(new SelectBuilder() //
				.addVar(r) //
				.setDistinct(true) //
				.addBind(new NodeValueNode(Vocabulary.Root.asNode()), r) //
				.addWhere(r, GidsService.unique("?x", variableName), GidsService.unique("?y", variableName)) //
		);

		return b;
	}

	/**
	 * @param r
	 * @param p
	 * @param o
	 * @param cb
	 * @return
	 */
	protected SelectBuilder createSource(final String r, final String p, final String o, final SelectBuilder cb) {
		final SelectBuilder qs = new SelectBuilder() //
				.setDistinct(true) //
				.addVar(r).addVar(p).addVar(o) //
				.addVar("?so") //
				.addVar("?f") //
				.addVar("?t") //
				.addSubQuery(cb) //
				.addWhere("?st", RDF.subject, r) //
				.addWhere("?st", RDF.type, RDF.Statement) //
				.addWhere("?st", RDF.predicate, p) //
				.addWhere("?st", RDF.object, o) //
				.addWhere("?st", Vocabulary.source, "?so") //
				.addOptional("?st", Vocabulary.from, "?f") //
				.addOptional("?st", Vocabulary.to, "?t") //
		;
		return qs;
	}

	protected void delete(final Graph<? extends Model> g, final GidsAttribute<? extends RDFObject> child,
			final boolean purge) {
		if (child != null && child.getValues() != null) {
			child.getValues().entries().forEach(e -> {
				delete(g, e.getValue().getRight(), purge);
			});
		}
	}

	@Override
	protected void delete(final Graph<? extends Model> g, final RDFObject object, final boolean purge) {
		g.beginWrite();
		try {
			final Resource resource = g.getModel().createResource(object.getId());
			deleteDetails(g, resource, object, purge);
			// Manually remove reifications because using Jena's library function absolutely
			// kills performance
			final Update clearReifications = new UpdateBuilder() //
					.addWhere("?r", RDF.subject, resource) //
					.addWhere("?r", RDF.type, RDF.Statement) //
					.addWhere("?r", "?p", "?o") //
					.addDelete("?r", "?p", "?o") //
					.build();
			UpdateAction.execute(clearReifications, g.getModel());
			g.getModel().removeAll(resource, null, null);
			if (purge) {
				g.getModel().removeAll(null, null, resource);
				final Update purgeReifications = new UpdateBuilder() //
						.addWhere("?r", RDF.object, resource) //
						.addWhere("?r", RDF.type, RDF.Statement) //
						.addDelete("?r", "?p", "?o") //
						.addDelete("?r", "?p", "?o") //
						.build();
				UpdateAction.execute(purgeReifications, g.getModel());
			}
			g.commit();
		} finally {
			g.end();
		}
	}

	@Override
	protected void deleteDetails(final Graph<? extends Model> g, final Resource resource, final RDFObject object,
			final boolean purge) {
		if (object instanceof Organisation) {
			final Organisation o = (Organisation) object;
			delete(g, o.getAddress(), purge);
			CollectionUtils.emptyIfNull(o.getLocation()).forEach(l -> delete(g, l, purge));
		}
	}

	@SuppressWarnings("unchecked")
	private <B extends Address.AddressBuilder<?, ?>> B getAddress(final GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties,
			final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> sources,
			final Resource resource, final B builder) {
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

	private <T> GidsAttribute<T> getAlternatives(final GraphOrRemote graph, final Resource resource,
			final MultiValuedMap<Property, RDFNode> properties,
			final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> sources,
			final Property p, final Function<RDFNode, T> mapper) {
		final Collection<RDFNode> all = properties.get(p);
		final var builder = GidsAttribute.<T>builder();
		for (final RDFNode n : all) {
			try {
				final T v = mapper.apply(n);
				if (v != null) {
					sources.get(Pair.of(p, n)).stream() //
							.map(t -> Triple.of(t.getLeft(), t.getMiddle(),
									GidsService.reverseSources.get(t.getRight()))) //
							.filter(t -> t.getRight() != null) //
							.forEach(s -> builder.alternative(s.getRight(), s.getLeft(), s.getMiddle(), v));
				}
			} catch (final Exception e) {
				// Mask error
			}
		}
		return builder.build();
	}

	private <T> List<GidsAttribute<T>> getAlternativesList(final GraphOrRemote graph, final Resource resource,
			final MultiValuedMap<Property, RDFNode> properties,
			final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> sources,
			final Property p, final Function<RDFNode, T> mapper) {
		final Collection<RDFNode> all = properties.get(p);
		final List<GidsAttribute<T>> result = new ArrayList<>();
		for (final RDFNode n : all) {
			try {
				final T v = mapper.apply(n);
				if (v != null) {
					sources.get(Pair.of(p, n)).stream() //
							.map(t -> Triple.of(t.getLeft(), t.getMiddle(),
									GidsService.reverseSources.get(t.getRight()))) //
							.filter(t -> t.getRight() != null) //
							.map(s -> GidsAttribute.<T>builder() //
									.alternative(s.getRight(), s.getLeft(), s.getMiddle(), v) //
									.build()) //
							.forEach(result::add);
				}
			} catch (final Exception e) {
				// Mask error
			}
		}
		return result.isEmpty() ? null : result;
	}

	@SuppressWarnings("unchecked")
	private <B extends CareOffice.CareOfficeBuilder<?, ?>> B getCareOffice(final GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties,
			final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> sources,
			final Resource resource, final B builder) {
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
	private <B extends Concessionaire.ConcessionaireBuilder<?, ?>> B getConcessionaire(final GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties,
			final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> sources,
			final Resource resource, final B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.name(getAlternatives(graph, resource, properties, sources, Vocabulary.name, RDFService::getString)) //
		;
	}

	private <B extends GidsObject.GidsObjectBuilder<?, ?>> B getGidsObject(final GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return getRDFObject(graph, properties, resource, builder) //
		;
	}

	@SuppressWarnings("unchecked")
	private <U extends GidsObject> U getGidsObject(final GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final Class<GidsObject> t) {
		final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> sources = getSources(
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

	@SuppressWarnings("unchecked")
	private <B extends Location.LocationBuilder<?, ?>> B getLocation(final GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties,
			final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> sources,
			final Resource resource, final B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.name(getAlternativesList(graph, resource, properties, sources, Vocabulary.name, RDFService::getString)) //
				.primaryName(getAlternatives(graph, resource, properties, sources, Vocabulary.primaryName,
						RDFService::getString)) //
				.number(getAlternatives(graph, resource, properties, sources, Vocabulary.number, RDFService::getString)) //
				.agb(getAlternativesList(graph, resource, properties, sources, Vocabulary.agb, RDFService::getString)) //
				.sbi(getAlternativesList(graph, resource, properties, sources, Vocabulary.sbi, RDFService::getString)) //
				.address(getAlternatives(graph, resource, properties, sources, Vocabulary.address,
						n -> getObject(graph, n, Address.class))) //
		;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <U extends RDFObject> U getObject(final GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final Class<U> t) {
		if (GidsObject.class.isAssignableFrom(t))
			return (U) getGidsObject(graph, properties, resource, (Class<GidsObject>) t);
		throw new IllegalArgumentException("Cannot load RDF objects of type " + t.getSimpleName());
	}

	@Override
	protected Map<Resource, Class<? extends RDFObject>> getObjectTypes() {
		return GidsService.objectTypes;
	}

	@SuppressWarnings("unchecked")
	private <B extends Organisation.OrganisationBuilder<?, ?>> B getOrganisation(final GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties,
			final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> sources,
			final Resource resource, final B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.address(getAlternatives(graph, resource, properties, sources, Vocabulary.address,
						n -> getObject(graph, n, Address.class))) //
				.office(getAlternatives(graph, resource, properties, sources, Vocabulary.office,
						n -> getObject(graph, n, CareOffice.class))) //
				.name(getAlternativesList(graph, resource, properties, sources, Vocabulary.name, RDFService::getString)) //
				.primaryName(getAlternatives(graph, resource, properties, sources, Vocabulary.primaryName,
						RDFService::getString)) //
				.lastModified(getAlternatives(graph, resource, properties, sources, Vocabulary.lastModified,
						RDFService::getDateTime)) //
				.agb(getAlternativesList(graph, resource, properties, sources, Vocabulary.agb, RDFService::getString)) //
				.sbi(getAlternativesList(graph, resource, properties, sources, Vocabulary.sbi, RDFService::getString)) //
				.kvk(getAlternatives(graph, resource, properties, sources, Vocabulary.kvk, RDFService::getString)) //
				.location(getAlternativesList(graph, resource, properties, sources, Vocabulary.location,
						n -> getObject(graph, n, Location.class))) //
				.deliveryMethod(getAlternatives(graph, resource, properties, sources, Vocabulary.deliveryMethod,
						n -> n.isResource()
								? RDFService.getEnum(Collections.singletonList(n.asResource()),
										GidsService.reverseDeliveryMethods)
								: null)) //
		;
	}

	@Override
	protected @NotNull String getPrefix() {
		return GidsService.GIDS;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected MultiValuedMap<Property, RDFNode> getProperties(final GraphOrRemote g, final Resource r) {
		if (g.isCache())
			return g.getCache().get(r);
		if (g.isGraph())
			return RDFService.getProperties(g.getGraph(), r);
		if (g.isRemote())
			return (MultiValuedMap) GidsService.search(g, r, null, null, s -> s) //
					.collect(HashSetValuedHashMap::new, (b, s) -> b.put(s.getPredicate(), s.getObject()),
							HashSetValuedHashMap::putAll); //
		throw new IllegalArgumentException(); // Should never be reachable
	}

	@SuppressWarnings("unchecked")
	private <B extends Region.RegionBuilder<?, ?>> B getRegion(final GraphOrRemote graph,
			final MultiValuedMap<Property, RDFNode> properties,
			final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> sources,
			final Resource resource, final B builder) {
		return (B) getGidsObject(graph, properties, resource, builder) //
				.code(getAlternatives(graph, resource, properties, sources, Vocabulary.code, RDFService::getString)) //
		;
	}

	private Collection<Triple<LocalDate, LocalDate, Resource>> getRootSources(final GraphOrRemote graph,
			final Resource resource) {
		if (graph.isCache()) {
			final MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> map = graph
					.getSources().get(Vocabulary.Root);
			if (map != null)
				return map.get(Pair.of(Vocabulary.root, resource));
			return Collections.emptyList();
		}
		final Query q = new SelectBuilder() //
				.setDistinct(true) //
				.addVar("?so") //
				.addVar("?f") //
				.addVar("?t") //
				.addWhere("?st", RDF.subject, Vocabulary.Root) //
				.addWhere("?st", RDF.predicate, Vocabulary.root) //
				.addWhere("?st", RDF.type, RDF.Statement) //
				.addWhere("?st", RDF.object, resource) //
				.addWhere("?st", Vocabulary.source, "?so") //
				.addOptional("?st", Vocabulary.from, "?f") //
				.addOptional("?st", Vocabulary.to, "?t") //
				.build();
		return GidsService.search(graph, q) //
				.map(s -> Triple.of(RDFService.getDate(s.get("?f")), RDFService.getDate(s.get("?t")),
						s.get("?so").asResource())) //
				.collect(Collectors.toList());
	}

	private MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>> getSources(
			final GraphOrRemote graph, final Resource resource) {
		if (graph.isCache())
			return graph.getSources().get(resource);
		final Query q = new SelectBuilder() //
				.setDistinct(true) //
				.addVar("?so") //
				.addVar("?f") //
				.addVar("?t") //
				.addVar("?p") //
				.addVar("?o") //
				.addWhere("?st", RDF.subject, resource) //
				.addWhere("?st", RDF.type, RDF.Statement) //
				.addWhere("?st", RDF.predicate, "?p") //
				.addWhere("?st", RDF.object, "?o") //
				.addWhere("?st", Vocabulary.source, "?so") //
				.addOptional("?st", Vocabulary.from, "?f") //
				.addOptional("?st", Vocabulary.to, "?t") //
				.build();
		return GidsService.search(graph, q) //
				.collect(HashSetValuedHashMap::new,
						(b, s) -> b.put(Pair.of(new PropertyImpl(s.get("?p").asResource().getURI()), s.get("?o")),
								Triple.of(RDFService.getDate(s.get("?f")), RDFService.getDate(s.get("?t")),
										s.get("?so").asResource())),
						HashSetValuedHashMap::putAll); //
	}

	@SuppressWarnings("unchecked")
	public <U extends GidsObject> Optional<GidsAttribute<U>> lookupById(final Graph<? extends Model> graph,
			@NotNull final String id) {
		final GraphOrRemote g = new GraphOrRemote(graph);
		return lookupById(g, id).map(o -> addAlternatives(g, (U) o));
	}

	@SuppressWarnings("unchecked")
	public <U extends GidsObject> Optional<GidsAttribute<U>> lookupById(final String remote, final String auth,
			@NotNull final String id) {
		final GraphOrRemote g = new GraphOrRemote(remote, auth);
		return lookupById(g, id).map(o -> addAlternatives(g, (U) o));
	}

	/**
	 * @param graph
	 * @param qq
	 * @param variableName
	 * @param p
	 * @param o
	 * @return
	 */
	protected Map<Resource, MultiValuedMap<Property, RDFNode>> prefetchCache(final GraphOrRemote graph, final Query qq,
			final String r, final String p, final String o) {
		return GidsService.search(graph, qq) //
				.map(s -> Triple.of(s.getResource(r), s.getResource(p), s.get(o))) //
				.filter(t -> t.getLeft() != null && t.getLeft().isURIResource()) //
				.filter(t -> t.getMiddle() != null && t.getMiddle().isURIResource()) //
				.map(t -> Triple.of(t.getLeft(), ResourceFactory.createProperty(t.getMiddle().getURI()), t.getRight())) //
				.collect(Collectors.groupingBy(Triple::getLeft, Collector.of(HashSetValuedHashMap::new,
						(m, t) -> m.put(t.getMiddle(), t.getRight()), (m, n) -> {
							m.putAll(n);
							return m;
						})));
	}

	/**
	 * @param graph
	 * @param qs
	 * @param r
	 * @param p
	 * @param o
	 */
	protected Map<Resource, MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>>> prefetchSources(
			final GraphOrRemote graph, final Query qs, final String r, final String p, final String o) {
		return GidsService.search(graph, qs) //
				.map(s -> Triple.of(s.getResource(r),
						Pair.of(ResourceFactory.createProperty(s.getResource(p).getURI()), s.get(o)),
						Triple.of(RDFService.getDate(s.get("?f")), RDFService.getDate(s.get("?t")),
								s.getResource("?so")))) //
				.filter(t -> t.getLeft() != null && t.getLeft().isURIResource()) //
				.collect(Collectors.groupingBy(Triple::getLeft, Collector.of(HashSetValuedHashMap::new,
						(m, t) -> m.put(t.getMiddle(), t.getRight()), (m, n) -> {
							m.putAll(n);
							return m;
						})));
	}

	public <U extends GidsObject> List<GidsAttribute<U>> query(final Graph<? extends Model> graph, final Query q,
			final Class<U> clazz) throws ParseException {
		return query(new GraphOrRemote(graph), q, clazz);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <U extends GidsObject> List<GidsAttribute<U>> query(final GraphOrRemote graph, final Query q,
			final Class<U> clazz) throws ParseException {
		if (graph.isCache())
			throw new IllegalArgumentException("Please do not call with a cache direcly");
		if (q == null || !q.isSelectType())
			throw new IllegalArgumentException("Exactly one SELECT query must be given");
		if (q.getProjectVars().size() != 1)
			throw new IllegalArgumentException("Query must project onto exactly one value (the requested resource)");
		final String variableName = "?" + q.getProjectVars().iterator().next().getVarName();
		GidsService.log.trace("Exceuting with variable {} class {} query {}", variableName, clazz, q);

		final String r = GidsService.unique("?r", variableName);
		final String p = GidsService.unique("?p", variableName);
		final String o = GidsService.unique("?o", variableName);
		final SelectBuilder resourceList = createCompleteResourceList(q, clazz, variableName, r);
		final SelectBuilder cb = createCache(r, p, o, resourceList);
		final Map<Resource, MultiValuedMap<Property, RDFNode>> cache = prefetchCache(graph, cb.build(), r, p, o);
		GidsService.log.debug("Cache {} {}", cache.size(),
				cache.values().stream().mapToLong(MultiValuedMap::size).sum());

		final SelectBuilder qs = createSource(r, p, o, resourceList);
		final Map<Resource, MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>>> sources = prefetchSources(
				graph, qs.build(), r, p, o);
		GidsService.log.debug("Sources {} {}", sources.size(),
				sources.values().stream().mapToLong(MultiValuedMap::size).sum());

		final GraphOrRemote g = new GraphOrRemote(cache, sources);
		final List<GidsObject> attributes = GidsService.search(graph, q) //
				.map(s -> {
					try {
						final Resource v = s.getResource(variableName);
//						GidsService.log.debug(" - {} -> {}", s, v);
						return v;
					} catch (final Exception ex) {
						GidsService.log.debug(" - Failed extracting from {}", s);
						return null;
					}
				}) //
				.filter(Objects::nonNull) //
				.filter(cache::containsKey) //
				.map(rr -> getObject(g, rr, GidsObject.class)) //
				.filter(Objects::nonNull) //
				.filter(oo -> clazz == null || clazz.isInstance(oo)) //
				.collect(Collectors.toList());

		return (List) attributes.stream() //
				.map(oo -> addAlternatives(g, oo)) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
	}

	public <U extends GidsObject> List<GidsAttribute<U>> query(final String remote, final Query q, final Class<U> clazz)
			throws ParseException {
		return query(remote, null, q, clazz);
	}

	public <U extends GidsObject> List<GidsAttribute<U>> query(final String remote, final String auth, final Query q,
			final Class<U> clazz) throws ParseException {
		return query(new GraphOrRemote(remote, auth), q, clazz);
	}

	public void save(final Graph<? extends Model> g,
			final Collection<? extends GidsAttribute<? extends GidsObject>> objects) {
		g.beginWrite();
		try {
			CollectionUtils.emptyIfNull(objects).forEach(o -> save(g, o));
			g.commit();
		} finally {
			g.end();
		}
	}

	public void save(final Graph<? extends Model> g, final GidsAttribute<? extends GidsObject> object) {
		addObject(g, Vocabulary.Root, Vocabulary.root, object, true);
	}

	public void save(final String remote, final String auth, final GidsAttribute<? extends GidsObject> object) {
		final Model model = ModelFactory.createDefaultModel();
		final Graph<Model> graph = Graph.create(model);
		save(graph, object);
		try (RDFConnection connection = GidsService.getConnection(GidsService.getUploadURL(remote), auth)) {
			connection.begin(TxnType.WRITE);
			try {
				connection.load(model);
				connection.commit();
			} catch (final Throwable t) {
				connection.abort();
				throw t;
			} finally {
				connection.end();
			}
		}
	}

	protected void saveAddress(final Graph<? extends Model> g, final Resource resource, final Address object) {
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

	protected void saveCareOffice(final Graph<? extends Model> g, final Resource resource, final CareOffice object) {
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

	protected void saveConcessionaire(final Graph<? extends Model> g, final Resource resource,
			final Concessionaire object) {
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

	@Override
	protected void saveDetails(final Graph<? extends Model> g, final Resource resource, final RDFObject object) {
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

	protected void saveLocation(final Graph<? extends Model> g, final Resource resource, final Location object) {
		g.beginWrite();
		try {
			saveGidsObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Location);
			addName(g, resource, object);
			addProperty(g, resource, Vocabulary.number, object.getNumber());
			addAgb(g, resource, object);
			addSbi(g, resource, object);
			addAddress(g, resource, object, true);
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveOrganisation(final Graph<? extends Model> g, final Resource resource,
			final Organisation object) {
		g.beginWrite();
		try {
			saveGidsObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Organisation);
			addAddress(g, resource, object, true);
			addObject(g, resource, Vocabulary.office, object.getOffice(), false);
			addName(g, resource, object);
			addLastModified(g, resource, object);
			addAgb(g, resource, object);
			addSbi(g, resource, object);
			addKvk(g, resource, object);
			addObjects(g, resource, Vocabulary.location, object.getLocation(), true);
			addEnum(g, resource, Vocabulary.deliveryMethod, object.getDeliveryMethod(), GidsService.deliveryMethods);
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveRegion(final Graph<? extends Model> g, final Resource resource, final Region object) {
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

}
