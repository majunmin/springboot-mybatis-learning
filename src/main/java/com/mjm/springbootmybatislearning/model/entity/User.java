package com.mjm.springbootmybatislearning.model.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Alias("user")
@Data
public class User {
    private Integer id;

    private String userName;

    private Integer age;

    private String password;

    private Double weight;

}