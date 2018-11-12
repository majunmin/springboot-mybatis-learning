package com.mjm.springbootmybatislearning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * springboot已经集成了redis缓存，只需要在pom.xml中加载redis，然后通过注解即可完成配置。
 */
@EnableCaching
@SpringBootApplication
@MapperScan(value = "com.mjm.springbootmybatislearning.dao.mapper")
public class SpringbootMybatisLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootMybatisLearningApplication.class, args);
    }
}
