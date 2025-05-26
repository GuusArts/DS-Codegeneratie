package nl.kik.commons.dcat2.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DistributionTest {
    @Test
    public void testDistributionFields() {
        Distribution distribution = new Distribution();
        distribution.setId("dist1");
        distribution.setTitle("Test Distribution");
        distribution.setAccessURL("http://example.com");

        assertEquals("dist1", distribution.getId());
        assertEquals("Test Distribution", distribution.getTitle());
        assertEquals("http://example.com", distribution.getAccessURL());
    }
} 