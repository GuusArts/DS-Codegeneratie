package nl.kik.datastation.service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD4;
import org.springframework.stereotype.Service;

import nl.kik.datastation.dto.Graph;
import nl.kik.datastation.dto.RDFObject;
import nl.kik.datastation.dto.dcat.Catalog;
import nl.kik.datastation.dto.dcat.Catalog.CatalogBuilder;
import nl.kik.datastation.dto.dcat.CatalogRecord;
import nl.kik.datastation.dto.dcat.CatalogRecord.CatalogRecordBuilder;
import nl.kik.datastation.dto.dcat.DCATObject;
import nl.kik.datastation.dto.dcat.DCATObject.DCATObjectBuilder;
import nl.kik.datastation.dto.dcat.DataService;
import nl.kik.datastation.dto.dcat.DataService.DataServiceBuilder;
import nl.kik.datastation.dto.dcat.Dataset;
import nl.kik.datastation.dto.dcat.Dataset.DatasetBuilder;
import nl.kik.datastation.dto.dcat.Distribution;
import nl.kik.datastation.dto.dcat.Distribution.DistributionBuilder;
import nl.kik.datastation.dto.dcat.Kind;
import nl.kik.datastation.dto.dcat.PeriodOfTime;
import nl.kik.datastation.dto.dcat.PeriodOfTime.PeriodOfTimeBuilder;
import nl.kik.datastation.dto.dcat.Relationship;
import nl.kik.datastation.dto.dcat.Relationship.RelationshipBuilder;
import nl.kik.datastation.dto.dcat.Resource.ResourceBuilder;
import nl.kik.datastation.dto.dcat.Role;
import nl.kik.datastation.dto.dcat.Role.RoleBuilder;
import nl.kik.datastation.dto.foaf.Agent;
import nl.kik.datastation.dto.foaf.Agent.AgentBuilder;
import nl.kik.datastation.dto.foaf.FOAFObject;
import nl.kik.datastation.dto.foaf.FOAFObject.FOAFObjectBuilder;
import nl.kik.datastation.dto.foaf.Group;
import nl.kik.datastation.dto.foaf.Group.GroupBuilder;
import nl.kik.datastation.dto.foaf.Organization;
import nl.kik.datastation.dto.foaf.Organization.OrganizationBuilder;
import nl.kik.datastation.dto.foaf.Person;
import nl.kik.datastation.dto.foaf.Person.PersonBuilder;

@Service
public class DCATService extends AbstractRDFService {
	public static class Vocabulary extends DCAT {
		private static final Model m = ModelFactory.createDefaultModel();

		public static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";
		public static final Resource FOAF_NAMESPACE = m.createResource(FOAF_NS);

		public static final Resource Agent = m.createResource(FOAF_NS + "Agent");
		public static final Resource Group = m.createResource(FOAF_NS + "Group");
		public static final Resource Organization = m.createResource(FOAF_NS + "Organization");
		public static final Resource Person = m.createResource(FOAF_NS + "Person");

		public static final Property member = m.createProperty(FOAF_NS + "member");
		public static final Property name = m.createProperty(FOAF_NS + "name");

	}

	public static final String DCAT = "dcat";

	private static Map<Resource, Class<? extends RDFObject>> objectTypes = Map.ofEntries( //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.CatalogRecord, CatalogRecord.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Distribution, Distribution.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(DCTerms.PeriodOfTime, PeriodOfTime.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Relationship, Relationship.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Resource,
					nl.kik.datastation.dto.dcat.Resource.class), //
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
	private static Map<Resource, Kind> reverseKinds = reverse(kinds);

	@Override
	protected void saveDetails(Graph<? extends Model> g, Resource resource, RDFObject object) {
		if (object instanceof DCATObject) {
			saveDetails(g, resource, (DCATObject) object);
		} else if (object instanceof FOAFObject) {
			saveDetails(g, resource, (FOAFObject) object);
		} else {
			throw new IllegalArgumentException("Cannot save RDF objects of type " + object.getClass().getSimpleName());
		}
	}

