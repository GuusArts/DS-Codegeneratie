package nl.kik.commons.dcat2.dto;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

public class CatalogTest {
    @Test
    public void testCatalogFields() {
        Catalog catalog = new Catalog();
        catalog.setId("cat1");
        catalog.setTitle("Test Catalog");
        catalog.setDescription("A test catalog");
        Dataset dataset = new Dataset();
        catalog.setDatasets(Collections.singletonList(dataset));

        assertEquals("cat1", catalog.getId());
        assertEquals("Test Catalog", catalog.getTitle());
        assertEquals("A test catalog", catalog.getDescription());
        assertEquals(1, catalog.getDatasets().size());
    }
} 