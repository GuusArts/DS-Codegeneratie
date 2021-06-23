package nl.kik.commons.datastation.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.kik.commons.datastation.service.CatalogService;
import nl.kik.commons.datastation.service.DefaultKeyService;
import nl.kik.commons.datastation.service.DefaultValidationService;
import nl.kik.commons.datastation.service.KeyService;
import nl.kik.commons.datastation.service.MessageService;
import nl.kik.commons.datastation.service.ResultService;
import nl.kik.commons.datastation.service.SPARQLService;
import nl.kik.commons.datastation.service.ValidationService;
import nl.kik.commons.datastation.service.VerifiableCredentialService;

@Configuration
public class DatastationConfig {
	@Bean
	@ConditionalOnMissingBean
	public CatalogService catalogServce() {
		return new CatalogService();
	}

	@Bean
	@ConditionalOnMissingBean
	public KeyService keyService() {
		return new DefaultKeyService();
	}

	@Bean
	@ConditionalOnMissingBean
	public MessageService messageService() {
		return new MessageService();
	}

	@Bean
	@ConditionalOnMissingBean
	public ResultService resultService() {
		return new ResultService();
	}

	@Bean
	@ConditionalOnMissingBean
	public SPARQLService sparqLService() {
		return new SPARQLService();
	}

	@Bean
	@ConditionalOnMissingBean
	public ValidationService vcValidationService() {
		return new DefaultValidationService<>();
	}

	@Bean
	@ConditionalOnMissingBean
	public VerifiableCredentialService verifiableCredentialService() {
		return new VerifiableCredentialService();
	}
}
