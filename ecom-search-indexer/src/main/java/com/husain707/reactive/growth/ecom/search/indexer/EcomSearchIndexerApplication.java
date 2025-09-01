package com.husain707.reactive.growth.ecom.search.indexer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.husain707.reactive.growth.ecom.search.indexer",
        "com.husain707.reactive.growth.ecom.search.common"
})
public class EcomSearchIndexerApplication {

    public static void main(String[] args){
        SpringApplication.run(EcomSearchIndexerApplication.class, args);
    }

}
