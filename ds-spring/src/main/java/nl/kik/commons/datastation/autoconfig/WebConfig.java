package nl.kik.commons.datastation.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.kik.commons.datastation.mvc.RequestMessageConverter;
import nl.kik.commons.datastation.mvc.ResponseMessageConverter;
import nl.kik.commons.datastation.mvc.VerifiableCredentialMessageConverter;
import nl.kik.commons.datastation.service.KeyService;
import nl.kik.commons.datastation.service.MessageService;
import nl.kik.commons.datastation.service.ResultService;
import nl.kik.commons.datastation.service.ValidationService;
import nl.kik.commons.datastation.service.VerifiableCredentialService;

@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
public class WebConfig {
	@Bean
	@ConditionalOnMissingBean
	public RequestMessageConverter requestMessageConverter(final MessageService service,
			final VerifiableCredentialService vcService, final KeyService keys, final ValidationService validator) {
		return new RequestMessageConverter(service, vcService, keys, validator);
	}

	@Bean
	@ConditionalOnMissingBean
	public ResponseMessageConverter responseMessageConverter(final MessageService service,
			final ResultService resultService, final KeyService keys, final ValidationService validator) {
		return new ResponseMessageConverter(service, resultService, keys, validator);
	}

	@Bean
	@ConditionalOnMissingBean
	public VerifiableCredentialMessageConverter verifiableCredentialMessageConverter(
			final VerifiableCredentialService service, final KeyService keys, final ValidationService validator) {
		return new VerifiableCredentialMessageConverter(service, keys, validator);
	}

}
