package nl.kik.commons.datastation.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nl.kik.commons.datastation.service.DCAT2Service;
import nl.kik.commons.dto.RDFObject;

/**
 * Implementation of the DCAT2Serializer interface.
 * This class delegates to DCAT2Service for serialization and deserialization.
 */
@Component
public class DCAT2SerializerImpl implements DCAT2Serializer {
    
    private final DCAT2Service dcat2Service;
    
    @Autowired
    public DCAT2SerializerImpl(DCAT2Service dcat2Service) {
        this.dcat2Service = dcat2Service;
    }

    @Override
    public void serializeToRdfXml(Collection<? extends RDFObject> objects, OutputStream outputStream) {
        dcat2Service.serialize(objects, outputStream, RDFFormat.RDFXML);
    }

    @Override
    public void serializeToTurtle(Collection<? extends RDFObject> objects, OutputStream outputStream) {
        dcat2Service.serialize(objects, outputStream, RDFFormat.TURTLE);
    }

    @Override
    public void serializeToJsonLd(Collection<? extends RDFObject> objects, OutputStream outputStream) {
        dcat2Service.serialize(objects, outputStream, RDFFormat.JSONLD);
    }

    @Override
    public Collection<RDFObject> deserializeFromRdfXml(InputStream inputStream) {
        return dcat2Service.deserialize(inputStream, Lang.RDFXML);
    }

    @Override
    public Collection<RDFObject> deserializeFromTurtle(InputStream inputStream) {
        return dcat2Service.deserialize(inputStream, Lang.TURTLE);
    }

    @Override
    public Collection<RDFObject> deserializeFromJsonLd(InputStream inputStream) {
        return dcat2Service.deserialize(inputStream, Lang.JSONLD);
    }
}