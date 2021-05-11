package nl.kik.datastation.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.kik.datastation.mvc.RequestMessageConverter;
import nl.kik.datastation.mvc.ResponseMessageConverter;
import nl.kik.datastation.mvc.VerifiableCredentialMessageConverter;

@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
public class WebConfig {
	@Bean
	@ConditionalOnMissingBean
	public VerifiableCredentialMessageConverter verifiableCredentialMessageConverter() {
		return new VerifiableCredentialMessageConverter();
	}

	@Bean
	@ConditionalOnMissingBean
	public RequestMessageConverter requestMessageConverter() {
		return new RequestMessageConverter();
	}

	@Bean
	@ConditionalOnMissingBean
	public ResponseMessageConverter responseMessageConverter() {
		return new ResponseMessageConverter();
	}
}
