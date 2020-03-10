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

## SqlDao 接口
这个接口是框架的核心接口，所有DAO操作都从这个接口发起
### select
调用<code>SqlDao.select(Class&lt;T&gt; entityType)</code>方法获取<code>Selector&lt;T&gt;</code>对象，支持链式调用
Selector接口主要包含以下方法
- <code>Selector&lt;T&gt; where(@Nullable SqlCriterion criterion)</code>
  设置select条件，多次调用只保留最后一次的值
  ```java
  // 查询id为123的Post
  dao.select(Post.class).where(SqlCriterion.eq("id", 123)).asOne();
  
  // Lambda版本
  dao.select(Post.class).where(c -> c.eq(Post::getId, 123));
  ```
- <code>Selector&lt;T&gt; orderBy(@Nullable SqlOrderBy order)</code>
  设置select排序，多次调用只保留最后一次的值
  ```java
  // 根据标题（升序）和创建时间（降序）排序
  dao.select(Post.class).orderBy(SqlOrderBy.create().asc("title").desc("createTime")).asList();

  // Lambda版本
  dao.select(Post.class).orderBy(o -> o.asc(Post::getTitle).desc(Post::getCreateTime)).asList();
  ```
- <code>Selector&lt;T&gt; forUpdate()</code>
  设置是否附带forUpdate
  ```java
  // 查询id为123的Post，并加上FOR UPDATE
  dao.select(Post.class).where(c -> c.eq(Post::getId, 123)).forUpdate().asOne();

  // 可以通过参数指定是否加上FOR UPDATE
  dao.select(Post.class).where(c -> c.eq(Post::getId, 123)).forUpdate(false).asOne();
  ```
- <code>Optional&lt;T&gt; asOne()</code>
  执行查询，返回最多一条结果，与<code>SqlSession.selectOne()</code>执行效果相同
  ```java
  // 查询id为123的Post，如果存在，打印创建时间
  // 这里使用了Java8的Optional接口简化写法，省去了额外的if条件判断
  dao.select(Post.class).where(c -> c.eq(Post::getId, 123)).asOne()
      .ifPresent(post -> System.out.println(post.getCreateTime()));
  ```
- <code>&lt;R&gt; Optional&lt;R&gt; asOne(SqlProjections projections)</code>
  执行查询，返回一个或多个字段
  ```java
  // 查询id为123的Post，打印createTime字段
  dao.select(Post.class).where(c -> c.eq(Post::getId, 123))
      .asOne(SqlProjections.create().property("createTime")))
      .ifPresent(createTime -> System.out.println(createTime));
  
  // 查询id为123的Post，返回title和createTime字段
  dao.select(Post.class).where(c -> c.eq(Post::getId, 123))
      .asOne(SqlProjections.create()
          .property("postTitle", "title")
          .property("postCreateTime", "createTime"))
      .ifPresent(post -> System.out.println(post.getTitle() + "\t" + post.getCreateTime()));

  // Lambda写法
  dao.select(Post.class).where(c -> c.eq(Post::getId, 123))
      .asOne(p -> p.property(Post::getCreateTime))
      .ifPresent(createTime -> System.out.println(createTime));

  dao.select(Post.class).where(c -> c.eq(Post::getId, 123))
      .asOne(p -> p.property("postTitle", "title")
          .property("postCreateTime", "createTime"))
      .ifPresent(result -> System.out.println(result.get("postTitle") + "\t" + result.get("postCreateTime")));
  ```
- <code>List&lt;T&gt; asList()</code>
  执行查询，返回多条结果
  ```java
  // 查询start大于等于100的Post
  dao.select(Post.class).where(c -> c.ge(Post::getStar, 100)).asList();

  // 查询start大于等于100的Post，并按照LIMIT 100, 50进行分页
  dao.select(Post.class).where(c -> c.ge(Post::getStar, 100))
      .asList(new RowBounds(100, 50));
      
  // 查询start大于等于100的Post，按照star字段降序，返回title和star字段，并按照LIMIT 100, 50进行分页
  dao.select(Post.class).where(c -> c.ge(Post::getStar, 100))
      .orderBy(o -> o.desc(Post::getStar))
      .asList(new RowBounds(100, 50), p -> p.property(Post::getTitle).property(Post::getStar));
  ```
- <code>BoundList&lt;T&gt; asBoundList(RowBounds rowBounds)</code>
  执行查询，返回多条结果，并统计总数
  ```java
  // 查询start大于等于100的Post，并按照LIMIT 100, 50进行分页
  dao.select(Post.class).where(c -> c.ge(Post::getStar, 100))
      .asBoundList(new RowBounds(100, 50));
      
  // 查询start大于等于100的Post，按照star字段降序，返回title和star字段，并按照LIMIT 100, 50进行分页
  dao.select(Post.class).where(c -> c.ge(Post::getStar, 100))
      .orderBy(o -> o.desc(Post::getStar))
      .asBoundList(new RowBounds(100, 50), p -> p.property(Post::getTitle).property(Post::getStar));
  ```
- <code>&lt;K&gt; Map&lt;K, T&gt; asMap(String mapKey)</code>
  执行查询，返回多条结果，并以mapKey为关键字放置在map中，与<code>SqlSession.selectMap()</code>执行效果相同
  ```java
  // 查询所有Post，并按照id放置在map中
  dao.select(Post.class).asMap("id");
  ```
### select count

<code>long count(Class&lt;?&gt; entityType, @Nullable SqlCriterion criterion)</code>