	protected void saveDetails(Graph<? extends Model> g, Resource resource, DCATObject object) {
		if (object instanceof Catalog) {
			saveCatalog(g, resource, (Catalog) object);
		} else if (object instanceof Dataset) {
			saveDataset(g, resource, (Dataset) object);
		} else if (object instanceof DataService) {
			saveDataService(g, resource, (DataService) object);
		} else if (object instanceof nl.kik.datastation.dto.dcat.Resource) {
			saveResource(g, resource, (nl.kik.datastation.dto.dcat.Resource) object);
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

	protected void saveDCATObject(Graph<? extends Model> g, Resource resource, DCATObject object) {
		saveRDFObject(g, resource, object);
	}

	protected void saveCatalog(Graph<? extends Model> g, Resource resource, Catalog object) {
		g.beginWrite();
		try {
			saveDataset(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Catalog);
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.themeTaxonomy, object.getThemeTaxonomy());// This will fail with the default
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

	protected void saveDataset(Graph<? extends Model> g, Resource resource, Dataset object) {
		g.beginWrite();
		try {
			saveResource(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Dataset);
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.distribution, object.getDistribution());
			addURL(g, resource, DCTerms.accrualPeriodicity, object.getAccrualPeriodicity());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters, object.getSpatialResolutionInMeters());
			addObject(g, resource, DCTerms.temporal, object.getTemporal());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.temporalResolution, object.getTemporalResolution());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveDataService(Graph<? extends Model> g, Resource resource, DataService object) {
		g.beginWrite();
		try {
			saveResource(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.DataService);
			addURL(g, resource, org.apache.jena.vocabulary.DCAT.endpointURL, object.getEndpointURL());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.endpointDescription, object.getEndpointDescription());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.servesDataset, object.getServesDataset());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveResource(Graph<? extends Model> g, Resource resource,
			nl.kik.datastation.dto.dcat.Resource object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Resource);
			addURL(g, resource, DCTerms.conformsTo, object.getConformsTo());
			if (object.getContactPoint() != null) {
				g.getModel().add(resource, org.apache.jena.vocabulary.DCAT.contactPoint, kinds.get(object.getContactPoint()));
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
			addURL(g, resource, DCTerms.license, object.getLicense());
			addAll(g, resource, DCTerms.isReferencedBy, object.getIsReferencedBy());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveCatalogRecord(Graph<? extends Model> g, Resource resource, CatalogRecord object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.CatalogRecord);
			addURL(g, resource, DCTerms.conformsTo, object.getConformsTo());
			addProperty(g, resource, DCTerms.description, object.getDescription());
			addProperty(g, resource, DCTerms.title, object.getTitle());
			addProperty(g, resource, DCTerms.issued, object.getIssued());
			addProperty(g, resource, DCTerms.modified, object.getModified());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveDistribution(Graph<? extends Model> g, Resource resource, Distribution object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Distribution);
			addURL(g, resource, DCTerms.conformsTo, object.getConformsTo());
			addProperty(g, resource, DCTerms.description, object.getDescription());
			addProperty(g, resource, DCTerms.title, object.getTitle());
			addProperty(g, resource, DCTerms.issued, object.getIssued());
			addProperty(g, resource, DCTerms.modified, object.getModified());
			addAllURLs(g, resource, org.apache.jena.vocabulary.DCAT.accessURL, object.getAccessURL());
			addAll(g, resource, org.apache.jena.vocabulary.DCAT.accessService, object.getAccessService());
			addAllURLs(g, resource, org.apache.jena.vocabulary.DCAT.downloadURL, object.getDownloadURL());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.byteSize, object.getByteSize());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters, object.getSpatialResolutionInMeters());
			addProperty(g, resource, org.apache.jena.vocabulary.DCAT.temporalResolution, object.getTemporalResolution());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void savePeriodOfTime(Graph<? extends Model> g, Resource resource, PeriodOfTime object) {
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

	protected void saveRelationship(Graph<? extends Model> g, Resource resource, Relationship object) {
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

	protected void saveRole(Graph<? extends Model> g, Resource resource, Role object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Role);
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveDetails(Graph<? extends Model> g, Resource resource, FOAFObject object) {
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

	protected void saveFOAFObject(Graph<? extends Model> g, Resource resource, FOAFObject object) {
		saveRDFObject(g, resource, object);
	}

	protected void saveAgent(Graph<? extends Model> g, Resource resource, Agent object) {
		g.beginWrite();
		try {
			saveFOAFObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Agent);
			addProperty(g, resource, Vocabulary.name, object.getName());
			addURL(g, resource, DCTerms.type, object.getType());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveGroup(Graph<? extends Model> g, Resource resource, Group object) {
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

	protected void saveOrganization(Graph<? extends Model> g, Resource resource, Organization object) {
		g.beginWrite();
		try {
			saveAgent(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Organization);
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void savePerson(Graph<? extends Model> g, Resource resource, Person object) {
		g.beginWrite();
		try {
			saveAgent(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Person);
			g.commit();
		} finally {
			g.end();
		}
	}

	@Override
	protected void deleteDetails(Graph<? extends Model> g, Resource resource, RDFObject object, boolean purge) {

	}

	@Override
	protected @NotNull String getPrefix() {
		return DCAT;
	}

	@Override
	protected Map<Resource, Class<? extends RDFObject>> getObjectTypes() {
		return objectTypes;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <U extends RDFObject> U getObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, Class<U> t) {
		if (DCATObject.class.isAssignableFrom(t)) {
			return (U) getDCATObject(graph, properties, resource, (Class<DCATObject>) t);
		} else if (FOAFObject.class.isAssignableFrom(t)) {
			return (U) getFOAFObject(graph, properties, resource, (Class<FOAFObject>) t);
		}
		throw new IllegalArgumentException("Cannot load RDF objects of type " + t.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	protected <U extends DCATObject> U getDCATObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, Class<U> t) {
		if (Catalog.class.isAssignableFrom(t)) {
			return (U) getCatalog(graph, properties, resource, Catalog.builder()).build();
		} else if (Dataset.class.isAssignableFrom(t)) {
			return (U) getDataset(graph, properties, resource, Dataset.builder()).build();
		} else if (DataService.class.isAssignableFrom(t)) {
			return (U) getDataService(graph, properties, resource, DataService.builder()).build();
		} else if (nl.kik.datastation.dto.dcat.Resource.class.isAssignableFrom(t)) {
			return (U) getResource(graph, properties, resource, nl.kik.datastation.dto.dcat.Resource.builder()).build();
		} else if (CatalogRecord.class.isAssignableFrom(t)) {
			return (U) getCatalogRecord(graph, properties, resource, CatalogRecord.builder()).build();
		} else if (Distribution.class.isAssignableFrom(t)) {
			return (U) getDistribution(graph, properties, resource, Distribution.builder()).build();
		} else if (PeriodOfTime.class.isAssignableFrom(t)) {
			return (U) getPeriodOfTime(graph, properties, resource, PeriodOfTime.builder()).build();
		} else if (Relationship.class.isAssignableFrom(t)) {
			return (U) getRelationship(graph, properties, resource, Relationship.builder()).build();
		} else if (Role.class.isAssignableFrom(t)) {
			return (U) getRole(graph, properties, resource, Role.builder()).build();
		}
		throw new IllegalArgumentException("Cannot load DCAT objects of type " + t.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	private <B extends CatalogBuilder<?, ?>> B getCatalog(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getDataset(graph, properties, resource, builder) //
				.themeTaxonomy(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.themeTaxonomy, RDFObject.class)) //
				.hasPart(getSet(graph, properties, DCTerms.hasPart, nl.kik.datastation.dto.dcat.Resource.class)) //
				.dataset(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.dataset, Dataset.class)) //
				.service(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.service, DataService.class)) //
				.catalog(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.catalog, Catalog.class)) //
				.record(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.record, CatalogRecord.class)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends DatasetBuilder<?, ?>> B getDataset(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getResource(graph, properties, resource, builder) //
				.distribution(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.distribution, Distribution.class)) //
				.accrualPeriodicity(getURL(properties, DCTerms.accrualPeriodicity)) //
				.temporalResolution(getDuration(properties, org.apache.jena.vocabulary.DCAT.temporalResolution))
				.temporal(getObject(graph, properties, DCTerms.temporal, PeriodOfTime.class)) //
				.spatialResolutionInMeters(getFloat(properties, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends DataServiceBuilder<?, ?>> B getDataService(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getResource(graph, properties, resource, builder) //
				.endpointURL(getURL(properties, org.apache.jena.vocabulary.DCAT.endpointURL)) //
				.endpointDescription(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.endpointDescription, RDFObject.class)) //
				.servesDataset(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.servesDataset, Dataset.class)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends ResourceBuilder<?, ?>> B getResource(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.conformsTo(getURL(properties, DCTerms.conformsTo)) //
				.contactPoint(getEnum(properties, org.apache.jena.vocabulary.DCAT.contactPoint, reverseKinds)) //
				.creator(getObject(graph, properties, DCTerms.creator, Agent.class)) //
				.description(getString(properties, DCTerms.description)) //
				.title(getString(properties, DCTerms.title)) //
				.issued(getDateTime(properties, DCTerms.issued)) //
				.modified(getDateTime(properties, DCTerms.modified)) //
				.publisher(getObject(graph, properties, DCTerms.publisher, Agent.class)) //
				.identifier(getString(properties, DCTerms.identifier)) //
				.relation(getSet(graph, properties, DCTerms.relation, RDFObject.class)) //
				.qualifiedRelation(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.qualifiedRelation, Relationship.class)) //
				.keyword(getStringSet(properties, org.apache.jena.vocabulary.DCAT.keyword)) //
				.license(getURL(properties, DCTerms.license)) //
				.isReferencedBy(getSet(graph, properties, DCTerms.isReferencedBy, RDFObject.class)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends CatalogRecordBuilder<?, ?>> B getCatalogRecord(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.conformsTo(getURL(properties, DCTerms.conformsTo)) //
				.description(getString(properties, DCTerms.description)) //
				.title(getString(properties, DCTerms.title)) //
				.issued(getDateTime(properties, DCTerms.issued)) //
				.modified(getDateTime(properties, DCTerms.modified)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends DistributionBuilder<?, ?>> B getDistribution(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.conformsTo(getURL(properties, DCTerms.conformsTo)) //
				.description(getString(properties, DCTerms.description)) //
				.title(getString(properties, DCTerms.title)) //
				.issued(getDateTime(properties, DCTerms.issued)) //
				.modified(getDateTime(properties, DCTerms.modified)) //
				.accessURL(getURLSet(properties, org.apache.jena.vocabulary.DCAT.accessURL)) //
				.accessService(getSet(graph, properties, org.apache.jena.vocabulary.DCAT.accessService, DataService.class)) //
				.downloadURL(getURLSet(properties, org.apache.jena.vocabulary.DCAT.downloadURL)) //
				.byteSize(getDouble(properties, org.apache.jena.vocabulary.DCAT.byteSize)) //
				.temporalResolution(getDuration(properties, org.apache.jena.vocabulary.DCAT.temporalResolution))
				.spatialResolutionInMeters(getFloat(properties, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends PeriodOfTimeBuilder<?, ?>> B getPeriodOfTime(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.startDate(getDateTime(properties, org.apache.jena.vocabulary.DCAT.startDate)) //
				.endDate(getDateTime(properties, org.apache.jena.vocabulary.DCAT.endDate)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends RelationshipBuilder<?, ?>> B getRelationship(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getDCATObject(graph, properties, resource, builder) //
				.relation(getSet(graph, properties, DCTerms.relation, RDFObject.class)) //
				.hadRole(getObject(graph, properties, org.apache.jena.vocabulary.DCAT.hadRole, Role.class)) //
		;
	}

	private <B extends RoleBuilder<?, ?>> B getRole(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return getDCATObject(graph, properties, resource, builder) //
		;
	}

	private <B extends DCATObjectBuilder<?, ?>> B getDCATObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return getRDFObject(graph, properties, resource, builder) //
		;
	}

	@SuppressWarnings("unchecked")
	protected <U extends FOAFObject> U getFOAFObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, Class<U> t) {
		if (Group.class.isAssignableFrom(t)) {
			return (U) getGroup(graph, properties, resource, Group.builder()).build();
		} else if (Organization.class.isAssignableFrom(t)) {
			return (U) getOrganization(graph, properties, resource, Organization.builder()).build();
		} else if (Person.class.isAssignableFrom(t)) {
			return (U) getPerson(graph, properties, resource, Person.builder()).build();
		}
		throw new IllegalArgumentException("Cannot load FOAF objects of type " + t.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	private <B extends GroupBuilder<?, ?>> B getGroup(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getAgent(graph, properties, resource, builder) //
				.member(getSet(graph, properties, Vocabulary.member, Agent.class)) //
		;
	}

	@SuppressWarnings("unchecked")
	private <B extends OrganizationBuilder<?, ?>> B getOrganization(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) getAgent(graph, properties, resource, builder) //
				.name(getString(properties, Vocabulary.name)) //
				.type(getURL(properties, DCTerms.type)) //
		;
	}

	private <B extends PersonBuilder<?, ?>> B getPerson(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return getAgent(graph, properties, resource, builder) //
		;
	}

	private <B extends AgentBuilder<?, ?>> B getAgent(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return getFOAFObject(graph, properties, resource, builder) //
		;
	}

	private <B extends FOAFObjectBuilder<?, ?>> B getFOAFObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return getRDFObject(graph, properties, resource, builder) //
		;
	}

	public Collection<? extends Catalog> getAllCatalogs(Graph<? extends Model> graph) {
		return search(graph, null, RDF.type, Vocabulary.Catalog, Statement::getSubject) //
				.map(r -> Pair.of(r, getProperties(graph, r))) //
				.map(p -> getObject(graph, p.getRight(), p.getLeft())) //
				.filter(Catalog.class::isInstance) //
				.map(Catalog.class::cast) //
				.collect(Collectors.toList());
	}

}
