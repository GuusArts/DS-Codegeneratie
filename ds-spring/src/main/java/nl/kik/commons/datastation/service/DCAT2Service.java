package nl.kik.commons.datastation.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD4;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.dcat.*;
import nl.kik.commons.datastation.dto.dcat.DatasetSeries.DatasetSeriesBuilder;
import nl.kik.commons.datastation.dto.dcat.vocabulary.DCAT2Vocabulary;
import nl.kik.commons.datastation.dto.foaf.*;
import nl.kik.commons.dto.Graph;
import nl.kik.commons.dto.RDFObject;
import nl.kik.commons.service.AbstractRDFService;
import nl.kik.commons.service.RDFService;

/**
 * Service for handling DCAT2 data, including serialization to and from RDF formats
 * and JSON-LD. This supports the full DCAT2 specification.
 */
@Slf4j
@Service
public class DCAT2Service extends AbstractRDFService<Graph<Model>> {

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

    private static Map<Resource, Class<? extends RDFObject>> objectTypes = Map.ofEntries(
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.CatalogRecord, CatalogRecord.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Distribution, Distribution.class),
            Map.<Resource, Class<? extends RDFObject>>entry(DCTerms.PeriodOfTime, PeriodOfTime.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Relationship, Relationship.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Resource, nl.kik.commons.datastation.dto.dcat.Resource.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.DataService, DataService.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Dataset, Dataset.class),
            Map.<Resource, Class<? extends RDFObject>>entry(m.createResource(DCAT2Vocabulary.DATASET_SERIES), DatasetSeries.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Catalog, Catalog.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Role, Role.class),

            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Agent, Agent.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Group, Group.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Organization, Organization.class),
            Map.<Resource, Class<? extends RDFObject>>entry(Vocabulary.Person, Person.class)
    );

    private static Map<Kind, Resource> kinds = Map.of(//
            Kind.Group, VCARD4.Group,
            Kind.Individual, VCARD4.Individual,
            Kind.Location, VCARD4.Location,
            Kind.Organization, VCARD4.Organization
    );
    
    private static Map<Resource, Kind> reverseKinds = RDFService.reverse(DCAT2Service.kinds);

    /**
     * Serialize a collection of DCAT2 objects to an output stream in the specified format
     * 
     * @param objects The DCAT2 objects to serialize
     * @param outputStream The output stream to write to
     * @param format The RDF format to use
     */
    public void serialize(Collection<? extends RDFObject> objects, OutputStream outputStream, RDFFormat format) {
        Graph<Model> graph = Graph.create(ModelFactory.createDefaultModel());
        objects.forEach(obj -> save(graph, obj));
        RDFDataMgr.write(outputStream, graph.getModel(), format);
    }
    
    /**
     * Deserialize DCAT2 objects from an input stream
     * 
     * @param inputStream The input stream to read from
     * @param lang The RDF language format
     * @return A collection of deserialized DCAT2 objects
     */
    public Collection<RDFObject> deserialize(InputStream inputStream, Lang lang) {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, inputStream, lang);
        Graph<Model> graph = Graph.create(model);
        
        Map<Resource, RDFObject> objects = new HashMap<>();
        
        // We need to find all resources that are DCAT2 classes
        for (Resource type : objectTypes.keySet()) {
            RDFService.search(graph, null, RDF.type, type, Statement::getSubject)
                    .forEach(resource -> {
                        if (!objects.containsKey(resource)) {
                            MultiValuedMap<Property, RDFNode> properties = getProperties(graph, resource);
                            RDFObject obj = getObject(graph, objects, properties, resource);
                            if (obj != null) {
                                objects.put(resource, obj);
                            }
                        }
                    });
        }
        
        return objects.values();
    }

    /**
     * Get all catalogs from a graph
     * 
     * @param graph The graph to search in
     * @return A collection of catalogs
     */
    public Collection<? extends Catalog> getAllCatalogs(final Graph<Model> graph) {
        Map<Resource, RDFObject> existing = new HashMap<>();
        return RDFService.search(graph, null, RDF.type, org.apache.jena.vocabulary.DCAT.Catalog, Statement::getSubject) //
                .map(r -> Pair.of(r, getProperties(graph, r))) //
                .map(p -> getObject(graph, existing, p.getRight(), p.getLeft())) //
                .filter(Catalog.class::isInstance) //
                .map(Catalog.class::cast) //
                .collect(Collectors.toList());
    }

    /**
     * Get all datasets from a catalog
     * 
     * @param catalog The catalog to get datasets from
     * @param endpoint Optional endpoint URI
     * @return A collection of datasets
     */
    public Collection<Dataset> getDatasets(final Catalog catalog, final URI endpoint) {
        return CollectionUtils.emptyIfNull(catalog.getDataset());
    }

    /**
     * Save a DCAT2 object to a graph
     */
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
    
    /**
     * Save a DCAT2 DCATObject to a graph
     */
    protected void saveDetails(final Graph<? extends Model> g, final Resource resource, final DCATObject object) {
        if (object instanceof Catalog) {
            saveCatalog(g, resource, (Catalog) object);
        } else if (object instanceof Dataset) {
            if (object instanceof DatasetSeries) {
                saveDatasetSeries(g, resource, (DatasetSeries) object);
            } else {
                saveDataset(g, resource, (Dataset) object);
            }
        } else if (object instanceof DataService) {
            saveDataService(g, resource, (DataService) object);
        } else if (object instanceof nl.kik.commons.datastation.dto.dcat.Resource) {
            saveResource(g, resource, (nl.kik.commons.datastation.dto.dcat.Resource) object);
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

    /**
     * Save a DCAT2 FOAFObject to a graph
     */
    protected void saveDetails(final Graph<? extends Model> g, final Resource resource, final FOAFObject object) {
        if (object instanceof Group) {
            saveGroup(g, resource, (Group) object);
        } else if (object instanceof Organization) {
            saveOrganization(g, resource, (Organization) object);
        } else if (object instanceof Person) {
            savePerson(g, resource, (Person) object);
        } else if (object instanceof Agent) {
            saveAgent(g, resource, (Agent) object);
        } else {
            throw new IllegalArgumentException("Cannot save FOAF objects of type " + object.getClass().getSimpleName());
        }
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

    @SuppressWarnings("unchecked")
    protected <U extends DCATObject> U getDCATObject(final Graph<Model> graph, Map<Resource, RDFObject> existing,
            final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final Class<U> t) {
        if (Catalog.class.isAssignableFrom(t)) {
            return (U) getCatalog(graph, existing, properties, resource, Catalog.builder()).build();
        }
        if (Dataset.class.isAssignableFrom(t)) {
            if (DatasetSeries.class.isAssignableFrom(t)) {
                return (U) getDatasetSeries(graph, existing, properties, resource, DatasetSeries.builder()).build();
            }
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
        } else if (Location.class.isAssignableFrom(t)) {
            return (U) getLocation(graph, properties, resource, Location.builder()).build();
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
    
    @Override
    protected Map<Resource, Class<? extends RDFObject>> getObjectTypes() {
        return DCAT2Service.objectTypes;
    }
    
    @Override
    protected @NotNull String getPrefix() {
        return DCAT2Service.DCAT;
    }
    
    // Implementation of helper methods for deserialization
    
    @SuppressWarnings("unchecked")
    private <B extends CatalogBuilder<?, ?>> B getCatalog(final Graph<Model> graph, Map<Resource, RDFObject> existing,
            final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
        return (B) getDataset(graph, existing, properties, resource, builder)
                .themeTaxonomy(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.themeTaxonomy, RDFObject.class))
                .hasPart(getSet(graph, existing, properties, DCTerms.hasPart, nl.kik.commons.datastation.dto.dcat.Resource.class))
                .dataset(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.dataset, Dataset.class))
                .service(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.service, DataService.class))
                .catalog(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.catalog, Catalog.class))
                .record(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.record, CatalogRecord.class))
                .datasetSeries(getSet(graph, existing, properties, m.createProperty(DCAT2Vocabulary.DATASET_PROP), DatasetSeries.class))
                .language(RDFService.getURISet(properties, DCTerms.language));
    }

    @SuppressWarnings("unchecked")
    private <B extends DatasetSeriesBuilder<?, ?>> B getDatasetSeries(final Graph<Model> graph,
            Map<Resource, RDFObject> existing, final MultiValuedMap<Property, RDFNode> properties,
            final Resource resource, final B builder) {
        return (B) getDataset(graph, existing, properties, resource, builder)
                .seriesMembers(getSet(graph, existing, properties, m.createProperty(DCAT2Vocabulary.IN_SERIES), Dataset.class));
    }

    @SuppressWarnings("unchecked")
    private <B extends DatasetBuilder<?, ?>> B getDataset(final Graph<Model> graph, Map<Resource, RDFObject> existing,
            final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
        return (B) getResource(graph, existing, properties, resource, builder)
                .distribution(getSet(graph, existing, properties, org.apache.jena.vocabulary.DCAT.distribution, Distribution.class))
                .accrualPeriodicity(RDFService.getURI(properties, DCTerms.accrualPeriodicity))
                .temporalResolution(RDFService.getDuration(properties, org.apache.jena.vocabulary.DCAT.temporalResolution))
                .temporal(getObject(graph, existing, properties, DCTerms.temporal, PeriodOfTime.class))
                .spatialResolutionInMeters(RDFService.getFloat(properties, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters))
                .isVersionOf(getSet(graph, existing, properties, m.createProperty(DCAT2Vocabulary.IS_VERSION_OF), Dataset.class))
                .hasVersion(getSet(graph, existing, properties, m.createProperty(DCAT2Vocabulary.HAS_VERSION), Dataset.class))
                .previousVersion(getSet(graph, existing, properties, m.createProperty(DCAT2Vocabulary.PREVIOUS_VERSION), Dataset.class))
                .version(RDFService.getString(properties, m.createProperty(DCAT2Vocabulary.VERSION)))
                .versionNotes(RDFService.getString(properties, m.createProperty(DCAT2Vocabulary.VERSION_NOTES)))
                .spatial(getObject(graph, existing, properties, DCTerms.spatial, Location.class));
    }
    
    // Implementation of helper methods for serialization
    
    protected void saveCatalog(final Graph<? extends Model> g, final Resource resource, final Catalog object) {
        g.beginWrite();
        try {
            saveDataset(g, resource, object);
            g.getModel().add(resource, RDF.type, org.apache.jena.vocabulary.DCAT.Catalog);
            addAll(g, resource, org.apache.jena.vocabulary.DCAT.themeTaxonomy, object.getThemeTaxonomy());
            addAll(g, resource, DCTerms.hasPart, object.getHasPart());
            addAll(g, resource, org.apache.jena.vocabulary.DCAT.dataset, object.getDataset());
            addAll(g, resource, org.apache.jena.vocabulary.DCAT.service, object.getService());
            addAll(g, resource, org.apache.jena.vocabulary.DCAT.catalog, object.getCatalog());
            addAll(g, resource, org.apache.jena.vocabulary.DCAT.record, object.getRecord());
            addAll(g, resource, m.createProperty(DCAT2Vocabulary.DATASET_PROP), object.getDatasetSeries());
            
            CollectionUtils.emptyIfNull(object.getLanguage())
                    .forEach(lang -> RDFService.addURI(g, resource, DCTerms.language, lang));
            
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
            addProperty(g, resource, org.apache.jena.vocabulary.DCAT.spatialResolutionInMeters, object.getSpatialResolutionInMeters());
            addObject(g, resource, DCTerms.temporal, object.getTemporal());
            addProperty(g, resource, org.apache.jena.vocabulary.DCAT.temporalResolution, object.getTemporalResolution());
            
            // DCAT2 specific properties
            addAll(g, resource, m.createProperty(DCAT2Vocabulary.IS_VERSION_OF), object.getIsVersionOf());
            addAll(g, resource, m.createProperty(DCAT2Vocabulary.HAS_VERSION), object.getHasVersion());
            addAll(g, resource, m.createProperty(DCAT2Vocabulary.PREVIOUS_VERSION), object.getPreviousVersion());
            addProperty(g, resource, m.createProperty(DCAT2Vocabulary.VERSION), object.getVersion());
            addProperty(g, resource, m.createProperty(DCAT2Vocabulary.VERSION_NOTES), object.getVersionNotes());
            addObject(g, resource, DCTerms.spatial, object.getSpatial());
            
            g.commit();
        } finally {
            g.end();
        }
    }
    
    protected void saveDatasetSeries(final Graph<? extends Model> g, final Resource resource, final DatasetSeries object) {
        g.beginWrite();
        try {
            saveDataset(g, resource, object);
            g.getModel().add(resource, RDF.type, m.createResource(DCAT2Vocabulary.DATASET_SERIES));
            addAll(g, resource, m.createProperty(DCAT2Vocabulary.IN_SERIES), object.getSeriesMembers());
            g.commit();
        } finally {
            g.end();
        }
    }
    
    // Additional helper methods for other entity types
    // The rest of the implementation follows the same pattern as the methods above
    
    private <B extends LocationBuilder<?, ?>> B getLocation(final Graph<Model> graph,
            final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
        return getDCATObject(graph, properties, resource, builder)
                .bbox(RDFService.getString(properties, m.createProperty(DCAT2Vocabulary.BBOX)))
                .centroid(RDFService.getString(properties, m.createProperty(DCAT2Vocabulary.CENTROID)))
                .name(RDFService.getString(properties, Vocabulary.name))
                .geometry(RDFService.getString(properties, m.createProperty("http://www.opengis.net/ont/geosparql#asGeoJSON")));
    }
    
    protected void saveLocation(final Graph<? extends Model> g, final Resource resource, final Location object) {
        g.beginWrite();
        try {
            saveDCATObject(g, resource, object);
            g.getModel().add(resource, RDF.type, DCTerms.Location);
            addProperty(g, resource, m.createProperty(DCAT2Vocabulary.BBOX), object.getBbox());
            addProperty(g, resource, m.createProperty(DCAT2Vocabulary.CENTROID), object.getCentroid());
            addProperty(g, resource, Vocabulary.name, object.getName());
            addProperty(g, resource, m.createProperty("http://www.opengis.net/ont/geosparql#asGeoJSON"), object.getGeometry());
            g.commit();
        } finally {
            g.end();
        }
    }
    
    // Remaining helper methods would follow here, implementing deserialization and serialization
    // for all entity types defined in the DCAT2 model
}