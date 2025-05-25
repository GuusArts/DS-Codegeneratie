package nl.kik.commons.dcat2.autoconfig;

import nl.kik.commons.dcat2.service.Dcat2RdfService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Dcat2AutoConfigurationTest {
    @Test
    public void testDcat2RdfServiceBeanCreation() {
        Dcat2AutoConfiguration config = new Dcat2AutoConfiguration();
        Dcat2RdfService service = config.dcat2RdfService();
        assertNotNull(service);
    }
} 