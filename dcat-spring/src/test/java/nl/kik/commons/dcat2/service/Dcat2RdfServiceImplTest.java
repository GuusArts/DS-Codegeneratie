package nl.kik.commons.dcat2.service;

import nl.kik.commons.dcat2.dto.Catalog;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Dcat2RdfServiceImplTest {
    @Test
    public void testToRdfReturnsModel() {
        Dcat2RdfService service = new Dcat2RdfServiceImpl();
        Catalog catalog = new Catalog();
        Model model = service.toRdf(catalog);
        assertNotNull(model);
    }

    @Test
    public void testFromRdfReturnsCatalog() {
        Dcat2RdfService service = new Dcat2RdfServiceImpl();
        Model model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        Catalog catalog = service.fromRdf(model);
        assertNotNull(catalog);
    }
} 