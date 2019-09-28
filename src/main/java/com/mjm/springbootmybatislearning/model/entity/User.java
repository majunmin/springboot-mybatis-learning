package com.mjm.springbootmybatislearning.model.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

@Alias("user")
@Data
public class User implements Serializable {

    private Integer id;

    private String userName;

    private Integer age;

    private String password;

    private Double weight;

    private Integer isHigh;

}