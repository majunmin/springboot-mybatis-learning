package com.mjm.springbootmybatislearning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "com.mjm.springbootmybatislearning.dao.mapper")
public class SpringbootMybatisLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootMybatisLearningApplication.class, args);
    }
}
