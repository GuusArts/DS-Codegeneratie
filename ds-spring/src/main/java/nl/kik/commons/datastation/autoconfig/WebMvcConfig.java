package nl.kik.commons.datastation.autoconfig;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(WebMvcConfigurer.class)
public class WebMvcConfig {
//	@Configuration
//	public static class InjectMessageConverters implements WebMvcConfigurer {
//		@Autowired
//		private VerifiableCredentialMessageConverter verifiableCredentialMessageConverter;
//		@Autowired
//		private RequestMessageConverter requestMessageConverter;
//		@Autowired
//		private ResponseMessageConverter responseMessageConverter;
//
//		@Override
//		public void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
//			converters.add(requestMessageConverter);
//			converters.add(responseMessageConverter);
//			converters.add(verifiableCredentialMessageConverter);
//		}
//	}
//
//	@Bean
//	@ConditionalOnMissingBean
//	@ConditionalOnBean(RestTemplate.class)
//	public RemoteDatastationService remoteDatastationService(final RestTemplate rest,
//			final ResponseMessageConverter responseConverter, final RequestMessageConverter requestConverter) {
//		return new RemoteDatastationService(rest, responseConverter, requestConverter);
//	}
//
}
