/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package springlink.mybatis.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static springlink.mybatis.sql.SqlCriterion.and;
import static springlink.mybatis.sql.SqlCriterion.between;
import static springlink.mybatis.sql.SqlCriterion.eq;
import static springlink.mybatis.sql.SqlCriterion.falseValue;
import static springlink.mybatis.sql.SqlCriterion.ge;
import static springlink.mybatis.sql.SqlCriterion.gt;
import static springlink.mybatis.sql.SqlCriterion.in;
import static springlink.mybatis.sql.SqlCriterion.isNotNull;
import static springlink.mybatis.sql.SqlCriterion.isNull;
import static springlink.mybatis.sql.SqlCriterion.le;
import static springlink.mybatis.sql.SqlCriterion.like;
import static springlink.mybatis.sql.SqlCriterion.lt;
import static springlink.mybatis.sql.SqlCriterion.ne;
import static springlink.mybatis.sql.SqlCriterion.not;
import static springlink.mybatis.sql.SqlCriterion.or;
import static springlink.mybatis.sql.SqlCriterion.trueValue;

import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import springlink.mybatis.entity.Author;
import springlink.mybatis.entity.Author2;
import springlink.mybatis.entity.Author3;
import springlink.mybatis.entity.Comment;
import springlink.mybatis.entity.GeneratedKeysTable;
import springlink.mybatis.entity.Post;
import springlink.mybatis.entity.Tag;
import springlink.mybatis.entity.Tag2;
import springlink.mybatis.registry.SqlDialect;
import springlink.mybatis.registry.SqlRegistry;
import springlink.mybatis.sql.SqlCriterion;
import springlink.mybatis.sql.SqlOrderBy;
import springlink.mybatis.sql.SqlProjections;
import springlink.mybatis.sql.SqlUpdate;
import springlink.mybatis.util.BoundList;

public class DefaultSqlDaoTest {
	private static SqlSessionFactory sqlSessionFactory;
	private static SqlRegistry sqlRegistry;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		try (Reader reader = Resources.getResourceAsReader("entity/mybatis-config-h2.xml")) {
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		}

		try (Reader reader = Resources.getResourceAsReader("entity/blog-h2.sql");
				SqlSession session = sqlSessionFactory.openSession()) {
			ScriptRunner runner = new ScriptRunner(session.getConnection());
			runner.setLogWriter(null);
			runner.runScript(reader);
			session.commit();
		}

