package nl.kik.commons.dcat2.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.kik.commons.dcat2.service.DCAT2Service;
import nl.kik.commons.dcat2.service.DCAT2SerializationService;

/**
 * Spring auto-configuration for DCAT2 services.
 * This class automatically configures the DCAT2 services when the module is included.
 */
@Configuration
public class DCAT2AutoConfiguration {

    /**
     * Creates the DCAT2Service bean if not already defined.
     * 
     * @return A new DCAT2Service instance
     */
    @Bean
    @ConditionalOnMissingBean
    public DCAT2Service dcat2Service() {
        return new DCAT2Service();
    }
    
    /**
     * Creates the DCAT2SerializationService bean if not already defined.
     * 
     * @return A new DCAT2SerializationService instance
     */
    @Bean
    @ConditionalOnMissingBean
    public DCAT2SerializationService dcat2SerializationService() {
        return new DCAT2SerializationService();
    }
}
