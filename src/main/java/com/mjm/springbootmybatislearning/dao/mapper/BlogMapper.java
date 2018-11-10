package com.mjm.springbootmybatislearning.dao.mapper;

import com.mjm.springbootmybatislearning.model.entity.Blog;
import com.mjm.springbootmybatislearning.model.entity.BlogAuthor;

import java.util.List;

public interface BlogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Blog record);

    int insertSelective(Blog record);

    Blog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Blog record);

    int updateByPrimaryKey(Blog record);

    List<BlogAuthor> selectBlogAuthor();
}