		Configuration cfg = sqlSessionFactory.getConfiguration();
		sqlRegistry = new SqlRegistry(cfg, SqlDialect.get("h2"));
		sqlRegistry.addType(Comment.class);
		sqlRegistry.addType(Author.class);
		sqlRegistry.addType(Author2.class);
		sqlRegistry.addType(Author3.class);
		sqlRegistry.addType(Post.class);
		sqlRegistry.addType(Tag.class);
		sqlRegistry.addType(Tag2.class);
		sqlRegistry.addType(GeneratedKeysTable.class);
	}

	@Test
	public void shouldInsert() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			Post post = new Post();
			post.setId(6);
			post.setAuthorId(102);
			post.setBlogId(2);
			post.setBody("Yes, this is a test post, you're right");
			post.setCreatedOn(new Date());
			post.setDraft(false);
			post.setSection("DISCUSS");
			post.setStar(3);
			post.setSubject("Testing my dao");

			assertThat(dao.insert(Post.class, post)).isEqualTo(1);
		}
	}

	@Test
	public void shouldInsertWithGeneratedKeys() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			GeneratedKeysTable gkt = new GeneratedKeysTable();
			gkt.setId2(123);

			assertThat(dao.insert(GeneratedKeysTable.class, gkt)).isEqualTo(1);
			assertThat(gkt.getId1()).isEqualTo(1);
			assertThat(gkt.getId2()).isEqualTo(123);
		}
	}

	@Test
	public void shouldDelete() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.count(Post.class, eq("id", 1))).isEqualTo(1L);

			assertThat(dao.delete(Post.class, eq("id", 1))).isEqualTo(1);

			assertThat(dao.count(Post.class, eq("id", 1))).isEqualTo(0L);

			session.rollback();

			assertThat(dao.count(Post.class, eq("id", 1))).isEqualTo(1L);
		}
	}

	@Test
	public void shouldUpdateWithShortMethod() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.update(Post.class, SqlUpdate.create().set("section", "NEW_SECTION"), eq("id", 1)))
					.isEqualTo(1);

			assertThat(dao.update(Post.class, SqlUpdate.create().add("star", 9), eq("id", 1)))
					.isEqualTo(1);

			assertThat(dao.update(Post.class, SqlUpdate.create().subtract("star", 3), eq("id", 1)))
					.isEqualTo(1);

			assertThat(dao.update(Post.class, SqlUpdate.create().nullify("blogId"), eq("id", 1)))
					.isEqualTo(1);

			assertThat(dao.update(Post.class, SqlUpdate.create().set("authorId", 11).set("body", "text body"), eq("id", 1)))
					.isEqualTo(1);

			Post post = dao.select(Post.class).where(eq("id", 1)).asOne().orElse(null);
			assertThat(post.getSection()).isEqualTo("NEW_SECTION");
			assertThat(post.getStar()).isEqualTo(6);
			assertThat(post.getBlogId()).isNull();
			assertThat(post.getAuthorId()).isEqualTo(11);
			assertThat(post.getBody()).isEqualTo("text body");

			session.rollback();
		}
	}

	@Test
	public void shouldUpdate() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.update(Post.class, SqlUpdate.create().set("section", "NEW_SECTION"), eq("id", 1)))
					.isEqualTo(1);

			assertThat(dao.update(Post.class, SqlUpdate.create().add("star", 9), eq("id", 1)))
					.isEqualTo(1);

			assertThat(dao.update(Post.class, SqlUpdate.create().subtract("star", 3), eq("id", 1)))
					.isEqualTo(1);

			assertThat(dao.update(Post.class, SqlUpdate.create().nullify("blogId"), eq("id", 1)))
					.isEqualTo(1);

			Post postValue = new Post();
			postValue.setAuthorId(11);
			postValue.setBody("text body");
			assertThat(dao.updateEntity(Post.class, postValue, eq("id", 1)))
					.isEqualTo(1);
			assertThat(dao.update(Post.class, SqlUpdate.create().set("authorId", 33), eq("id", 1)))
					.isEqualTo(1);

			Post post = dao.select(Post.class).where(eq("id", 1)).asOne().orElse(null);
			assertThat(post.getSection()).isEqualTo("NEW_SECTION");
			assertThat(post.getStar()).isEqualTo(6);
			assertThat(post.getBlogId()).isNull();
			assertThat(post.getAuthorId()).isEqualTo(33);
			assertThat(post.getBody()).isEqualTo("text body");

			Date now = new Date(System.currentTimeMillis() / 1000 * 1000);

			Post emptyPostValue = new Post();
			emptyPostValue.setId(1);
			emptyPostValue.setAuthorId(11);
			emptyPostValue.setCreatedOn(now);
			emptyPostValue.setSection("SECTION");
			emptyPostValue.setSubject("SUBJECT");
			emptyPostValue.setBody("BODY");
			emptyPostValue.setDraft(false);
			emptyPostValue.setStar(123);
			assertThat(dao.updateEntity(Post.class, emptyPostValue, false, eq("id", 1)))
					.isEqualTo(1);

			post = dao.select(Post.class).where(eq("id", 1)).asOne().orElse(null);
			assertThat(post.getId()).isEqualTo(1);
			assertThat(post.getAuthorId()).isEqualTo(11);
			assertThat(post.getBlogId()).isNull();
			assertThat(post.getBody()).isEqualTo("BODY");
			assertThat(post.getCreatedOn()).isEqualTo(now);
			assertThat(post.getDraft()).isFalse();
			assertThat(post.getSection()).isEqualTo("SECTION");
			assertThat(post.getStar()).isEqualTo(123);
			assertThat(post.getSubject()).isEqualTo("SUBJECT");

			session.rollback();
		}
	}

	@Test
	public void shouldUpdateByPropertyAlias() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			Date date = new Date(System.currentTimeMillis() / 1000 * 1000);

			assertThat(dao.update(Post.class, SqlUpdate.create().set("#CreateDate", date), eq("id", 1)))
					.isEqualTo(1);

			Post post = dao.select(Post.class).where(eq("id", 1)).asOne().orElse(null);
			assertThat(post.getCreateTime()).isEqualTo(date);

			session.rollback();
		}
	}

	@Test
	public void shouldUpdateNothing() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.update(Post.class, (SqlUpdate) null, eq("id", 1))).isEqualTo(0);
		}
	}

	@Test
	public void shouldAllowNullArguments() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			dao.select(Post.class).where((SqlCriterion) null).orderBy((SqlOrderBy) null);

			dao.update(Post.class, (SqlUpdate) null, null);
		}
	}

	@Test
	public void shouldCount() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.count(Post.class, eq("id", 1))).isEqualTo(1);

			assertThat(dao.count(Post.class, (SqlCriterion) null)).isEqualTo(5);
		}
	}

	@Test
	public void shouldExists() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.exists(Post.class, eq("id", 1))).isTrue();

			assertThat(dao.exists(Post.class, (SqlCriterion) null)).isTrue();

			assertThat(dao.exists(Post.class, and(eq("id", 999), like("blogAuthorName", "%j%")))).isFalse();
		}
	}

	@Test
	public void shouldSelectEntity() throws ParseException {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Post post = dao.select(Post.class).where(eq("#id", 2)).asOne().orElse(null);
			assertThat(post.getAuthorId()).isEqualTo(101);
			assertThat(post.getAuthorName()).isEqualTo("jim");
			assertThat(post.getBlogAuthorName()).isEqualTo("jim");
			assertThat(post.getBlogId()).isEqualTo(1);
			assertThat(post.getBlogTitle()).isEqualTo("Jim Business");
			assertThat(post.getBody()).isEqualTo("That's not a dog.  THAT's a dog!");
			assertThat(post.getCreatedOn()).isEqualTo(dateFormat.parse("2008-01-12 00:00:00"));
			assertThat(post.getCreateTime()).isEqualTo(dateFormat.parse("2008-01-12 00:00:00"));
			assertThat(post.getDraft()).isEqualTo(false);
			assertThat(post.getId()).isEqualTo(2);
			assertThat(post.getSection()).isEqualTo("VIDEOS");
			assertThat(post.getStar()).isEqualTo(100);
			assertThat(post.getSubject()).isEqualTo("Paul Hogan on Toy Dogs");
		}
	}

	@Test
	public void shouldSelectEntityWithOrders() throws ParseException {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.select(Post.class).orderBy(SqlOrderBy.create().asc("section").desc("star")).asList())
					.extracting(Post::getId)
					.containsExactly(4, 5, 1, 3, 2);
		}
	}

	@Test
	public void shouldSelectProjectionWithOrders() throws ParseException {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.select(Post.class).orderBy(SqlOrderBy.create().asc("section").desc("star"))
					.asList(SqlProjections.create().property("id", "#id")))
							.containsExactly(4, 5, 1, 3, 2);
		}
	}

	@Test
	public void shouldSelectEntityForUpdate() throws ParseException {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.select(Post.class).orderBy(SqlOrderBy.create().asc("section").desc("star")).forUpdate().asList())
					.extracting(Post::getId)
					.containsExactly(4, 5, 1, 3, 2);
		}
	}

	@Test
	public void shouldSelectProjection() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(
					dao.select(Post.class).where(eq("id", 2)).<Integer>asOne(SqlProjections.create().property("aid", "authorId"))
							.orElse(null))
									.isEqualTo(101);

			assertThat(dao.select(Post.class).<String>asList(SqlProjections.create().distinct("s", "section")))
					.containsExactlyInAnyOrder("NEWS", "VIDEOS", "PODCASTS", "IMAGES");

			assertThat(dao.select(Post.class).<Long>asOne(SqlProjections.create().count("aid", "authorId")).orElse(null))
					.isEqualTo(5);

			assertThat(
					dao.select(Post.class).<Long>asOne(SqlProjections.create().countDistinct("aid", "authorId")).orElse(null))
							.isEqualTo(2);

			assertThat(dao.select(Post.class).<Number>asOne(SqlProjections.create().max("s", "star")).orElse(null).intValue())
					.isEqualTo(100);

			assertThat(dao.select(Post.class).<Number>asOne(SqlProjections.create().min("s", "star")).orElse(null).intValue())
					.isEqualTo(0);

			assertThat(dao.select(Post.class).<Number>asOne(SqlProjections.create().sum("s", "star")).orElse(null).intValue())
					.isEqualTo(183);

			assertThat(dao.select(Post.class).<Number>asOne(SqlProjections.create().avg("s", "star")).orElse(null).intValue())
					.isEqualTo(183 / 5);

			// Join column
			assertThat(dao.select(Post.class).where(eq("#id", 2))
					.<String>asOne(SqlProjections.create().property("an", "authorName")).orElse(null))
							.isEqualTo("jim");

			assertThat(
					dao.select(Post.class).<Long>asOne(SqlProjections.create().count("ban", "blogAuthorName")).orElse(null))
							.isEqualTo(4);

			assertThat(dao.select(Post.class).<String>asList(SqlProjections.create().distinct("an", "authorName")))
					.containsExactlyInAnyOrder("jim", "sally");
		}
	}

	@Test
	public void shouldSelectProjections() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.select(Post.class).where(eq("id", 2)).<Map<String, Integer>>asOne(SqlProjections.create()
					.property("bid", "blogId")
					.property("aid", "authorId")).orElse(null)
					.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue())))
							.isEqualTo(ImmutableMap.of("bid", 1, "aid", 101));

			BoundList<Integer> bl1 = dao.select(Post.class).orderBy(SqlOrderBy.create().asc("id"))
					.asBoundList(new RowBounds(1, 2), SqlProjections.create().property("id", "id"));
			assertThat(bl1.total()).isEqualTo(5);
			assertThat(bl1.offset()).isEqualTo(1);
			assertThat(bl1.limit()).isEqualTo(2);
			assertThat(bl1).containsExactlyInAnyOrder(2, 3);

			BoundList<Map<String, Object>> bl2 = dao.select(Post.class).orderBy(SqlOrderBy.create().asc("id"))
					.asBoundList(new RowBounds(1, 2), SqlProjections.create().property("id", "id").property("aid", "authorId"));
			assertThat(bl1.total()).isEqualTo(5);
			assertThat(bl1.offset()).isEqualTo(1);
			assertThat(bl1.limit()).isEqualTo(2);
			assertThat(
					bl2.get(0).entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue())))
							.containsEntry("id", 2)
							.containsEntry("aid", 101);
			assertThat(
					bl2.get(1).entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue())))
							.containsEntry("id", 3)
							.containsEntry("aid", 102);

			assertThat(dao.select(Post.class).asList(SqlProjections.create().property("id", "id")))
					.containsExactlyInAnyOrder(1, 2, 3, 4, 5);

			assertThat(dao.select(Post.class)
					.<Map<String, Integer>>asList(SqlProjections.create().property("id", "id").property("aid", "authorId"))
					.stream()
					.map(item -> item.entrySet().stream()
							.collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue()))))
									.containsExactlyInAnyOrder(
											ImmutableMap.of("id", 1, "aid", 101),
											ImmutableMap.of("id", 2, "aid", 101),
											ImmutableMap.of("id", 3, "aid", 102),
											ImmutableMap.of("id", 4, "aid", 102),
											ImmutableMap.of("id", 5, "aid", 101));

			assertThat(dao.select(Post.class).asList(new RowBounds(1, 2),
					SqlProjections.create().property("id", "id")))
							.containsExactlyInAnyOrder(2, 3);

			assertThat(dao.select(Post.class).<Map<String, Object>>asList(new RowBounds(1, 2),
					SqlProjections.create().property("id", "id").property("aid", "authorId"))
					.stream()
					.map(item -> item.entrySet().stream()
							.collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue()))))
									.containsExactlyInAnyOrder(
											ImmutableMap.of("id", 2, "aid", 101),
											ImmutableMap.of("id", 3, "aid", 102));
		}
	}

	@Test
	public void shouldCountWithConditions() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.count(Post.class, eq("section", "NEWS"))).isEqualTo(1);

			// mysql is case insensitive
			// assertThat(dao.count(Post.class, eq("section", "news"))).isEqualTo(0);

			assertThat(dao.count(Post.class, ne("section", "NEWS"))).isEqualTo(4);

			// mysql is case insensitive
			// assertThat(dao.count(Post.class, ne("section", "news"))).isEqualTo(5);

			assertThat(dao.count(Post.class, gt("star", 66))).isEqualTo(1);

			assertThat(dao.count(Post.class, ge("star", 66))).isEqualTo(2);

			assertThat(dao.count(Post.class, lt("star", 66))).isEqualTo(3);

			assertThat(dao.count(Post.class, le("star", 66))).isEqualTo(4);

			assertThat(dao.count(Post.class, isNull("blogId"))).isEqualTo(1);

			assertThat(dao.count(Post.class, isNotNull("blogId"))).isEqualTo(4);

			assertThat(dao.count(Post.class, like("body", "%'%"))).isEqualTo(1);

			assertThat(dao.count(Post.class, like("subject", "%~%", "~"))).isEqualTo(1);

			assertThat(dao.count(Post.class, between("star", 7, 66))).isEqualTo(3);

			assertThat(dao.count(Post.class, in("star", 7, 66, 99))).isEqualTo(2);

			assertThat(dao.count(Post.class, not(eq("section", "NEWS")))).isEqualTo(4);
		}
	}

	@Test
	public void shouldSelectWithRowBounds() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			BoundList<Post> partialList = dao.select(Post.class)
					.where(isNotNull("blogId"))
					.orderBy(SqlOrderBy.create().asc("id"))
					.asBoundList(new RowBounds(1, 5));

			assertThat(partialList.offset()).isEqualTo(1);
			assertThat(partialList.limit()).isEqualTo(5);
			assertThat(partialList.total()).isEqualTo(4);
			assertThat(partialList).hasSize(3);
			assertThat(partialList.get(0).getId()).isEqualTo(2);
			assertThat(partialList.get(1).getId()).isEqualTo(3);
			assertThat(partialList.get(2).getId()).isEqualTo(4);

			List<Post> list = dao.select(Post.class)
					.where(isNotNull("blogId"))
					.orderBy(SqlOrderBy.create().desc("id"))
					.asList(new RowBounds(1, 5));

			assertThat(list).hasSize(3);
			assertThat(list.get(0).getId()).isEqualTo(3);
			assertThat(list.get(1).getId()).isEqualTo(2);
			assertThat(list.get(2).getId()).isEqualTo(1);
		}
	}

	@Test
	public void shouldWorkWithoutSqlEntityAnnotation() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.select(Tag.class).where(eq("id", 1)).asOne().orElse(null).getName()).isEqualTo("funny");
		}
	}

	@Test
	public void shouldWorkWithSqlCacheRefAnnotation() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.select(Author2.class).where(eq("id", 101)).asOne().orElse(null).getUsername()).isEqualTo("jim");
			assertThat(dao.select(Author3.class).where(eq("id", 101)).asOne().orElse(null).getUsername()).isEqualTo("jim");
		}
	}

	@Test
	public void shouldWorkWithConstantValue() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.select(Post.class).where(or(eq("id", 101), trueValue())).asList()).hasSize(5);
			assertThat(dao.select(Author3.class).where(and(eq("id", 101), falseValue())).asList()).hasSize(0);
		}
	}

	@Test
	public void shouldWorkWithSqlNameStrategyAnnotation() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.select(Comment.class).where(eq("id", 1)).asOne().orElse(null).getPostId()).isEqualTo(1);
			assertThat(dao.select(Tag2.class).where(eq("id", 1)).asOne().orElse(null).getNaMe()).isEqualTo("funny");
		}
	}

	@Test
	public void shouldLambdaWorks() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

			assertThat(dao.select(Post.class)
					.orderBy(SqlOrderBy.create(Post.class).asc(Post::getSection).desc(Post::getStar))
					.asList(SqlProjections.create(Post.class).property("id", Post::getId)))
							.containsExactly(4, 5, 1, 3, 2);

			assertThat(dao.select(Comment.class)
					.where(SqlCriterion.lambda(Comment.class, t -> t.eq(Comment::getId, 1)))
					.asOne().orElse(null).getPostId()).isEqualTo(1);

			assertThat(dao.count(Post.class,
					SqlCriterion.lambda(Post.class, t -> t.eq(Post::getSection, "NEWS")))).isEqualTo(1);

			assertThat(dao.exists(Post.class, SqlCriterion.lambda(Post.class, t -> t.eq(Post::getId, 1)))).isTrue();

			assertThat(dao.count(Post.class, SqlCriterion.lambda(Post.class, t -> t.eq(Post::getId, 1)))).isEqualTo(1L);

			session.rollback();

			assertThat(dao.update(Post.class, (u) -> u.set(Post::getSection, "NEW_SECTION"), c -> c.eq(Post::getId, 1)))
					.isEqualTo(1);

			session.rollback();

			assertThat(dao.update(Post.class,
					u -> u.set(Post::getSection, "NEW_SECTION").set(Post::getStar, 9),
					t -> t.eq(Post::getId, 1)))
							.isEqualTo(1);

			session.rollback();
		}
	}

	@Test
	public void testSelectPerformance() {
		SqlSession session = Mockito.mock(SqlSession.class);
		when(session.selectOne(null, null)).thenReturn(Lists.newArrayList());
		SqlDao dao = new DefaultSqlDao(sqlRegistry, session);

		int times = 200;
		long ts1 = 0, ts2 = 0, ts3 = 0;
		long s = 0;
		System.gc();
		for (int i = 0; i < times; ++i) {
			long ss = System.currentTimeMillis();
			dao.select(Post.class).where(eq("#id", i)).where(like("subject", "I%")).asOne();
			long st = System.currentTimeMillis() - ss;
			if (st > ts1) {
				ts1 = st;
			} else if (st > ts2) {
				ts2 = st;
			} else if (st > ts3) {
				ts3 = st;
			}
			s += st;
		}
		System.out.println(String.format("avg: %.2f, top1: %d, top2: %d, top3: %d, ", ((double) s / times), ts1, ts2, ts3));
	}
}
