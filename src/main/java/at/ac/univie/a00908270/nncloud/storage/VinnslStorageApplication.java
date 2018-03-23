package at.ac.univie.a00908270.nncloud.storage;

import at.ac.univie.a00908270.nncloud.storage.data.StorageProperties;
import at.ac.univie.a00908270.nncloud.storage.data.StorageService;
import com.mongodb.MongoClientOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class VinnslStorageApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(VinnslStorageApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			//storageService.deleteAll();
			storageService.init();
			
		};
	}
	
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/*").allowedOrigins("*");
				registry.addMapping("/storage/*").allowedOrigins("*");
			}
		};
	}
	
	@Bean
	public MongoClientOptions mongoOptions() {
		return MongoClientOptions.builder().serverSelectionTimeout(2000).build();
	}
	
	@EnableSwagger2
	public class SwaggerConfig {
		@Bean
		public Docket api() {
			return new Docket(DocumentationType.SWAGGER_2)
					.select()
					.apis(RequestHandlerSelectors.basePackage("at.ac.univie.a00908270.nncloud.storage"))
					.paths(PathSelectors.any())
					.build()
					.apiInfo(apiInfo());
		}
		
		private ApiInfo apiInfo() {
			return new ApiInfo(
					"NN Storage Service",
					"Webservice to store and retrieve files",
					"0.0.1-SNAPSHOT",
					null,
					new Contact("a00908270", "https://a00908270.github.io", "a00908270@unet.univie.ac.at"),
					"MIT", "https://github.com/a00908270/vinnsl-nn-cloud/LICENSE", Collections.emptyList());
		}
	}
}
