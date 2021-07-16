package com.kamtar.transport.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.collect.Lists.newArrayList;

@Configuration
@EnableAsync
@EnableSwagger2
public class Swagger implements WebMvcConfigurer {
    @Bean
    public Docket apiDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build().apiInfo(metaData())
                .globalResponseMessage(RequestMethod.GET, newArrayList(
                        new ResponseMessageBuilder()
                                .code(500).message("Erreur serveur")
                                .responseModel(new ModelRef("string")).build(),
                        new ResponseMessageBuilder()
                                .code(403)
                                .message("Erreur d'autorisation")
                                .build(),
                        new ResponseMessageBuilder()
                                .code(401)
                                .message("Erreur d'autorisation")
                                .build(),
                        new ResponseMessageBuilder()
                                .code(404)
                                .message("Ressource non trouvée")
                                .build())

                );
       }
       private ApiInfo metaData() {
           ApiInfo apiInfo = new ApiInfo(
                   "kamtar-transport REST API",
                   "Ce microservice s'occupe des API de Kamtar Transport",
                   "1.0",
                   "Terms of service",
                   "Webcaruel",
                  "Apache License Version 2.0",
                   "https://www.apache.org/licenses/LICENSE-2.0");
           return apiInfo;

       }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/v2/api-docs", "/v2/api-docs");
        registry.addRedirectViewController("/swagger-resources/configuration/ui", "/swagger-resources/configuration/ui");
        registry.addRedirectViewController("/swagger-resources/configuration/security", "/swagger-resources/configuration/security");
        registry.addRedirectViewController("/swagger-resources", "/swagger-resources");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html**").addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}

