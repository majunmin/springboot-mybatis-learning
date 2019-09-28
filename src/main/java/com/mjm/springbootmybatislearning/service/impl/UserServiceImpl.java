package com.mjm.springbootmybatislearning.service.impl;

import com.mjm.springbootmybatislearning.dao.mapper.BlogMapper;
import com.mjm.springbootmybatislearning.dao.mapper.UserMapper;
import com.mjm.springbootmybatislearning.model.entity.Blog;
import com.mjm.springbootmybatislearning.model.entity.User;
import com.mjm.springbootmybatislearning.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 用户业务类 </br>
 *
 * @author majunmin
 * @description
 * @datetime 2019-01-16 17:53
 * @since
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private BlogMapper blogMapper;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public void releaseBlog(Blog blog, Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        blogMapper.insertSelective(blog);
        System.out.println("问题呀！！");
        userMapper.updateAgeById(userId, user.getAge() + 1);
    }
}
