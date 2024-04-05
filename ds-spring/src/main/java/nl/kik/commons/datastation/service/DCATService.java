package nl.kik.commons.datastation.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD4;
import org.springframework.stereotype.Service;

import nl.kik.commons.datastation.dto.dcat.Catalog;
import nl.kik.commons.datastation.dto.dcat.Catalog.CatalogBuilder;
import nl.kik.commons.datastation.dto.dcat.CatalogRecord;
import nl.kik.commons.datastation.dto.dcat.CatalogRecord.CatalogRecordBuilder;
import nl.kik.commons.datastation.dto.dcat.DCATObject;
import nl.kik.commons.datastation.dto.dcat.DCATObject.DCATObjectBuilder;
import nl.kik.commons.datastation.dto.dcat.DataService;
import nl.kik.commons.datastation.dto.dcat.DataService.DataServiceBuilder;
import nl.kik.commons.datastation.dto.dcat.Dataset;
import nl.kik.commons.datastation.dto.dcat.Dataset.DatasetBuilder;
import nl.kik.commons.datastation.dto.dcat.Distribution;
import nl.kik.commons.datastation.dto.dcat.Distribution.DistributionBuilder;
import nl.kik.commons.datastation.dto.dcat.Kind;
import nl.kik.commons.datastation.dto.dcat.PeriodOfTime;
import nl.kik.commons.datastation.dto.dcat.PeriodOfTime.PeriodOfTimeBuilder;
import nl.kik.commons.datastation.dto.dcat.Relationship;
import nl.kik.commons.datastation.dto.dcat.Relationship.RelationshipBuilder;
import nl.kik.commons.datastation.dto.dcat.Resource.ResourceBuilder;
import nl.kik.commons.datastation.dto.dcat.Role;
import nl.kik.commons.datastation.dto.dcat.Role.RoleBuilder;
import nl.kik.commons.datastation.dto.foaf.Agent;
import nl.kik.commons.datastation.dto.foaf.Agent.AgentBuilder;
import nl.kik.commons.datastation.dto.foaf.FOAFObject;
import nl.kik.commons.datastation.dto.foaf.FOAFObject.FOAFObjectBuilder;
import nl.kik.commons.datastation.dto.foaf.Group;
import nl.kik.commons.datastation.dto.foaf.Group.GroupBuilder;
import nl.kik.commons.datastation.dto.foaf.Organization;
import nl.kik.commons.datastation.dto.foaf.Organization.OrganizationBuilder;
import nl.kik.commons.datastation.dto.foaf.Person;
import nl.kik.commons.datastation.dto.foaf.Person.PersonBuilder;
import nl.kik.commons.dto.Graph;
import nl.kik.commons.dto.RDFObject;
import nl.kik.commons.service.AbstractRDFService;
import nl.kik.commons.service.RDFService;

@Service
public class DCATService extends AbstractRDFService<Graph<Model>> {
	public static class Vocabulary extends DCAT {
		private static final Model m = ModelFactory.createDefaultModel();

		public static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";
		public static final Resource FOAF_NAMESPACE = Vocabulary.m.createResource(Vocabulary.FOAF_NS);

		public static final Resource Agent = Vocabulary.m.createResource(Vocabulary.FOAF_NS + "Agent");
		public static final Resource Group = Vocabulary.m.createResource(Vocabulary.FOAF_NS + "Group");
		public static final Resource Organization = Vocabulary.m.createResource(Vocabulary.FOAF_NS + "Organization");
		public static final Resource Person = Vocabulary.m.createResource(Vocabulary.FOAF_NS + "Person");

		public static final Property member = Vocabulary.m.createProperty(Vocabulary.FOAF_NS + "member");
		public static final Property name = Vocabulary.m.createProperty(Vocabulary.FOAF_NS + "name");

	}

	public static final String DCAT = "dcat";