```java
  // 查询title包含“title”的Post数量
  dao.count(Post.class, c -> c.like(Post::getTitle, "%news%"));
```

### select exists

<code>boolean exists(Class&lt;?&gt; entityType, @Nullable SqlCriterion criterion)</code>

```java
  // 查询是否存在id为123的Post
  dao.exists(Post.class, c -> c.eq(Post::getId, 123));
```

### update
- <code>&lt;T&gt; int update(Class&lt;T&gt; entityType, @Nullable SqlUpdate update, @Nullable SqlCriterion criterion)</code>
  更新指定字段
  ```java
  // id为123的Post，title设置为“Big news”，star值增加3
  dao.update(Post.class,
      SqlUpdate.create().set("title", "Big news").add("star", 1),
      SqlCriterion.eq("id", 123));

  // Lambda写法
  dao.update(Post.class,
      u -> u.set(Post::getTitle, "Big news").add(Post::getStar, 1),
      c -> c.eq(Post::getId, 123));
  ```
- <code>&lt;T&gt; int updateEntity(Class&lt;T&gt; entityType, @Nullable T entity, boolean ignoreNulls, @Nullable SqlCriterion criterion)</code>
  更新整个实体
  ```java
  Post post = new Post();
  post.setTitle("Big news");
  post.setStar(500);
  // 按照post中的非空字段更新id为123的Post
  dao.update(Post.class, post, true, c -> c.eq(Post::getId, 123));

  // 其中ignoreNulls可以省略，默认值为true
  dao.update(Post.class, post, c -> c.eq(Post::getId, 123));
  ```

### delete

<code>int delete(Class&lt;?&gt; entityType, @Nullable SqlCriterion criterion)</code>

```java
// 删除star小于10的Post
dao.delete(Post.class, c -> c.lt(Post::getStar, 10));
```

### insert

<code>&lt;T&gt; int insert(Class&lt;T&gt; entityType, @Nullable T value)</code>

```java
Post post = new Post();
post.setTitle("New post");
post.setCreateTime(new Date());
// 插入新的Post
dao.insert(Post.class, post);
```

## SqlCriterion条件
```java
SqlCriterion.eq("id", 123); // id = 123
SqlCriterion.ne("id", 123); // id <> 123
SqlCriterion.gt("star", 100); // star > 100
SqlCriterion.ge("star", 100); // star >= 100
SqlCriterion.lt("star", 200); // star < 200
SqlCriterion.le("star", 200); // star <= 200
SqlCriterion.isNull("postId"); // postId IS NULL
SqlCriterion.isNotNull("postId"); // postId IS NOT NULL
SqlCriterion.like("title", "%news%"); // title LIKE '%news%'
SqlCriterion.like("title", "%big^_news%", "^"); // title LIKE '%big^_news%' ESCAPE '^'
SqlCriterion.between("star", 20, 80); // star BETWEEN 20 AND 80
SqlCriterion.in("section", Arrays.asList("SPORTS", "LIVE", "ART")); // section IN('SPORTS', 'LIVE', 'ART')
SqlCriterion.in("section", "SPORTS", "LIVE", "ART"); // section IN('SPORTS', 'LIVE', 'ART')

SqlCriterion.and(SqlCriterion.lt("star", 800), SqlCriterion.gt("star", 100)); // star < 800 AND star > 100
SqlCriterion.and(Arrays.asList(SqlCriterion.lt("star", 800), SqlCriterion.gt("star", 100))); // star < 800 AND star > 100

SqlCriterion.or(SqlCriterion.gt("star", 50), SqlCriterion.lt("star", 40)); // star > 50 OR star < 40
SqlCriterion.or(Arrays.asList(SqlCriterion.gt("star", 50), SqlCriterion.lt("star", 40))); // star > 50 OR star < 40

SqlCriterion.not(SqlCriterion.eq("id", 123)); // NOT (id = 123)
SqlCriterion.not(SqlCriterion.or(SqlCriterion.gt("star", 50), SqlCriterion.lt("star", 40))); // NOT(star > 50 OR star < 40)
SqlCriterion.notAny(SqlCriterion.gt("star", 50), SqlCriterion.lt("star", 40)); // NOT(star > 50 OR star < 40)

SqlCriterion..none(); // 空条件
SqlCriterion.trueValue(); // (1 = 1)
SqlCriterion.falseValue(); // (1 = 0)

// Lambda版本
SqlCriterion.lambda(Post.class, c -> c.eq(Post::getId, 123));

SqlCriterion.lambda(Post.class, c -> SqlCriterion.and(
  c.gt(Post::getStar, 500),
  c.like(Post::getTitle, "%news%"),
  c.in(Post::getSection, "SPORTS", "ART")
));
```

## SqlOrderBy排序

```java
SqlOrderBy.create().desc("star").asc("title"); // ORDER BY star DESC, title ASC

// Lambda版本
SqlOrderBy.lambda(Post.class, o -> o.desc(Post::getStar).asc(Post::getTitle));
```

## SqlUpdate更新

```java
SqlUpdate.create().set("title", "New title").add("star", 2); // SET title = 'New title', star = star + 2, 
SqlUpdate.create().subtract("star", 3).nullify("createTime"); // SET star = star - 3, createTime = NULL

// Lambda版本
SqlUpdate.lambda(Post.class, u -> u.set(Post::getTitle, "New title").add(Post::getStar, 2));
SqlUpdate.lambda(Post.class, u -> u.subtract(Post::getStar, 3).nullify(Post::getCreateTime));
```

## SqlProjections投影

```java
SqlProjections.
```