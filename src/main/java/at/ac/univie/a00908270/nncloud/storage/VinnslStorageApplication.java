package at.ac.univie.a00908270.nncloud.storage;

import at.ac.univie.a00908270.nncloud.storage.data.StorageProperties;
import at.ac.univie.a00908270.nncloud.storage.data.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
}
