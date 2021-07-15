# ClassPathMapperScanner


ClassPathMapperScanner 继承自 ClassPathBeanDefinitionScanner, 会将 包路径下的  类(mapper类)解析为 BeanDefinition ,
并设置 beanClass = MapperFactoryBean(or 自定义的 factoryBean), 这样 每一个 mapper类 就都对应这儿一个 MapperFactoryBean, 
spring容器中创建的其实是 MapperFactoryBean,
然后调用 其 `MapperFactoryBean#getBean()` 可以获取 `MapperProxy` 类, 最终自动注入的 `Mapper类` 也就是这个 `MapperProxy`(代理类, cglib or jdk？)


`MapperFactoryBean#getBean()`
```java
  public T getObject() throws Exception {
    return getSqlSession().getMapper(this.mapperInterface);
  }
```

`SqlSessionTemplate#getMapper`
```java
  public <T> T getMapper(Class<T> type) {
    return getConfiguration().getMapper(type, this);
  }
```

`Configuration#getMapper()`
```java
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
  }
```

`MapperRegistry#getMapper()`
```java
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
```

> 这里 knownMapper 做一个缓存, 缓存 mapper -> MapperProxyFactory, 
> 实际创建代理类的是  `MapperProxyFactory#newInstance()`
> MapperProxy 实现了  InvocationHandler 接口

`MapperProxyFactory`
> 这里使用的是JDK动态代理 (为接口实现代理类)
```java
public class MapperProxyFactory<T> {

  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

  public MapperProxyFactory(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  public Class<T> getMapperInterface() {
    return mapperInterface;
  }

  public Map<Method, MapperMethod> getMethodCache() {
    return methodCache;
  }

  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }

}

```


## MapperFactoryBean


<img src="https://gitee.com/niubenwsl/image_repo/raw/master/image/java/MapperFactoryBean.png" style="zoom:50%;" />

```java
	public final void afterPropertiesSet() throws IllegalArgumentException, BeanInitializationException {
		// Let abstract subclasses check their configuration.
		checkDaoConfig();

		// Let concrete implementations initialize themselves.
		try {
			initDao();
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Initialization of DAO failed", ex);
		}
	}
```
MapperFactoryBean 继承了  DaoSupport,  DaoSupport#afterPropertiesSet (实现了 InitializingBean), 当 bean 初始化时回调

### 1. checkDaoConfig

`MapperFactoryBean#checkConfig()`
```java
  protected void checkDaoConfig() {
    super.checkDaoConfig();

    notNull(this.mapperInterface, "Property 'mapperInterface' is required");

    Configuration configuration = getSqlSession().getConfiguration();
    if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
      try {
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

`Configuration#addMapper()`
```java
  public <T> void addMapper(Class<T> type) {
    mapperRegistry.addMapper(type);
  }
```

`MapperRegistry#addMapper()`

> knownMapper 就是在这里添加的(mapper -> MapperProxy)
> 


```java
  public <T> void addMapper(Class<T> type) {
    if (type.isInterface()) {
      if (hasMapper(type)) {
        throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
      }
      boolean loadCompleted = false;
      try {
        knownMappers.put(type, new MapperProxyFactory<T>(type));
        // It's important that the type is added before the parser is run
        // otherwise the binding may automatically be attempted by the
        // mapper parser. If the type is already known, it won't try.
        MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
        parser.parse();
        loadCompleted = true;
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type);
        }
      }
    }
  }
```







