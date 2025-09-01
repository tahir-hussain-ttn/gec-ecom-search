package com.husain707.reactive.growth.ecom.search.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.husain707.reactive.growth.ecom.search.api",
        "com.husain707.reactive.growth.ecom.search.common"
})
@EnableReactiveElasticsearchRepositories
public class EcomSearchApiApplication {
    public static void main(String[] args){
        SpringApplication.run(EcomSearchApiApplication.class, args);
    }
}
