package com.mjm.springbootmybatislearning.service;

import com.mjm.springbootmybatislearning.model.entity.Blog;

/**
 *
 * @author majunmin
 * @description
 * @datetime 2019-01-16 17:53
 * @since
 */
public interface UserService {


    /**
     * 发布博客
     * @param blog
     * @param userId
     */
    void releaseBlog(Blog blog, Integer userId) throws Exception;
}