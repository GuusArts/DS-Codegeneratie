package nl.kik.commons.datastation.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import nl.kik.commons.dto.RDFObject;

/**
 * Utility interface for serializing and deserializing DCAT2 objects.
 * This interface separates the API from the implementation which is provided
 * in the ds-spring module to maintain proper dependency isolation.
 */
public interface Dcat2Serializer {

    /**
     * Serialize a collection of RDF objects to an output stream in RDF/XML format
     * 
     * @param objects The objects to serialize
     * @param outputStream The output stream to write to
     */
    void serializeToRdfXml(Collection<? extends RDFObject> objects, OutputStream outputStream);
    
    /**
     * Serialize a collection of RDF objects to an output stream in Turtle format
     * 
     * @param objects The objects to serialize
     * @param outputStream The output stream to write to
     */
    void serializeToTurtle(Collection<? extends RDFObject> objects, OutputStream outputStream);
    
    /**
     * Serialize a collection of RDF objects to an output stream in JSON-LD format
     * 
     * @param objects The objects to serialize
     * @param outputStream The output stream to write to
     */
    void serializeToJsonLd(Collection<? extends RDFObject> objects, OutputStream outputStream);
    
    /**
     * Serialize a collection of RDF objects to a string in JSON-LD format
     * 
     * @param objects The objects to serialize
     * @return The JSON-LD string
     */
    default String toJsonLd(Collection<? extends RDFObject> objects) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializeToJsonLd(objects, baos);
        return baos.toString(StandardCharsets.UTF_8);
    }
    
    /**
     * Serialize a collection of RDF objects to a string in Turtle format
     * 
     * @param objects The objects to serialize
     * @return The Turtle string
     */
    default String toTurtle(Collection<? extends RDFObject> objects) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializeToTurtle(objects, baos);
        return baos.toString(StandardCharsets.UTF_8);
    }
    
    /**
     * Deserialize RDF objects from an input stream in RDF/XML format
     * 
     * @param inputStream The input stream to read from
     * @return A collection of deserialized RDF objects
     */
    Collection<RDFObject> deserializeFromRdfXml(InputStream inputStream);
    
    /**
     * Deserialize RDF objects from an input stream in Turtle format
     * 
     * @param inputStream The input stream to read from
     * @return A collection of deserialized RDF objects
     */
    Collection<RDFObject> deserializeFromTurtle(InputStream inputStream);
    
    /**
     * Deserialize RDF objects from an input stream in JSON-LD format
     * 
     * @param inputStream The input stream to read from
     * @return A collection of deserialized RDF objects
     */
    Collection<RDFObject> deserializeFromJsonLd(InputStream inputStream);
}