	private static Map<Resource, Class<? extends RDFObject>> objectTypes = Map.ofEntries( //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.CatalogRecord, CatalogRecord.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Distribution, Distribution.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(DCTerms.PeriodOfTime, PeriodOfTime.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Relationship, Relationship.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Resource,
					nl.kik.commons.datastation.dto.dcat.Resource.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.DataService, DataService.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Dataset, Dataset.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Catalog, Catalog.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Role, Role.class), //

			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Agent, Agent.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Group, Group.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Organization, Organization.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Person, Person.class) //
	);

	private static Map<Kind, Resource> kinds = Map.of(//
			Kind.Group, VCARD4.Group, //
			Kind.Individual, VCARD4.Individual, //
			Kind.Location, VCARD4.Location, //
			Kind.Organization, VCARD4.Organization //
	);
	private static Map<Resource, Kind> reverseKinds = RDFService.reverse(DCATService.kinds);

	@Override
	protected void deleteDetails(final Graph<? extends Model> g, final Resource resource, final RDFObject object,
			final boolean purge) {

	}

	private <B extends AgentBuilder<?, ?>> B getAgent(final Graph<Model> graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return getFOAFObject(graph, properties, resource, builder) //
		;
	}

	public Collection<? extends Catalog> getAllCatalogs(final Graph<Model> graph) {
		Map<Resource, RDFObject> existing = new HashMap<>();
		return RDFService.search(graph, null, RDF.type, org.apache.jena.vocabulary.DCAT.Catalog, Statement::getSubject) //
				.map(r -> Pair.of(r, getProperties(graph, r))) //
				.map(p -> getObject(graph, existing, p.getRight(), p.getLeft())) //
				.filter(Catalog.class::isInstance) //
				.map(Catalog.class::cast) //
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private <B extends CatalogBuilder<?, ?>> B getCatalog(final Graph<Model> graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return (B) getDataset(graph, existing, properties, resource, builder) //
				.themeTaxonomy(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.themeTaxonomy,
						RDFObject.class)) //
				.hasPart(getSet(graph, existing, properties, DCTerms.hasPart,
						nl.kik.commons.datastation.dto.dcat.Resource.class)) //
				.dataset(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.dataset, Dataset.class)) //
				.service(
						getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.service, DataService.class)) //
				.catalog(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.catalog, Catalog.class)) //
				.record(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.record,
						CatalogRecord.class)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends CatalogRecordBuilder<?, ?>> B getCatalogRecord(final Graph<Model> graph,
			Map<Resource, RDFObject> existing, final MultiValuedMap<Property, RDFNode> properties,
			final Resource resource, final B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.conformsTo(RDFService.getURISet(properties, DCTerms.conformsTo)) //
				.description(RDFService.getString(properties, DCTerms.description)) //
				.title(RDFService.getString(properties, DCTerms.title)) //
				.issued(RDFService.getDateTime(properties, DCTerms.issued)) //
				.modified(RDFService.getDateTime(properties, DCTerms.modified)) //
				.primaryTopic(getObject(graph, existing, properties, FOAF.primaryTopic, nl.kik.commons.datastation.dto.dcat.Resource.class));
	}

	@SuppressWarnings("unchecked")
	private <B extends DataServiceBuilder<?, ?>> B getDataService(final Graph<Model> graph,
			Map<Resource, RDFObject> existing, final MultiValuedMap<Property, RDFNode> properties,
			final Resource resource, final B builder) {
		return (B) getResource(graph, existing, properties, resource, builder) //
				.endpointURL(RDFService.getURI(properties, org.apache.jena.vocabulary.DCAT.endpointURL)) //
				.endpointDescription(getSet(graph, existing, properties,
						org.apache.jena.vocabulary.DCAT.endpointDescription, RDFObject.class)) //
				.servesDataset(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.servesDataset,
						Dataset.class)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends DatasetBuilder<?, ?>> B getDataset(final Graph<Model> graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return (B) getResource(graph, existing, properties, resource, builder) //
				.distribution(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.distribution,
						Distribution.class)) //
				.accrualPeriodicity(RDFService.getURI(properties, DCTerms.accrualPeriodicity)) //
				.temporalResolution(
						RDFService.getDuration(properties, org.apache.jena.vocabulary.DCAT.temporalResolution))
				.temporal(getObject(graph, existing, properties, DCTerms.temporal, PeriodOfTime.class)) //
				.spatialResolutionInMeters(
						RDFService.getFloat(properties, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters)) //
		;
	}

	private <B extends DCATObjectBuilder<?, ?>> B getDCATObject(final Graph<Model> graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return getRDFObject(graph, properties, resource, builder) //
		;
	}

	@SuppressWarnings("unchecked")
	protected <U extends DCATObject> U getDCATObject(final Graph<Model> graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final Class<U> t) {
		if (Catalog.class.isAssignableFrom(t)) {
			return (U) getCatalog(graph, existing, properties, resource, Catalog.builder()).build();
		}
		if (Dataset.class.isAssignableFrom(t)) {
			return (U) getDataset(graph, existing, properties, resource, Dataset.builder()).build();
		}
		if (DataService.class.isAssignableFrom(t)) {
			return (U) getDataService(graph, existing, properties, resource, DataService.builder()).build();
		}
		if (nl.kik.commons.datastation.dto.dcat.Resource.class.isAssignableFrom(t)) {
			return (U) getResource(graph, existing, properties, resource,
					nl.kik.commons.datastation.dto.dcat.Resource.builder()).build();
		}
		if (CatalogRecord.class.isAssignableFrom(t)) {
			return (U) getCatalogRecord(graph, existing, properties, resource, CatalogRecord.builder()).build();
		} else if (Distribution.class.isAssignableFrom(t)) {
			return (U) getDistribution(graph, existing, properties, resource, Distribution.builder()).build();
		} else if (PeriodOfTime.class.isAssignableFrom(t)) {
			return (U) getPeriodOfTime(graph, properties, resource, PeriodOfTime.builder()).build();
		} else if (Relationship.class.isAssignableFrom(t)) {
			return (U) getRelationship(graph, existing, properties, resource, Relationship.builder()).build();
		} else if (Role.class.isAssignableFrom(t)) {
			return (U) getRole(graph, properties, resource, Role.builder()).build();
		}
		throw new IllegalArgumentException("Cannot load DCAT objects of type " + t.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	private <B extends DistributionBuilder<?, ?>> B getDistribution(final Graph<Model> graph,
			Map<Resource, RDFObject> existing, final MultiValuedMap<Property, RDFNode> properties,
			final Resource resource, final B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.conformsTo(RDFService.getURISet(properties, DCTerms.conformsTo)) //
				.description(RDFService.getString(properties, DCTerms.description)) //
				.title(RDFService.getString(properties, DCTerms.title)) //
				.issued(RDFService.getDateTime(properties, DCTerms.issued)) //
				.modified(RDFService.getDateTime(properties, DCTerms.modified)) //
				.accessURL(RDFService.getURISet(properties, org.apache.jena.vocabulary.DCAT.accessURL)) //
				.accessService(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.accessService,
						DataService.class)) //
				.downloadURL(RDFService.getURISet(properties, org.apache.jena.vocabulary.DCAT.downloadURL)) //
				.byteSize(RDFService.getDouble(properties, org.apache.jena.vocabulary.DCAT.byteSize)) //
				.temporalResolution(
						RDFService.getDuration(properties, org.apache.jena.vocabulary.DCAT.temporalResolution))
				.spatialResolutionInMeters(
						RDFService.getFloat(properties, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters)) //
		;
	}

	private <B extends FOAFObjectBuilder<?, ?>> B getFOAFObject(final Graph<Model> graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return getRDFObject(graph, properties, resource, builder) //
		;
	}

	@SuppressWarnings("unchecked")
	protected <U extends FOAFObject> U getFOAFObject(final Graph<Model> graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final Class<U> t) {
		if (Group.class.isAssignableFrom(t)) {
			return (U) getGroup(graph, existing, properties, resource, Group.builder()).build();
		}
		if (Organization.class.isAssignableFrom(t)) {
			return (U) getOrganization(graph, properties, resource, Organization.builder()).build();
		}
		if (Person.class.isAssignableFrom(t)) {
			return (U) getPerson(graph, properties, resource, Person.builder()).build();
		}
		throw new IllegalArgumentException("Cannot load FOAF objects of type " + t.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	private <B extends GroupBuilder<?, ?>> B getGroup(final Graph<Model> graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return (B) getAgent(graph, properties, resource, builder) //
				.member(getSet(graph, existing, properties, Vocabulary.member, Agent.class)) //
		;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <U extends RDFObject> U getObject(final Graph<Model> graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final Class<U> t) {
		if (DCATObject.class.isAssignableFrom(t)) {
			return (U) getDCATObject(graph, existing, properties, resource, (Class<DCATObject>) t);
		}
		if (FOAFObject.class.isAssignableFrom(t)) {
			return (U) getFOAFObject(graph, existing, properties, resource, (Class<FOAFObject>) t);
		}
		throw new IllegalArgumentException("Cannot load RDF objects of type " + t.getSimpleName());
	}

	@Override
	protected Map<Resource, Class<? extends RDFObject>> getObjectTypes() {
		return DCATService.objectTypes;
	}

	@SuppressWarnings("unchecked")
	private <B extends OrganizationBuilder<?, ?>> B getOrganization(final Graph<Model> graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return (B) getAgent(graph, properties, resource, builder) //
				.name(RDFService.getString(properties, Vocabulary.name)) //
				.type(RDFService.getURI(properties, DCTerms.type)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends PeriodOfTimeBuilder<?, ?>> B getPeriodOfTime(final Graph<Model> graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.startDate(RDFService.getDateTime(properties, org.apache.jena.vocabulary.DCAT.startDate)) //
				.endDate(RDFService.getDateTime(properties, org.apache.jena.vocabulary.DCAT.endDate)) //
		;
	}

	private <B extends PersonBuilder<?, ?>> B getPerson(final Graph<Model> graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return getAgent(graph, properties, resource, builder) //
		;
	}

	@Override
	protected @NotNull String getPrefix() {
		return DCATService.DCAT;
	}

	@SuppressWarnings("unchecked")
	private <B extends RelationshipBuilder<?, ?>> B getRelationship(final Graph<Model> graph,
			Map<Resource, RDFObject> existing, final MultiValuedMap<Property, RDFNode> properties,
			final Resource resource, final B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.relation(getSet(graph, existing, properties, DCTerms.relation, RDFObject.class)) //
				.hadRole(getObject(graph, existing, properties, org.apache.jena.vocabulary.DCAT.hadRole, Role.class)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends ResourceBuilder<?, ?>> B getResource(final Graph<Model> graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.conformsTo(RDFService.getURISet(properties, DCTerms.conformsTo)) //
				.contactPoint(RDFService.getEnum(properties, org.apache.jena.vocabulary.DCAT.contactPoint,
						DCATService.reverseKinds)) //
				.creator(getObject(graph, existing, properties, DCTerms.creator, Agent.class)) //
				.description(RDFService.getString(properties, DCTerms.description)) //
				.title(RDFService.getString(properties, DCTerms.title)) //
				.issued(RDFService.getDateTime(properties, DCTerms.issued)) //
				.modified(RDFService.getDateTime(properties, DCTerms.modified)) //
				.publisher(getObject(graph, existing, properties, DCTerms.publisher, Agent.class)) //
				.identifier(RDFService.getString(properties, DCTerms.identifier)) //
				.relation(getSet(graph, existing, properties, DCTerms.relation, RDFObject.class)) //
				.qualifiedRelation(getSet(graph, existing, properties,
						org.apache.jena.vocabulary.DCAT.qualifiedRelation, Relationship.class)) //
				.keyword(RDFService.getStringSet(properties, org.apache.jena.vocabulary.DCAT.keyword)) //
				.license(RDFService.getURI(properties, DCTerms.license)) //
				.isReferencedBy(getSet(graph, existing, properties, DCTerms.isReferencedBy, RDFObject.class)) //
		;
	}

	private <B extends RoleBuilder<?, ?>> B getRole(final Graph<Model> graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
		return getDCATObject(graph, properties, resource, builder) //
		;
	}

	protected void saveAgent(final Graph<? extends Model> g, final Resource resource, final Agent object) {
		g.beginWrite();
		try {
			saveFOAFObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Agent);
			addProperty(g, resource, Vocabulary.name, object.getName());
			RDFService.addURI(g, resource, DCTerms.type, object.getType());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveCatalog(final Graph<? extends Model> g, final Resource resource, final Catalog object) {
		g.beginWrite();
		try {
			saveDataset(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Catalog);
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.themeTaxonomy, object.getThemeTaxonomy());// This will
			// fail with
			// the
			// default
			// implementation; must be
			// handled in a subclass if used
			addAll(g, resource, DCTerms.hasPart, object.getHasPart());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.dataset, object.getDataset());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.service, object.getService());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.catalog, object.getCatalog());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.record, object.getRecord());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveCatalogRecord(final Graph<? extends Model> g, final Resource resource,
			final CatalogRecord object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.CatalogRecord);
			CollectionUtils.emptyIfNull(object.getConformsTo())
					.forEach(c -> RDFService.addURI(g, resource, DCTerms.conformsTo, c));
			addProperty(g, resource, DCTerms.description, object.getDescription());
			addProperty(g, resource, DCTerms.title, object.getTitle());
			addProperty(g, resource, DCTerms.issued, object.getIssued());
			addProperty(g, resource, DCTerms.modified, object.getModified());
			addObject(g, resource, FOAF.primaryTopic, object.getPrimaryTopic());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveDataService(final Graph<? extends Model> g, final Resource resource, final DataService object) {
		g.beginWrite();
		try {
			saveResource(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.DataService);
			RDFService.addURI(g, resource, org.apache.jena.vocabulary.DCAT.endpointURL, object.getEndpointURL());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.endpointDescription, object.getEndpointDescription());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.servesDataset, object.getServesDataset());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveDataset(final Graph<? extends Model> g, final Resource resource, final Dataset object) {
		g.beginWrite();
		try {
			saveResource(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Dataset);
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.distribution, object.getDistribution());
			RDFService.addURI(g, resource, DCTerms.accrualPeriodicity, object.getAccrualPeriodicity());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters,
					object.getSpatialResolutionInMeters());
			addObject(g, resource, DCTerms.temporal, object.getTemporal());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.temporalResolution,
					object.getTemporalResolution());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveDCATObject(final Graph<? extends Model> g, final Resource resource, final DCATObject object) {
		saveRDFObject(g, resource, object);
	}

	protected void saveDetails(final Graph<? extends Model> g, final Resource resource, final DCATObject object) {
		if (object instanceof Catalog) {
			saveCatalog(g, resource, (Catalog) object);
		} else if (object instanceof Dataset) {
			saveDataset(g, resource, (Dataset) object);
		} else if (object instanceof DataService) {
			saveDataService(g, resource, (DataService) object);
		} else if (object instanceof nl.kik.commons.datastation.dto.dcat.Resource) {
			saveResource(g, resource, (nl.kik.commons.datastation.dto.dcat.Resource) object);
		} else if (object instanceof CatalogRecord) {
			saveCatalogRecord(g, resource, (CatalogRecord) object);
		} else if (object instanceof Distribution) {
			saveDistribution(g, resource, (Distribution) object);
		} else if (object instanceof PeriodOfTime) {
			savePeriodOfTime(g, resource, (PeriodOfTime) object);
		} else if (object instanceof Relationship) {
			saveRelationship(g, resource, (Relationship) object);
		} else if (object instanceof Role) {
			saveRole(g, resource, (Role) object);
		} else {
			throw new IllegalArgumentException("Cannot save DCAT objects of type " + object.getClass().getSimpleName());
		}
	}

	protected void saveDetails(final Graph<? extends Model> g, final Resource resource, final FOAFObject object) {
		if (object instanceof Group) {
			saveGroup(g, resource, (Group) object);
		} else if (object instanceof Organization) {
			saveOrganization(g, resource, (Organization) object);
		} else if (object instanceof Person) {
			savePerson(g, resource, (Person) object);
//		} else if (object instanceof Agent) { Abstract class, skip
//			saveAgent(g, resource, (Agent) object);
		} else {
			throw new IllegalArgumentException("Cannot save FOAF objects of type " + object.getClass().getSimpleName());
		}
	}

	@Override
	protected void saveDetails(final Graph<? extends Model> g, final Resource resource, final RDFObject object) {
		if (object instanceof DCATObject) {
			saveDetails(g, resource, (DCATObject) object);
		} else if (object instanceof FOAFObject) {
			saveDetails(g, resource, (FOAFObject) object);
		} else {
			throw new IllegalArgumentException("Cannot save RDF objects of type " + object.getClass().getSimpleName());
		}
	}

	protected void saveDistribution(final Graph<? extends Model> g, final Resource resource,
			final Distribution object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Distribution);
			CollectionUtils.emptyIfNull(object.getConformsTo())
					.forEach(c -> RDFService.addURI(g, resource, DCTerms.conformsTo, c));
			addProperty(g, resource, DCTerms.description, object.getDescription());
			addProperty(g, resource, DCTerms.title, object.getTitle());
			addProperty(g, resource, DCTerms.issued, object.getIssued());
			addProperty(g, resource, DCTerms.modified, object.getModified());
			RDFService.addAllURIs(g, resource, org.apache.jena.vocabulary.DCAT.accessURL, object.getAccessURL());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.accessService, object.getAccessService());
			RDFService.addAllURIs(g, resource, org.apache.jena.vocabulary.DCAT.downloadURL, object.getDownloadURL());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.byteSize, object.getByteSize());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters,
					object.getSpatialResolutionInMeters());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.temporalResolution,
					object.getTemporalResolution());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveFOAFObject(final Graph<? extends Model> g, final Resource resource, final FOAFObject object) {
		saveRDFObject(g, resource, object);
	}

	protected void saveGroup(final Graph<? extends Model> g, final Resource resource, final Group object) {
		g.beginWrite();
		try {
			saveAgent(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Group);
			addAll(g, resource, Vocabulary.member, object.getMember());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveOrganization(final Graph<? extends Model> g, final Resource resource,
			final Organization object) {
		g.beginWrite();
		try {
			saveAgent(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Organization);
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void savePeriodOfTime(final Graph<? extends Model> g, final Resource resource,
			final PeriodOfTime object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, DCTerms.PeriodOfTime);
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.startDate, object.getStartDate());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.endDate, object.getEndDate());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void savePerson(final Graph<? extends Model> g, final Resource resource, final Person object) {
		g.beginWrite();
		try {
			saveAgent(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Person);
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveRelationship(final Graph<? extends Model> g, final Resource resource,
			final Relationship object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Relationship);
			addAll(g, resource, DCTerms.relation, object.getRelation());
			addObject(g, resource, org.apache.jena.vocabulary.DCAT.hadRole, object.getHadRole());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveResource(final Graph<? extends Model> g, final Resource resource,
			final nl.kik.commons.datastation.dto.dcat.Resource object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Resource);
			CollectionUtils.emptyIfNull(object.getConformsTo())
					.forEach(c -> RDFService.addURI(g, resource, DCTerms.conformsTo, c));
			if (object.getContactPoint() != null) {
				g.getModel().add(resource, org.apache.jena.vocabulary.DCAT.contactPoint,
						DCATService.kinds.get(object.getContactPoint()));
			}
			addObject(g, resource, DCTerms.creator, object.getCreator());
			addProperty(g, resource, DCTerms.description, object.getDescription());
			addProperty(g, resource, DCTerms.title, object.getTitle());
			addProperty(g, resource, DCTerms.issued, object.getIssued());
			addProperty(g, resource, DCTerms.modified, object.getModified());
			addObject(g, resource, DCTerms.publisher, object.getPublisher());
			addProperty(g, resource, DCTerms.identifier, object.getIdentifier());
			addAll(g, resource, DCTerms.relation, object.getRelation());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.qualifiedRelation, object.getQualifiedRelation());
			addAllProperties(g, resource, org.apache.jena.vocabulary.DCAT.keyword, object.getKeyword());
			RDFService.addURI(g, resource, DCTerms.license, object.getLicense());
			addAll(g, resource, DCTerms.isReferencedBy, object.getIsReferencedBy());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveRole(final Graph<? extends Model> g, final Resource resource, final Role object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Role);
			g.commit();
		} finally {
			g.end();
		}
	}

}
