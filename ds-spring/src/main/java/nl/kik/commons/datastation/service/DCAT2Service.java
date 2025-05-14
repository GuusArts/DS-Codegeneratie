package nl.kik.commons.datastation.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import nl.kik.commons.datastation.dto.dcat2.Catalog2;
import nl.kik.commons.datastation.dto.dcat2.DCAT2Object;
import nl.kik.commons.datastation.dto.dcat2.DataService2;
import nl.kik.commons.datastation.dto.dcat2.Dataset2;
import nl.kik.commons.datastation.dto.dcat2.DatasetSeries;
import nl.kik.commons.datastation.dto.dcat2.Distribution2;
import nl.kik.commons.datastation.dto.dcat2.Relationship2;
import nl.kik.commons.datastation.dto.dcat2.ResourceTimeEvolution;
import nl.kik.commons.dto.RDFObject;

/**
 * Service for working with DCAT2 objects.
 * Extends the existing DCATService to add support for DCAT2-specific classes and properties.
 */
@Service
public class DCAT2Service extends DCATService {

    /**
     * DCAT2 vocabulary extension.
     */
    public static class Vocabulary extends DCATService.Vocabulary {
        private static final Model m = ModelFactory.createDefaultModel();
        
        public static final Resource DatasetSeries = Vocabulary.m.createResource(DCAT.getURI() + "DatasetSeries");
        public static final Property inSeries = Vocabulary.m.createProperty(DCAT.getURI() + "inSeries");
        public static final Property seriesMember = Vocabulary.m.createProperty(DCAT.getURI() + "seriesMember");
        
        public static final Property hasVersion = DCTerms.hasVersion;
        public static final Property isVersionOf = DCTerms.isVersionOf;
        public static final Property previousVersion = Vocabulary.m.createProperty(DCAT.getURI() + "previousVersion");
        public static final Property nextVersion = Vocabulary.m.createProperty(DCAT.getURI() + "nextVersion");
        public static final Property versionInfo = Vocabulary.m.createProperty(DCAT.getURI() + "versionInfo");
        public static final Property versionNotes = Vocabulary.m.createProperty(DCAT.getURI() + "versionNotes");
        
        public static final Property hasTarget = Vocabulary.m.createProperty(DCAT.getURI() + "hasTarget");
        public static final Property citation = Vocabulary.m.createProperty(DCAT.getURI() + "citation");
        
        public static final Property compressionFormat = Vocabulary.m.createProperty(DCAT.getURI() + "compressionFormat");
        public static final Property packageFormat = Vocabulary.m.createProperty(DCAT.getURI() + "packageFormat");
        
        public static final Property created = DCTerms.created;
        public static final Property modified = DCTerms.modified;
        public static final Property issued = DCTerms.issued;
        public static final Property deprecated = Vocabulary.m.createProperty(DCAT.getURI() + "deprecated");
        public static final Property withdrawn = Vocabulary.m.createProperty(DCAT.getURI() + "withdrawn");
    }
    
    @Override
    protected Map<Resource, Class<? extends RDFObject>> getObjectTypes() {
        final Map<Resource, Class<? extends RDFObject>> result = new HashMap<>(super.getObjectTypes());
        result.put(Vocabulary.DatasetSeries, DatasetSeries.class);
        return result;
    }
}
