package com.mjm.springbootmybatislearning;

import com.mjm.springbootmybatislearning.dao.mapper.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;

public class OriginMybatis {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void before(){

        /**
         * 原生 Mybatis加载
         */
        try {
            InputStream inputStream = Resources.getResourceAsStream("mybatisConfig.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void contextLoads() {

        SqlSession sqlSession = sqlSessionFactory.openSession();
        try{
            UserMapper userMapper =  sqlSession.getMapper(UserMapper.class);
            System.out.println(userMapper.selectByPrimaryKey(1));
        } catch (Exception ex){
            ex.printStackTrace();
        }finally {
            sqlSession.close();
        }

    }

}
