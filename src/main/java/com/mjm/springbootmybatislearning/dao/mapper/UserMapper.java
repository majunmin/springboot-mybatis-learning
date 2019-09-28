package com.mjm.springbootmybatislearning.dao.mapper;

import com.mjm.springbootmybatislearning.model.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

/**
 * @Author majunmin
 *
 * #p0 第一个参数
 */
@CacheConfig(cacheNames = "user")
public interface UserMapper {

    /**
     * 删除缓存
     * @param id
     * @return
     */
    @CacheEvict(key = "#p0")
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    /**
     * 先从缓存中（redis）查询
     * 若未查到 ： 从数据库查询
     * @param id
     * @return
     */
//    @Cacheable(key = "#p0")
    User selectByPrimaryKey(Integer id);

    /**
     * 更新缓存
     * @param record
     * @return
     */
    @CachePut(key = "#p0.id")
    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

//    @CachePut(key = "#p0")
    int updateAgeById(@Param("id") Integer userId, @Param("age") Integer age);
}