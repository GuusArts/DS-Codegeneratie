package nl.kik.commons.dcat2.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.dcat2.dto.Catalog;
import nl.kik.commons.dcat2.dto.CatalogRecord;
import nl.kik.commons.dcat2.dto.DataService;
import nl.kik.commons.dcat2.dto.Dataset;
import nl.kik.commons.dcat2.dto.Distribution;

/**
 * Service for serializing DCAT2 objects to different RDF formats.
 * Supports various output formats including JSON-LD, Turtle, RDF/XML, etc.
 */
@Service
@Slf4j
public class DCAT2SerializationService {
    
    @Autowired
    private DCAT2Service dcat2Service;
    
    /**
     * Serializes a DCAT2 catalog to the specified output stream in JSON-LD format.
     * 
     * @param catalog The catalog to serialize
     * @param outputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void serializeCatalogToJSONLD(Catalog catalog, OutputStream outputStream) throws IOException {
        Model model = dcat2Service.convertCatalogToRDF(catalog);
        RDFDataMgr.write(outputStream, model, RDFFormat.JSONLD);
    }
    
    /**
     * Serializes a DCAT2 catalog to the specified output stream in Turtle format.
     * 
     * @param catalog The catalog to serialize
     * @param outputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void serializeCatalogToTurtle(Catalog catalog, OutputStream outputStream) throws IOException {
        Model model = dcat2Service.convertCatalogToRDF(catalog);
        RDFDataMgr.write(outputStream, model, RDFFormat.TURTLE);
    }
    
    /**
     * Serializes a DCAT2 catalog to the specified output stream in RDF/XML format.
     * 
     * @param catalog The catalog to serialize
     * @param outputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void serializeCatalogToRDFXML(Catalog catalog, OutputStream outputStream) throws IOException {
        Model model = dcat2Service.convertCatalogToRDF(catalog);
        RDFDataMgr.write(outputStream, model, RDFFormat.RDFXML);
    }
    
    /**
     * Serializes a DCAT2 dataset to the specified output stream in JSON-LD format.
     * 
     * @param dataset The dataset to serialize
     * @param outputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void serializeDatasetToJSONLD(Dataset dataset, OutputStream outputStream) throws IOException {
        Model model = dcat2Service.convertDatasetToRDF(dataset);
        RDFDataMgr.write(outputStream, model, RDFFormat.JSONLD);
    }
    
    /**
     * Serializes a DCAT2 dataset to the specified output stream in Turtle format.
     * 
     * @param dataset The dataset to serialize
     * @param outputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void serializeDatasetToTurtle(Dataset dataset, OutputStream outputStream) throws IOException {
        Model model = dcat2Service.convertDatasetToRDF(dataset);
        RDFDataMgr.write(outputStream, model, RDFFormat.TURTLE);
    }
    
    /**
     * Serializes a DCAT2 dataset to the specified output stream in RDF/XML format.
     * 
     * @param dataset The dataset to serialize
     * @param outputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void serializeDatasetToRDFXML(Dataset dataset, OutputStream outputStream) throws IOException {
        Model model = dcat2Service.convertDatasetToRDF(dataset);
        RDFDataMgr.write(outputStream, model, RDFFormat.RDFXML);
    }
    
    /**
     * Serializes a DCAT2 object to the specified output stream in the requested format.
     * 
     * @param object The DCAT2 object to serialize
     * @param outputStream The output stream to write to
     * @param format The RDF format to use
     * @throws IOException If an I/O error occurs
     */
    public void serialize(Object object, OutputStream outputStream, RDFFormat format) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        
        if (object instanceof Catalog) {
            model = dcat2Service.convertCatalogToRDF((Catalog) object);
        } else if (object instanceof Dataset) {
            model = dcat2Service.convertDatasetToRDF((Dataset) object);
        } else if (object instanceof Distribution) {
            model = dcat2Service.convertDistributionToRDF((Distribution) object);
        } else if (object instanceof DataService) {
            model = dcat2Service.convertDataServiceToRDF((DataService) object);
        } else if (object instanceof CatalogRecord) {
            model = dcat2Service.convertCatalogRecordToRDF((CatalogRecord) object);
        } else {
            throw new IllegalArgumentException("Unsupported DCAT2 object type: " + object.getClass().getName());
        }
        
        RDFDataMgr.write(outputStream, model, format);
    }
    
    /**
     * Generate a unique URI for a DCAT2 object.
     * 
     * @param baseUri The base URI to use
     * @param type The type of object
     * @return A unique URI
     */
    public String generateDCATUri(String baseUri, String type) {
        return baseUri + type.toLowerCase() + "/" + UUID.randomUUID().toString();
    }
}
