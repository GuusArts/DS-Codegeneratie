package nl.kik.commons.dcat2.dto;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

public class DatasetTest {
    @Test
    public void testDatasetFields() {
        Dataset dataset = new Dataset();
        dataset.setId("ds1");
        dataset.setTitle("Test Dataset");
        dataset.setDescription("A test dataset");
        Distribution distribution = new Distribution();
        dataset.setDistributions(Collections.singletonList(distribution));

        assertEquals("ds1", dataset.getId());
        assertEquals("Test Dataset", dataset.getTitle());
        assertEquals("A test dataset", dataset.getDescription());
        assertEquals(1, dataset.getDistributions().size());
    }
} 