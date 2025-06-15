package nl.kik.commons.dcat2.service;

import nl.kik.commons.dcat2.dto.Catalog;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Dcat2ValidatorTest {
    @Test
    public void testValidCatalog() {
        Catalog catalog = new Catalog();
        catalog.setId("urn:test");
        catalog.setTitle("Valid Catalog");
        Dcat2Validator validator = new Dcat2Validator();
        assertTrue(validator.validate(catalog));
    }

    @Test
    public void testInvalidCatalog() {
        Catalog catalog = new Catalog();
        catalog.setId("urn:test");
        // No title set
        Dcat2Validator validator = new Dcat2Validator();
        assertFalse(validator.validate(catalog));
    }
} 