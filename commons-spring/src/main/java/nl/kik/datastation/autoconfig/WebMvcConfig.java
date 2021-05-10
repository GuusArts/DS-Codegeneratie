package nl.kik.datastation.autoconfig;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import nl.kik.datastation.mvc.RequestMessageConverter;
import nl.kik.datastation.mvc.VerifiableCredentialMessageConverter;

@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(WebMvcConfigurer.class)
public class WebMvcConfig {
	@Configuration
	public static class InjectMessageConverters implements WebMvcConfigurer {
		@Autowired
		private VerifiableCredentialMessageConverter verifiableCredentialMessageConverter;
		@Autowired
		private RequestMessageConverter requestMessageConverter;

		@Override
		public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
			converters.add(requestMessageConverter);
			converters.add(verifiableCredentialMessageConverter);
		}
	}
}
