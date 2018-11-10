package com.mjm.springbootmybatislearning.model.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * Created by majunmin on 2018/11/9.
 */
@Data
@Alias("blogAuthor")
public class BlogAuthor {
    private Integer id;

    private String content;

    private String title;

    private Integer authorId;

    private Author author;
}
