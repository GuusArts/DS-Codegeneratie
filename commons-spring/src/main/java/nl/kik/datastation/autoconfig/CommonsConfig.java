package nl.kik.datastation.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.kik.datastation.service.CatalogService;
import nl.kik.datastation.service.DefaultKeyService;
import nl.kik.datastation.service.KeyService;
import nl.kik.datastation.service.MessageService;
import nl.kik.datastation.service.ResultService;
import nl.kik.datastation.service.SPARQLService;
import nl.kik.datastation.service.VerifiableCredentialService;

@Configuration
public class CommonsConfig {
	@Bean
	@ConditionalOnMissingBean
	public CatalogService catalogServce() {
		return new CatalogService();
	}

	@Bean
	@ConditionalOnMissingBean
	public MessageService messageService() {
		return new MessageService();
	}

	@Bean
	@ConditionalOnMissingBean
	public VerifiableCredentialService verifiableCredentialService() {
		return new VerifiableCredentialService();
	}

	@Bean
	@ConditionalOnMissingBean
	public KeyService keyService() {
		return new DefaultKeyService();
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
}
