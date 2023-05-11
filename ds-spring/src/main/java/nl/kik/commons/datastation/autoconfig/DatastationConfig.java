package nl.kik.commons.datastation.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.kik.commons.datastation.service.CatalogService;
import nl.kik.commons.datastation.service.CryptoService;
import nl.kik.commons.datastation.service.NoopCryptoService;
import nl.kik.commons.datastation.service.ResultService;
import nl.kik.commons.datastation.service.SPARQLService;

@Configuration
public class DatastationConfig {
	@Bean
	@ConditionalOnMissingBean
	CatalogService catalogServce() {
		return new CatalogService();
	}

	@Bean
	@ConditionalOnMissingBean
	ResultService resultService() {
		return new ResultService();
	}

	@Bean
	@ConditionalOnMissingBean
	SPARQLService sparqLService() {
		return new SPARQLService();
	}

	@Bean
	@ConditionalOnMissingBean
	CryptoService validationService() {
		return new NoopCryptoService();
	}

}
