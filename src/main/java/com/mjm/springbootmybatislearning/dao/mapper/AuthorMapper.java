package com.mjm.springbootmybatislearning.dao.mapper;

import com.mjm.springbootmybatislearning.model.entity.Author;

public interface AuthorMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Author record);

    int insertSelective(Author record);

    Author selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Author record);

    int updateByPrimaryKey(Author record);
}