# @MapperScan 注解原理

> 在springBoot 项目中 经常使用 @MapperScan 注解， 指定(basePackages),扫描Mapper接口类
> 或者通过 @Mapper 注解标记 Mapper 接口类， 其实这两种方式扫描配置用的是同一个地方， 只是扫描入口不同。

- @MapperScan 是根据 其注解上 @Import(MapperScannerRegistrar.class) 进行自动装配的， 最终调用的自动装配与 下面一致
- @MapperScan 自动装配的入口 是 MybatisAutoConfiguration 的内部类 MapperScannerRegistrarNotFoundConfiguration, (位于 spring-boot-start 下 mybatis-spring-boot-autoconfigure)
```java
   /*
   * {@link org.mybatis.spring.annotation.MapperScan} ultimately ends up
   * creating instances of {@link MapperFactoryBean}. If
   * {@link org.mybatis.spring.annotation.MapperScan} is used then this
   * auto-configuration is not needed. If it is _not_ used, however, then this
   * will bring in a bean registrar and automatically register components based
   * on the same component-scanning path as Spring Boot itself.
   */
  @org.springframework.context.annotation.Configuration
  //通过Import 引入配置 
  @Import({ AutoConfiguredMapperScannerRegistrar.class })
  @ConditionalOnMissingBean(MapperFactoryBean.class)
  public static class MapperScannerRegistrarNotFoundConfiguration {

    @PostConstruct
    public void afterPropertiesSet() {
      logger.debug("No {} found.", MapperFactoryBean.class.getName());
    }
  }
```

MapperScannerRegistrarNotFoundConfiguration 的代码逻辑是： 如果标记了 @MapperScan , 将会生成 MapperFactoryBean，
如果不存在 MapperFactoryBean 这个类实例， 也就是没 @MapperScan , 此时就会走 @Import 里面的配置， 下面看看 `AutoConfiguredMapperScannerRegistrar` 的配置

`AutoConfiguredMapperScannerRegistrar` 是 `MybatisAutoConfiguration` 的一个内部类

```java
  /**
   * This will just scan the same base package as Spring Boot does. If you want
   * more power, you can explicitly use
   * {@link org.mybatis.spring.annotation.MapperScan} but this will get typed
   * mappers working correctly, out-of-the-box, similar to using Spring Data JPA
   * repositories.
   */
  public static class AutoConfiguredMapperScannerRegistrar
      implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private BeanFactory beanFactory;

    private ResourceLoader resourceLoader;
    /**
     * importingClassMetadata: 是引入 AutoConfiguredMapperScannerRegistrar 注解的类的元信息
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

      logger.debug("Searching for mappers annotated with @Mapper");
      // 根据注册服务， 初始化 路径Mapper扫描器
      ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);

      try {
        if (this.resourceLoader != null) {
          scanner.setResourceLoader(this.resourceLoader);
        }
        
        // 获取扫描路径
        List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
        if (logger.isDebugEnabled()) {
          for (String pkg : packages) {
            logger.debug("Using auto-configuration base package '{}'", pkg);
          }
        }
        
        // 设置要扫描的类， 此处是标记了 @Mapper 的接口
        scanner.setAnnotationClass(Mapper.class);
        // 注册过滤器
        scanner.registerFilters();
        // =====> 开始扫描 注册bean <======
        scanner.doScan(StringUtils.toStringArray(packages));
      } catch (IllegalStateException ex) {
        logger.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.", ex);
      }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
      this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
      this.resourceLoader = resourceLoader;
    }
  }
```
上面的代码逻辑基本就是初始化 ClassPathMapperScanner 扫描器， 这扫描器继承了spring 的 ClassPathBeanDefinitionScanner, 主要作用就是扫描 @Mapper 接口并注册为spring Bean，
首先看看核心方法 doScan(), @MapperScan 注解的自动装配也是用了这个注解。

```java
   // ClassPathMapperScanner
   /**
   * Calls the parent search that will search and register all the candidates.
   * Then the registered objects are post processed to set them as
   * MapperFactoryBeans
   */
  public Set<BeanDefinitionHolder> doScan(String... basePackages) {
    // 扫描出 basePackage 包下所有标记了 @Mapper 的 BeanDefinition
    Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
    // 如果 BeanDefinition 不为空， 就进行处理
    if (beanDefinitions.isEmpty()) {
      logger.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
    } else {
      processBeanDefinitions(beanDefinitions);
    }

    return beanDefinitions;
  }
```

