package nl.kik.datastation.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.kik.datastation.mvc.RequestMessageConverter;
import nl.kik.datastation.mvc.ResponseMessageConverter;
import nl.kik.datastation.mvc.VerifiableCredentialMessageConverter;
import nl.kik.datastation.service.KeyService;
import nl.kik.datastation.service.MessageService;
import nl.kik.datastation.service.ResultService;
import nl.kik.datastation.service.ValidationService;
import nl.kik.datastation.service.VerifiableCredentialService;

@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
public class WebConfig {
	@Bean
	@ConditionalOnMissingBean
	public VerifiableCredentialMessageConverter verifiableCredentialMessageConverter(
			VerifiableCredentialService service, KeyService keys, ValidationService validator) {
		return new VerifiableCredentialMessageConverter(service, keys, validator);
	}

	@Bean
	@ConditionalOnMissingBean
	public RequestMessageConverter requestMessageConverter(MessageService service,
			VerifiableCredentialService vcService, KeyService keys, ValidationService validator) {
		return new RequestMessageConverter(service, vcService, keys, validator);
	}

	@Bean
	@ConditionalOnMissingBean
	public ResponseMessageConverter responseMessageConverter(MessageService service, ResultService resultService,
			KeyService keys, ValidationService validator) {
		return new ResponseMessageConverter(service, resultService, keys, validator);
	}
	
}
