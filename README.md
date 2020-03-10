# springlink-mybatis
基于MyBatis的ORM框架

## Spring JavaConfig 配置
```java
@Bean
public SqlRegistry sqlRegistry(SqlSessionFactory sqlSessionFactory) throws IOException, ClassNotFoundException {
  SqlRegistry registry = new SqlRegistry(sqlSessionFactory.getConfiguration(), SqlDialect.get("mysql"));
  registry.addPackage("com.github.springlink.example.entity", getClass().getClassLoader());
  return registry;
}
@Bean
public SqlDao sqlDao(SqlSessionFactory sqlSessionFactory, SqlRegistry registry) {
  return new DefaultSqlDao(registry, new SqlSessionTemplate(sqlSessionFactory))
}
```

## 实体类注解
- 在实体类上使用<code>@SqlEntity</code>注解，以便在<code>SqlRegistry</code>进行包扫描时发现这个实体类
  - <code>value</code>：数据库表名
  - <code>schema</code>：数据库Schema
  - <code>catalog</code>： 数据库Catalog
  - <code>nameStrategy</code>：名称转换策略，默认为下划线转驼峰
- 在实体类上使用<code>@SqlCache</code>注解，相当于为该实体类配置<code>&lt;cache&gt;</code>，注解参数与<code>&lt;cache&gt;</code>完全一致，这里不再详细描述
- 在实体类上使用<code>@SqlCacheRef</code>注解，相当于为该实体类配置 <code>&lt;cache-ref&gt;</code>
  - <code>value</code>：指定与哪个实体类共享缓存，不能与<code>namespace</code>同时指定
  - <code>namespace</code>：指定与哪个命名空间共享缓存，不能与<code>value</code>同时指定
- 在实体类字段上使用<code>@SqlProperty</code>注解，为字段配置不同属性
  - <code>aliases</code>：字段别名，可通过<code>#alias</code>的形式引用该字段，在不同实体上引用功能相同但字段名不同的属性时非常有用，默认为空
  - <code>column</code>：数据库列名，默认采取<code>@SqlEntity</code>的<code>nameStrategy</code>策略自动计算
  - <code>jdbcType</code>：MyBatis JDBCType，默认为<code>UNDEFINED</code>
  - <code>typeHandler</code>：MyBatis TypeHandler，默认为<code>UnknownTypeHandler</code>
  - <code>reference</code>：字段引用，设置后意味着该字段引用另一个字段的值，自身并非具体的数据库字段，常用于表连接的情况下，默认为空
  - <code>id</code>：是否为ID字段，加上此注解并不意味着该字段为数据库主键，而是用于MyBatis对于缓存的优化，官方文档描述为“一个 ID 结果；标记出作为 ID 的结果可以帮助提高整体性能”，默认为<code>false</code>
  - <code>generated</code>：是否由数据库生成值，配置为true相当于MyBatis的<code>&lt;insert useGeneratedKeys&gt;</code>配置，默认为<code>false</code>
- 在实体类字段上使用<code>@SqlIgnore</code>注解，标记该字段不出现在生成的SQL语句中，与JPA 的 <code>@Trasient</code> 注解类似
- 在<code>static final</code>字段上使用<code>@SqlJoin</code>注解，声明一条实体连接，字段值为相应的连接条件，在该实体的SELECT操作时，会自动生成相应的表连接语句
  - <code>value</code>：指定与哪个实体连接
  - <code>name</code>：为该实体连接命名，默认为空，直接使用注解字段的名称
  - <code>type</code>：指定连接类型，支持Inner、Left Outer、Right Outer和Full Outer，默认为Left Outer

## SqlDao