package com.rent1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger API文档配置
 *
 * 访问地址：http://localhost:8080/api/swagger-ui/index.html
 *
 * 注意：当前使用Springfox 3.0.0，与Spring Boot 2.7+路径匹配策略不兼容。
 * 如需要API文档，建议迁移至springdoc-openapi。
 */
@Configuration
//@EnableOpenApi  // 临时禁用：与Spring Boot 2.7+冲突
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.rent1.api"))
                .paths(PathSelectors.any())
                .build()
                .enable(false);  // 禁用Swagger
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("房东租赁管理系统 API文档")
                .description("房东租赁管理系统V1.0接口文档")
                .contact(new Contact("Rent1 Team", "", ""))
                .version("1.0.0")
                .build();
    }
}