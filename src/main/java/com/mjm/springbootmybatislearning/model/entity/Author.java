package com.mjm.springbootmybatislearning.model.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

@Data
@Alias("author")
public class Author implements Serializable {
    private Integer id;

    private String authorName;

    private Integer authorAge;

    private Date createTime;
}