package com.mjm.springbootmybatislearning.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Author majunmin
 */
@Configuration
public class RedisConfig {


   /**
    * redisTemplate 相关配置
    * @param connectionFactory
    * @return
    *
    * 1. spring-redis中使用了RedisTemplate来进行redis的操作，通过泛型的K和V设置键值对的对象类型。这里使用了string作为key的对象类型，值为Object。
    *
    * 2. 对于Object，spring-redis默认使用了jdk自带的序列化，不推荐使用默认了。所以使用了json的序列化方式
    *
    * 3. 对spring-redis对redis的五种数据类型也有支持
    *     HashOperations： 对hash类型的数据操作
    *     ValueOperations：对redis字符串类型数据操作
    *     ListOperations： 对链表类型的数据操作
    *     SetOperations：  对无序集合类型的数据操作
    *     ZSetOperations： 对有序集合类型的数据操作
    */

   @Bean
   public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
      RedisTemplate<Object, Object> template = new RedisTemplate<>();
      //配置连接工厂
      template.setConnectionFactory(connectionFactory);
      // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
      Jackson2JsonRedisSerializer jacksonSeial = new Jackson2JsonRedisSerializer(Object.class);
 
      ObjectMapper mapper = new ObjectMapper();
      // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
      mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
      //指定序列化输入类型， 类型必须是非 final
      //final修饰的类 String Integer  会抛出异常
      mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
      jacksonSeial.setObjectMapper(mapper);

      //值采用json序列化
      template.setValueSerializer(jacksonSeial);
      //使用StringRedisSerializer来序列化和反序列化redis的key值
      template.setKeySerializer(new StringRedisSerializer());

      // 设置hash key 和value序列化模式
      template.setHashKeySerializer(new StringRedisSerializer());
      template.setHashValueSerializer(jacksonSeial);
      template.afterPropertiesSet();
      return template;
   }
}