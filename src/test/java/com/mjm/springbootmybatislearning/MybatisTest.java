package com.mjm.springbootmybatislearning;

import com.mjm.springbootmybatislearning.dao.mapper.AuthorMapper;
import com.mjm.springbootmybatislearning.dao.mapper.BlogMapper;
import com.mjm.springbootmybatislearning.dao.mapper.UserMapper;
import com.mjm.springbootmybatislearning.model.entity.Author;
import com.mjm.springbootmybatislearning.model.entity.Blog;
import com.mjm.springbootmybatislearning.model.entity.BlogAuthor;
import com.mjm.springbootmybatislearning.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by majunmin on 2018/11/7.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MybatisTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthorMapper authorMapper;

    @Autowired
    private BlogMapper blogMapper;

    private SqlSessionFactory sqlSessionFactory = null;

    @Before
    public void before(){
        sqlSessionFactory  = (SqlSessionFactory)applicationContext.getBean("sqlSessionFactory");
    }

    @Test
    public void contextLoads() {

        SqlSession sqlSession = sqlSessionFactory.openSession();
        try{
            UserMapper userMapper =  sqlSession.getMapper(UserMapper.class);
            System.out.println(userMapper.selectByPrimaryKey(1));
        } catch (Exception ex){
            log.error("MybatisTest.contextLoads, ", ex.getStackTrace());
        }finally {
            sqlSession.close();
        }

    }


    @Test
    public void testMapper(){
        User user = userMapper.selectByPrimaryKey(1);
        System.out.println(user);
    }

    @Test
    public void createAuthor(){


        IntStream.range(1,20).forEach(i -> {
            Author author = new Author();
            author.setAuthorName("mjm" + i);
            author.setAuthorAge(20 + i);
            author.setCreateTime(new Date(System.currentTimeMillis()));
            authorMapper.insertSelective(author);

            Blog blog = new Blog();
            blog.setAuthorId(author.getId());
            blog.setTitle("Title"+ i);
            blog.setContent("Content"+ i + " : hello world!");
            blogMapper.insertSelective(blog);
        });

    }

    /**
     * 延迟加载 测试
     *
     * 延迟加载 对 association 和 collections 有效
     *
     * 修改mybatis 配置文件 打开mbatis延迟加载开关
     *
     * <settings>
     * 	    <!-- 打开延迟加载的开关 -->
     * 		<setting name="lazyLoadingEnabled" value="true" />
     * 		<!-- 将积极加载改为消息加载即按需加载 -->
     * 		<setting name="aggressiveLazyLoading" value="false"/>
     * 	</settings>
     *
     * 	2. 若不适用 association collections 如何实现延迟加载
     *     用编程的方式
     * 	    定义两个mapper，
     *
     *
     *
     */
    @Test
    public void selectBlogs(){
        List<BlogAuthor> blogs = blogMapper.selectBlogAuthor();
        blogs.forEach(blog -> System.out.println(blog.getAuthor()));

    }


    /**
     * 一级缓存 默认开启
     *
     */
    @Test
    public void levelOneCache(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        try{
            /**
             * 查看 sql语句print result, 查询sql语句 只会执行一次
             */
            Author author = sqlSession
                    .selectOne("com.mjm.springbootmybatislearning.dao.mapper.AuthorMapper.selectByPrimaryKey", 1);
            Author author1 = sqlSession
                    .selectOne("com.mjm.springbootmybatislearning.dao.mapper.AuthorMapper.selectByPrimaryKey", 1);

            System.out.println(author);
            System.out.println(author1);
        }catch(Exception ex){
            ex.printStackTrace();
        }finally {
            sqlSession.close();
        }

    }


    /**
     *  开启mybatis二级缓存： 1、设置mybatis.xml，也就是说mybatis默认二级缓存是关闭的。
     *                         <setting name="cacheEnabled" value="true" />
     *
     *                     2、设置mapper。在mapper.xml内添加标签：<cache/>
     *                          <cache />
     *                     3、pojo实现接口Serializable。实现该接口后也就说明二级缓存不仅可以存入内存中，还可以存入磁盘。
     *                          pojo implements Serializable
     *
     *                     4. mybatis自身实现二级缓存弊端在于只能作用于数据库，此时需要我们引用第三方库作为缓存库(redis memcache)，这样缓存更具有扩展性
     *
     * **注意**
     * 开启缓存的弊端是数据没有实时性，
     * 当数据库中的数据一旦修改，查询的数据还是缓存中的数据没有实时性，
     * 对于某些需要实时性显示数据的接口我们可以设置useCache="false",设置该属性后，
     * 该接口每次查询出来都是去执行sql查询出实时性数据。
     */
    @Test
    public void levelTwoCache(){
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        SqlSession sqlSession2 = sqlSessionFactory.openSession();

        try{
            /**
             * 查看 sql语句print result, 查询sql语句 只会执行一次
             */
            Author author = sqlSession1
                    .selectOne("com.mjm.springbootmybatislearning.dao.mapper.AuthorMapper.selectByPrimaryKey", 1);
            System.out.println(author);

            /**
             * sqlSession1 关闭之后 查询结果才会被缓存，否则 sqlSession2 还是 命中不到缓存
             */
            sqlSession1.close();

            Author author1 = sqlSession2
                    .selectOne("com.mjm.springbootmybatislearning.dao.mapper.AuthorMapper.selectByPrimaryKey", 1);

            System.out.println(author1);

            sqlSession2.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }finally {
//            sqlSession1.close();
//            sqlSession2.close();
        }
    }

    /**
     * Mybatis redis cache
     *
     * 启用缓存 spring @EnableCache
     *
     * @CacheEvict 一般注解到删除数据的操作上，会将一条或多条数据从缓存中删除。
     * @CachePut 是将数据加入到redis缓存中
     * @Cacheable 在获取数据的时候会先查询缓存，如果缓存中存在，则不执行查询数据库的方法，如果不存在则查询数据库，并加入到缓存中。
     *     Cacheable  根据查询的参数决定是否查询数据库
     * @CacheConfig  作用于类上,指定cacheNames，则后面的方法的注解value可省略且value值与类注解的value相同。
     */
    @Test
    public void redisCache(){
        User user1 = userMapper.selectByPrimaryKey(1);
        User user2 = userMapper.selectByPrimaryKey(1);

        System.out.println(user2);
        System.out.println(user1);

        user1.setUserName("mjm");
        int res = userMapper.updateByPrimaryKeySelective(user1);

        user1.setAge(77);
        int res2 = userMapper.updateByPrimaryKeySelective(user1);

        System.out.println("res" + res + " " + "res2" + res2);

    }
}
