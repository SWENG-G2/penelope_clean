package sweng.penelope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Springfox configuration for swagger-ui.
 */
@Configuration
public class SpringFoxConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30)
                // Only select our controllers
                .select().apis(RequestHandlerSelectors.basePackage("sweng.penelope.controllers"))
                .paths(PathSelectors.any()).build();
    }
}