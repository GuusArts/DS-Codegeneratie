package nl.kik.commons.dcat.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import nl.kik.commons.dcat.service.DCAT2Service;
import nl.kik.commons.dcat.service.DCAT2ServiceImpl;

/**
 * Auto-configuration for DCAT2 module.
 */
@Configuration
@ComponentScan(basePackages = "nl.kik.commons.dcat")
public class DCAT2AutoConfiguration {

    /**
     * Creates a DCAT2Service bean if one doesn't already exist.
     * 
     * @return The DCAT2Service bean
     */
    @Bean
    @ConditionalOnMissingBean
    public DCAT2Service dcat2Service() {
        return new DCAT2ServiceImpl();
    }
}