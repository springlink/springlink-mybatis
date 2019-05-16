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

package springlink.mybatis.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.apache.ibatis.session.Configuration;
import org.junit.Test;

import springlink.mybatis.entity.Author;
import springlink.mybatis.entity.Blog;
import springlink.mybatis.entity.Post;
import springlink.mybatis.registry.SqlContext;
import springlink.mybatis.registry.SqlDialect;
import springlink.mybatis.registry.SqlRegistry;

public class SqlCriterionTest {
	@Test
	public void shouldMakeEq() {
		assertThat(SqlCriterion.eq("xprop", "123456"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.EQ);
					assertThat(condition.getArgs()).containsExactly("123456");
				});
	}

	@Test
	public void shouldMakeNe() {
		assertThat(SqlCriterion.ne("xprop", "123456"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.NE);
					assertThat(condition.getArgs()).containsExactly("123456");
				});
	}

	@Test
	public void shouldMakeGt() {
		assertThat(SqlCriterion.gt("xprop", "123456"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.GT);
					assertThat(condition.getArgs()).containsExactly("123456");
				});
	}

	@Test
	public void shouldMakeGe() {
		assertThat(SqlCriterion.ge("xprop", "123456"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.GE);
					assertThat(condition.getArgs()).containsExactly("123456");
				});
	}

	@Test
	public void shouldMakeLt() {
		assertThat(SqlCriterion.lt("xprop", "123456"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.LT);
					assertThat(condition.getArgs()).containsExactly("123456");
				});
	}

	@Test
	public void shouldMakeLe() {
		assertThat(SqlCriterion.le("xprop", "123456"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.LE);
					assertThat(condition.getArgs()).containsExactly("123456");
				});
	}

	@Test
	public void shouldMakeIsNull() {
		assertThat(SqlCriterion.isNull("xprop"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.IS_NULL);
					assertThat(condition.getArgs()).isEmpty();
				});
	}

	@Test
	public void shouldMakeIsNotNull() {
		assertThat(SqlCriterion.isNotNull("xprop"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.IS_NOT_NULL);
					assertThat(condition.getArgs()).isEmpty();
				});
	}

	@Test
	public void shouldMakeLike() {
		assertThat(SqlCriterion.like("xprop", "123456"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.LIKE);
					assertThat(condition.getArgs()).containsExactly("123456");
				});
	}

	@Test
	public void shouldMakeLikeEsc() {
		assertThat(SqlCriterion.like("xprop", "123456", "**"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.LIKE_ESC);
					assertThat(condition.getArgs()).containsExactly("123456", "**");
				});
	}

	@Test
	public void shouldMakeBetween() {
		assertThat(SqlCriterion.between("xprop", "123456", "654321"))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.BETWEEN);
					assertThat(condition.getArgs()).containsExactly("123456", "654321");
				});
	}

	@Test
	public void shouldMakeIn() {
		assertThat(SqlCriterion.in("xprop", 1, 2, 3, 4, 5, 6, 7, 8, 9))
				.isInstanceOfSatisfying(SqlCriterion.Condition.class, condition -> {
					assertThat(condition.getProperty()).isEqualTo("xprop");
					assertThat(condition.getType()).isEqualTo(SqlCriterion.ConditionType.IN);
					assertThat(condition.getArgs()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
				});
	}

	@Test
	public void shouldMakeAnd() {
		SqlCriterion c1 = mock(SqlCriterion.class, "c1");
		SqlCriterion c2 = mock(SqlCriterion.class, "c2");
		SqlCriterion c3 = mock(SqlCriterion.class, "c3");
		assertThat(SqlCriterion.and(c1, c2, c3))
				.isInstanceOfSatisfying(SqlCriterion.Junction.class, junction -> {
					assertThat(junction.getType()).isEqualTo(SqlCriterion.JunctionType.AND);
					assertThat(junction.getCriteria()).containsExactly(c1, c2, c3);
				});
	}

	@Test
	public void shouldMakeOr() {
		SqlCriterion c1 = mock(SqlCriterion.class, "c1");
		SqlCriterion c2 = mock(SqlCriterion.class, "c2");
		SqlCriterion c3 = mock(SqlCriterion.class, "c3");
		assertThat(SqlCriterion.or(c1, c2, c3))
				.isInstanceOfSatisfying(SqlCriterion.Junction.class, junction -> {
					assertThat(junction.getType()).isEqualTo(SqlCriterion.JunctionType.OR);
					assertThat(junction.getCriteria()).containsExactly(c1, c2, c3);
				});
	}

	@Test
	public void shouldMakeNot() {
		SqlCriterion c1 = mock(SqlCriterion.class, "c1");
		assertThat(SqlCriterion.not(c1))
				.isInstanceOfSatisfying(SqlCriterion.Junction.class, junction -> {
					assertThat(junction.getType()).isEqualTo(SqlCriterion.JunctionType.NOT);
					assertThat(junction.getCriteria()).containsExactly(c1);
				});
	}
	
	@Test
	public void testToSql() {
		Configuration config = new Configuration();
		SqlRegistry registry = new SqlRegistry(config, SqlDialect.getDefault());
		SqlContext ctx = registry.getContext();
		ctx.putEntity(Post.class, "t");
		ctx.putEntity("blog", Blog.class, "b");
		ctx.putEntity("author", Author.class,"a");
		SqlCriterion c = SqlCriterion.and(
				SqlCriterion.or(Arrays.asList(
						SqlCriterion.eq("blogId", 101),
						SqlCriterion.gt("id", 3)
				)),
				SqlCriterion.or(Arrays.asList(
						SqlCriterion.eq("author.password", "Secret123"),
						SqlCriterion.notAll(Arrays.asList(
								SqlCriterion.eq("author.username", "BigAuthor"),
								SqlCriterion.like("blog.title", "%#%News%", "#")
						))
				))
		);
		ctx.putObject("where", c);
		String sql = ctx.sql("where");
		System.out.println(sql);
	}
}
