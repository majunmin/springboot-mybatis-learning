# MapperFactoryBean#getObject

> mybatis-spring 中， 需要对各个 Mapper 接口编写实现类， 实现类方法中一般都包含 SqlSessionTemplate 的相应方法。
> mybatis-spring 根据 Mapper 接口实现代理类,从而使coder 更加简洁

1. 配置

```xml
<bean id="userMapper" class="org.mybatis.spring.mapper.MapperFactoryBean" >
      <property name="mapperInterface" value="name.liux.share.dao.UserDao" />
      <property name="sqlSessionFactory" ref="sqlSessionFactory" />
</bean>

```

或者通过自动扫描的方式， 将 Mapper 接口类的 beanClass 设置为 MapperFactoryBean

每个 Mapper接口 都需要配置一个 MapperFactoryBean 接口,需注入两个参数: 一个是被代理的接口`mapperInterface`, 一个是 `sqlSessionFactory`

## MapperFactoryBean 的初始化

1. 入参 mapperInterface
  mapper接口声明,用于生成代理类（JDK动态代理）
2. 入参 sqlSessionFactory
  用于初始化 `SqlSessionDaoSupport`(也可以配置 `sqlSessionTemplate`), 配置`sqlSessionFactory`最终也是生成`sqlSessionTemplate`
3. 初始化回调
  SqlSessionDaoSupport 继承自 `DaoSupport` , `DaoSupport` 实现了 `InitializingBean` , `MapperFactoryBean` 初始化回调方法为 `DaoSupport#afterPropertiesSet`
  

### checkDaoConfig

上面说到 MapperFactoryBean 初始化回调方法为 `DaoSupport#afterPropertiesSet`,从源码（略）中可看出，`MapperFactoryBean` 重写了父类的回调方法 `checkDaoConfig` ,源码如下:

```java
  /**
   * {@inheritDoc}
   */
  @Override
  protected void checkDaoConfig() {
    super.checkDaoConfig();

    notNull(this.mapperInterface, "Property 'mapperInterface' is required");
    // 获取配置对象 
    Configuration configuration = getSqlSession().getConfiguration();
    // 判断当前 mapper接口 是否在当前 configuration 中注册(mapperRegistry)
    if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
      try {
        //当前mapper声明未在Configuration中注册，则执行注册逻辑
        configuration.addMapper(this.mapperInterface);
      } catch (Exception e) {
        logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", e);
        throw new IllegalArgumentException(e);
      } finally {
        ErrorContext.instance().reset();
      }
    }
  }
```

### 通过 getObject() 生成代理类

代理工厂类MapperFactoryBean生成代理类的源码如下:
```java
  /**
   * {@inheritDoc}
   */
  @Override
  public T getObject() throws Exception {
    // getSqlSession() 返回的是 sqlSessionTemplate
    return getSqlSession().getMapper(this.mapperInterface);
  }
```

#### 通过 getMapper() 生成代理类工厂

通过源码可以看出最终调用的 Configuration 内部的一个注册器类 MapperRegistry的方法,  MapperRegistry#getMaper() 
```java
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    // 获取代理类工厂类
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      // 获取代理类实例
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
```