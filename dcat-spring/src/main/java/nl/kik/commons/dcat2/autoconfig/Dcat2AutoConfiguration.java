package nl.kik.commons.dcat2.autoconfig;

import nl.kik.commons.dcat2.service.Dcat2RdfService;
import nl.kik.commons.dcat2.service.Dcat2RdfServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Dcat2AutoConfiguration {
    @Bean
    public Dcat2RdfService dcat2RdfService() {
        return new Dcat2RdfServiceImpl();
    }
} 