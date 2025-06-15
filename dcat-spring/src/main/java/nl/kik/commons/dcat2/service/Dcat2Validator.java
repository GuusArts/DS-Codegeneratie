package nl.kik.commons.dcat2.service;

import nl.kik.commons.dcat2.dto.Catalog;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;

public class Dcat2Validator {
    // In a real implementation, load SHACL shapes from a file or resource
    private static final String SHACL_SHAPES = """
        @prefix sh: <http://www.w3.org/ns/shacl#> .
        @prefix dcat: <http://www.w3.org/ns/dcat#> .
        @prefix dct: <http://purl.org/dc/terms/> .
        @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
        
        # Example: Catalog must have a title
        [] a sh:NodeShape ;
           sh:targetClass dcat:Catalog ;
           sh:property [
               sh:path dct:title ;
               sh:minCount 1 ;
           ] .
    """;

    public boolean validate(Catalog catalog) {
        Model dataModel = new Dcat2RdfServiceImpl().toRdf(catalog);
        Model shapesModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        org.apache.jena.riot.RDFDataMgr.read(shapesModel, new java.io.StringReader(SHACL_SHAPES), null, org.apache.jena.riot.Lang.TTL);
        ValidationReport report = ShaclValidator.get().validate(shapesModel.getGraph(), dataModel.getGraph());
        return report.conforms();
    }
} 