doScan() 主要就是调用了父类ClassPathBeanDefinitionScanner 的 doScan(),获取所有的类定义， 之后执行自定义逻辑  ClassPathMapperScanner#processBeanDefinitions

```java
// ClassPathMapperScanner
private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
    GenericBeanDefinition definition;
    for (BeanDefinitionHolder holder : beanDefinitions) {
      // 这个地方操作的是 BeanDefinition 的引用
      definition = (GenericBeanDefinition) holder.getBeanDefinition();

      if (logger.isDebugEnabled()) {
        logger.debug("Creating MapperFactoryBean with name '" + holder.getBeanName() 
          + "' and '" + definition.getBeanClassName() + "' mapperInterface");
      }

      // the mapper interface is the original class of the bean
      // but, the actual class of the bean is MapperFactoryBean
      // 设置构造器参数为 definition.getBeanClassName()， 通过构造器注入设置 MapperFactoryBean 的 mapperInterface 字段
      definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); // issue #59
      // 设置 beanClass = MapperFactroyBean.class, 这个 MapperFactroyBean 尤为重要， 当注入这个 BeanDefinition 的时候， 
      // 实际调用的是 MapperFactroyBean.getObject() 获取的 Mapper 接口的代理, 这个代理的具体的类是上面  definition.getBeanClassName() 设置的
      definition.setBeanClass(this.mapperFactoryBean.getClass());

      definition.getPropertyValues().add("addToConfig", this.addToConfig);

      boolean explicitFactoryUsed = false;
      if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
        definition.getPropertyValues().add("sqlSessionFactory", new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
        explicitFactoryUsed = true;
      } else if (this.sqlSessionFactory != null) {
        definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
        explicitFactoryUsed = true;
      }

      if (StringUtils.hasText(this.sqlSessionTemplateBeanName)) {
        if (explicitFactoryUsed) {
          logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
        }
        definition.getPropertyValues().add("sqlSessionTemplate", new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
        explicitFactoryUsed = true;
      } else if (this.sqlSessionTemplate != null) {
        if (explicitFactoryUsed) {
          logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
        }
        definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
        explicitFactoryUsed = true;
      }

      if (!explicitFactoryUsed) {
        if (logger.isDebugEnabled()) {
          logger.debug("Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName() + "'.");
        }
        // 设置自动装配模式 = 按类型装配
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
      }
    }
  }
```

*上面的代码主要做的事情就是重新扫描出`BeanDefinition`,设置构造器参数,这里构造器参数就是标注了`@Mapper`的接口*

> **总结**: 重写 BeanDefinition 的 beanClass = MapperFactoryBean.class, 并且将 beanClass 也就是MapperFactoryBean 的构造器参数设置为 标注了 @Mapper 的接口，
之后当 Mapper 接口注入的时候，实际调用的是 MapperFactoryBean 中 getObject() 获取的特定 Mapper 实例

如下是 MapperFactoryBean 的核心逻辑， mapperInterface 字段是通过上面的 
`definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());`设置的

```java
public class MapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {

  private Class<T> mapperInterface;

  private boolean addToConfig = true;

  public MapperFactoryBean() {
    //intentionally empty 
  }
  
  public MapperFactoryBean(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getObject() throws Exception {
    return getSqlSession().getMapper(this.mapperInterface);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<T> getObjectType() {
    return this.mapperInterface;
  }


  /**
   * Sets the mapper interface of the MyBatis mapper
   *
   * @param mapperInterface class of the interface
   */
  public void setMapperInterface(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  /**
   * Return the mapper interface of the MyBatis mapper
   * @return class of the interface
   */
  public Class<T> getMapperInterface() {
    return mapperInterface;
  }
}
```

MapperFactoryBean 实现了 FactoryBean 接口,以及继承了 SqlSessionDaoSupport .
继承 SqlSessionDaoSupport 的主要目的是为了获取 SqlSession, 通过sqlSession 获得具体的 mapper 代理类。


## 总结
**综上**， 首先根据 @MapperScan 获取 basePackages 或者根据 @Mapper 获取所在的 packages, 之后通过 ClassPathMapperScan 扫描包， 获取所有的的 Mapper 接口类的 BeanDefinition，
之后具体配置， 设置 beanClass = MapperFactoryBean, 设置 MapperFactoryBean 的构造器器参数为 实际的Mapper 接口类，通过 ClassPathBeanDefinitionScanner 父类进行 bean注册，自动注入的时候，
机会调用 MapperFactoryBean#getObject() 获取实际的调用类型。




