package com.lango.juyi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * 类说明 :自定义swagger配置信息
 *
 * @author lango
 */
@Configuration
// 开启 swagger2 的功能
@EnableSwagger2WebMvc
@Profile({"dev","test"})
public class SwaggerConfig {

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select() //选择哪些路径和api会生成document
				// 这里一定要标注你控制器的位置
                .apis(RequestHandlerSelectors.basePackage("com.lango.juyi.controller"))//controller路径
                //.apis(RequestHandlerSelectors.any())   //对所有api进行监控
                .paths(PathSelectors.any())  //对所有路径进行监控
                .build();
    }

    /**
     * api 信息
     * @return
     */
    //接口文档的一些基本信息
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("lango用户中心")//文档主标题
                .description("接口文档")//文档描述
                .termsOfServiceUrl("http://user.lg-sp.cn")
                .contact(new Contact("lango","https://github.com/superLango","2644992002@qq.com"))
                .version("1.0")//API的版本
                .build();
    }
}