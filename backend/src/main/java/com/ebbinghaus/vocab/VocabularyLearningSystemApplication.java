package com.ebbinghaus.vocab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.ebbinghaus.vocab.mapper")
@SpringBootApplication
public class VocabularyLearningSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocabularyLearningSystemApplication.class, args);
    }
}
