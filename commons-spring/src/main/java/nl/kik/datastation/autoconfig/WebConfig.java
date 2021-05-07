package nl.kik.datastation.autoconfig;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import nl.kik.datastation.dto.vc.VerifiableBase;
import nl.kik.datastation.mvc.MessageMessageConverter;
import nl.kik.datastation.mvc.VerifiableCredentialMessageConverter;

@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(WebMvcConfigurer.class)
public class WebConfig {
	@Bean
	@ConditionalOnMissingBean
	public VerifiableCredentialMessageConverter verifiableCredentialMessageConverter() {
		return new VerifiableCredentialMessageConverter();
	}

	@Bean
	@ConditionalOnMissingBean
	public <T extends VerifiableBase> MessageMessageConverter<T> messageMessageConverter() {
		return new MessageMessageConverter<T>();
	}

	@Configuration
	public static class InjectMessageConverters implements WebMvcConfigurer {
		@Autowired
		private VerifiableCredentialMessageConverter verifiableCredentialMessageConverter;
		@Autowired
		private MessageMessageConverter<?> messageMessageConverter;

		@Override
		public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
			converters.add(messageMessageConverter);
			converters.add(verifiableCredentialMessageConverter);
		}

	}
}
