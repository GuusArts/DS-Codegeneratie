package nl.kik.datastation.service;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.springframework.stereotype.Service;

import nl.kik.datastation.dto.Graph;
import nl.kik.datastation.dto.RDFObject;
import nl.kik.datastation.dto.dcat.Catalog;
import nl.kik.datastation.dto.dcat.CatalogRecord;
import nl.kik.datastation.dto.dcat.DCATObject;
import nl.kik.datastation.dto.dcat.DataService;
import nl.kik.datastation.dto.dcat.Dataset;
import nl.kik.datastation.dto.dcat.Distribution;
import nl.kik.datastation.dto.dcat.Location;
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
	}

	public static final String DCAT = "dcat";

	private static Map<Resource, Class<? extends RDFObject>> objectTypes = Map.ofEntries( //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.CatalogRecord, CatalogRecord.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Distribution, Distribution.class), //
			Map.<Resource, Class<? extends RDFObject>>entry(DCTerms.Location, Location.class), //
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

	protected Resource saveDetails(Graph<? extends Model> g, RDFObject object) {
		Resource resource = g.getModel().createResource(object.getId());
		if (object instanceof DCATObject) {
			saveDetails(g, resource, (DCATObject) object);
		} else if (object instanceof FOAFObject) {
			saveDetails(g, resource, (FOAFObject) object);
		} else {
			throw new IllegalArgumentException("Cannot save RDF objects of type " + object.getClass().getSimpleName());
		}
		return resource;
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
		} else if (object instanceof Location) {
			saveLocation(g, resource, (Location) object);
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
		saveDataset(g, resource, object);
	}

	protected void saveDataset(Graph<? extends Model> g, Resource resource, Dataset object) {
		saveResource(g, resource, object);
	}

	protected void saveDataService(Graph<? extends Model> g, Resource resource, DataService object) {
		saveResource(g, resource, object);
	}

	protected void saveResource(Graph<? extends Model> g, Resource resource,
			nl.kik.datastation.dto.dcat.Resource object) {
		saveDCATObject(g, resource, object);
	}

	protected void saveCatalogRecord(Graph<? extends Model> g, Resource resource, CatalogRecord object) {
		saveDCATObject(g, resource, object);
	}

	protected void saveDistribution(Graph<? extends Model> g, Resource resource, Distribution object) {
		saveDCATObject(g, resource, object);
	}

	protected void saveLocation(Graph<? extends Model> g, Resource resource, Location object) {
		saveDCATObject(g, resource, object);
	}

	protected void savePeriodOfTime(Graph<? extends Model> g, Resource resource, PeriodOfTime object) {
		saveDCATObject(g, resource, object);
	}

	protected void saveRelationship(Graph<? extends Model> g, Resource resource, Relationship object) {
		saveDCATObject(g, resource, object);
	}

	protected void saveRole(Graph<? extends Model> g, Resource resource, Role object) {
		saveDCATObject(g, resource, object);
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
		saveFOAFObject(g, resource, object);
	}

	protected void saveGroup(Graph<? extends Model> g, Resource resource, Group object) {
		saveAgent(g, resource, object);
	}

	protected void saveOrganization(Graph<? extends Model> g, Resource resource, Organization object) {
		saveAgent(g, resource, object);
	}

	protected void savePerson(Graph<? extends Model> g, Resource resource, Person object) {
		saveAgent(g, resource, object);
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
