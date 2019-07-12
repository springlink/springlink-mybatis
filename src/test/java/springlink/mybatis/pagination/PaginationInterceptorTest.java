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

package springlink.mybatis.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import springlink.mybatis.entity.Post;
import springlink.mybatis.util.BoundList;

public class PaginationInterceptorTest {
	private static SqlSessionFactory sqlSessionFactory;

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
		cfg.addInterceptor(new TestInterceptor());
		cfg.addInterceptor(new PaginationInterceptor());
		cfg.addInterceptor(new TestInterceptor());
	}

	@Test
	public void test() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			BoundList<Post> posts;

			posts = (BoundList<Post>) session.<Post>selectList(
					"springlink.mybatis.selectPosts", Collections.singletonMap("blogIds", Arrays.asList(1, 2)),
					new PaginationRowBounds(0, 3));
			assertThat(posts.total()).isEqualTo(4);
			assertThat(posts.offset()).isEqualTo(0);
			assertThat(posts.limit()).isEqualTo(3);
			assertThat(posts).hasSize(3);
			assertThat(posts).extracting(Post::getId).containsExactly(1, 2, 3);

			posts = (BoundList<Post>) session.<Post>selectList(
					"springlink.mybatis.selectPosts", Collections.singletonMap("blogIds", Arrays.asList(1, 2)),
					new PaginationRowBounds(3, 3));
			assertThat(posts.total()).isEqualTo(4);
			assertThat(posts.offset()).isEqualTo(3);
			assertThat(posts.limit()).isEqualTo(3);
			assertThat(posts).hasSize(1);
			assertThat(posts).extracting(Post::getId).containsExactly(4);

			List<Post> plainPosts = session.<Post>selectList(
					"springlink.mybatis.selectPosts", Collections.singletonMap("blogIds", Arrays.asList(1, 2)),
					new RowBounds(3, 3));
			assertThat(plainPosts).hasSize(1);
			assertThat(plainPosts).extracting(Post::getId).containsExactly(4);
		}
	}

	@Intercepts({
			@Signature(type = Executor.class, method = "query", args = {
					MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
			})
	})
	public static class TestInterceptor implements Interceptor {
		@Override
		public Object intercept(Invocation invocation) throws Throwable {
			return invocation.proceed();
		}

		@Override
		public Object plugin(Object target) {
			if (target instanceof Executor) {
				return Plugin.wrap(target, this);
			}
			return target;
		}

		@Override
		public void setProperties(Properties properties) {
		}
	}
}
