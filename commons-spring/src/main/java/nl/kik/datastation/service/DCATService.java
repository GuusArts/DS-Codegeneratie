package nl.kik.datastation.service;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD;
import org.apache.jena.vocabulary.VCARD4;
import org.springframework.stereotype.Service;

import nl.kik.datastation.dto.Graph;
import nl.kik.datastation.dto.RDFObject;
import nl.kik.datastation.dto.dcat.Catalog;
import nl.kik.datastation.dto.dcat.CatalogRecord;
import nl.kik.datastation.dto.dcat.DCATObject;
import nl.kik.datastation.dto.dcat.DataService;
import nl.kik.datastation.dto.dcat.Dataset;
import nl.kik.datastation.dto.dcat.Distribution;
import nl.kik.datastation.dto.dcat.Kind;
import nl.kik.datastation.dto.dcat.PeriodOfTime;
import nl.kik.datastation.dto.dcat.Relationship;
import nl.kik.datastation.dto.dcat.Role;
import nl.kik.datastation.dto.foaf.Agent;
import nl.kik.datastation.dto.foaf.FOAFObject;
import nl.kik.datastation.dto.foaf.Group;
import nl.kik.datastation.dto.foaf.Organization;
import nl.kik.datastation.dto.foaf.Person;

@Service
public class DCATService extends AbstractService {
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
	private static Map<Class<? extends RDFObject>, Resource> reverseObjects = reverse(objectTypes);

	private static Map<Kind, Resource> kinds = Map.of(//
			Kind.Group, VCARD4.Group, //
			Kind.Individual, VCARD4.Individual, //
			Kind.Location, VCARD4.Location, //
			Kind.Organization, VCARD4.Organization //
	);

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
			g.getModel().add(resource, RDF.type, Vocabulary.Catalog);
			addAll(g, resource, Vocabulary.themeTaxonomy, object.getThemeTaxonomy());// This will fail with the default
																						// implementation; must be
																						// handled in a subclass if used
			addAll(g, resource, DCTerms.hasPart, object.getHasPart());
			addAll(g, resource, Vocabulary.dataset, object.getDataset());
			addAll(g, resource, Vocabulary.service, object.getService());
			addAll(g, resource, Vocabulary.catalog, object.getCatalog());
			addAll(g, resource, Vocabulary.record, object.getRecord());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveDataset(Graph<? extends Model> g, Resource resource, Dataset object) {
		g.beginWrite();
		try {
			saveResource(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Dataset);
			addAll(g, resource, Vocabulary.distribution, object.getDistribution());
			addURL(g, resource, DCTerms.accrualPeriodicity, object.getAccrualPeriodicity());
			addProperty(g, resource, Vocabulary.spatialResolutionInMeters, object.getSpatialResolutionInMeters());
			addObject(g, resource, DCTerms.temporal, object.getTemporal());
			addProperty(g, resource, Vocabulary.temporalResolution, object.getTemporalResolution());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveDataService(Graph<? extends Model> g, Resource resource, DataService object) {
		g.beginWrite();
		try {
			saveResource(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.DataService);
			addURL(g, resource, Vocabulary.endpointURL, object.getEndpointURL());
			addAll(g, resource, Vocabulary.endpointDescription, object.getEndpointDescription());
			addAll(g, resource, Vocabulary.servesDataset, object.getServesDataset());
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
			g.getModel().add(resource, RDF.type, Vocabulary.Resource);
			addProperty(g, resource, DCTerms.conformsTo, object.getConformsTo());
			if (object.getContactPoint() != null) {
				g.getModel().add(resource, Vocabulary.contactPoint, kinds.get(object.getContactPoint()));
			}
			addObject(g, resource, DCTerms.creator, object.getCreator());
			addProperty(g, resource, DCTerms.description, object.getDescription());
			addProperty(g, resource, DCTerms.title, object.getTitle());
			addProperty(g, resource, DCTerms.issued, object.getIssued());
			addProperty(g, resource, DCTerms.modified, object.getModified());
			addObject(g, resource, DCTerms.publisher, object.getPublisher());
			addProperty(g, resource, DCTerms.identifier, object.getIdentifier());
			addAll(g, resource, DCTerms.relation, object.getRelation());
			addAll(g, resource, Vocabulary.qualifiedRelation, object.getQualifiedRelation());
			addAllProperties(g, resource, Vocabulary.keyword, object.getKeyword());
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
			g.getModel().add(resource, RDF.type, Vocabulary.CatalogRecord);
			addProperty(g, resource, DCTerms.conformsTo, object.getConformsTo());
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
			g.getModel().add(resource, RDF.type, Vocabulary.Distribution);
			addProperty(g, resource, DCTerms.conformsTo, object.getConformsTo());
			addProperty(g, resource, DCTerms.description, object.getDescription());
			addProperty(g, resource, DCTerms.title, object.getTitle());
			addProperty(g, resource, DCTerms.issued, object.getIssued());
			addProperty(g, resource, DCTerms.modified, object.getModified());
			addAllURLs(g, resource, Vocabulary.accessURL, object.getAccessURL());
			addAll(g, resource, Vocabulary.accessService, object.getAccessService());
			addAllURLs(g, resource, Vocabulary.downloadURL, object.getDownloadURL());
			addProperty(g, resource, Vocabulary.byteSize, object.getByteSize());
			addProperty(g, resource, Vocabulary.spatialResolutionInMeters, object.getSpatialResolutionInMeters());
			addProperty(g, resource, Vocabulary.temporalResolution, object.getTemporalResolution());
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
			addProperty(g, resource, Vocabulary.startDate, object.getStartDate());
			addProperty(g, resource, Vocabulary.endDate, object.getEndDate());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveRelationship(Graph<? extends Model> g, Resource resource, Relationship object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Relationship);
			addAll(g, resource, DCTerms.relation	, object.getRelation());
			addObject(g, resource, Vocabulary.hadRole, object.getHadRole());
			g.commit();
		} finally {
			g.end();
		}
	}

	protected void saveRole(Graph<? extends Model> g, Resource resource, Role object) {
		g.beginWrite();
		try {
			saveDCATObject(g, resource, object);
			g.getModel().add(resource, RDF.type, Vocabulary.Role);
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
			if (!StringUtils.isBlank(object.getName())) {
				g.getModel().add(resource, Vocabulary.name, object.getName());
			}
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
			for (Agent member : object.getMember()) {
				Resource m = saveDetails(g, member);
				g.getModel().add(resource, Vocabulary.member, m);
			}
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

	protected void deleteDetails(Graph<? extends Model> g, Resource resource, RDFObject object, boolean purge) {

	}

	@Override
	protected @NotNull String getPrefix() {
		return DCAT;
	}

	@Override
	protected <U extends RDFObject> U getObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource) {
		// TODO Auto-generated method stub
		return null;
	}

}
