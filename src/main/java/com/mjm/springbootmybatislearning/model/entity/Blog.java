package com.mjm.springbootmybatislearning.model.entity;

import lombok.Builder;
import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@Alias("blog")
public class Blog {

    private Integer id;

    private String content;

    private String title;

    private Integer authorId;

    private Author author;

}