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

package springlink.mybatis.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import springlink.mybatis.registry.SqlDialect;
import springlink.mybatis.registry.SqlRegistry;
import springlink.mybatis.sql.SqlCriterion;

public class CustomMapperTest {
	private static SqlSessionFactory sqlSessionFactory;
	private static SqlRegistry sqlRegistry;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		try (Reader reader = Resources.getResourceAsReader("entity/mybatis-config-h2.xml")) {
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		}
		runScript(sqlSessionFactory, "entity/blog-h2-schema.sql");
		runScript(sqlSessionFactory, "entity/blog-h2-data.sql");

		Configuration cfg = sqlSessionFactory.getConfiguration();
		cfg.addMapper(CustomMapper.class);

		sqlRegistry = new SqlRegistry(cfg, SqlDialect.get("mysql"));
	}

	private static void runScript(SqlSessionFactory sqlSessionFactory, String resource) throws IOException {
		try (SqlSession session = sqlSessionFactory.openSession();
				Reader reader = Resources.getResourceAsReader(resource)) {
			ScriptRunner runner = new ScriptRunner(session.getConnection());
			runner.setAutoCommit(true);
			runner.setStopOnError(false);
			runner.setLogWriter(null);
			runner.setErrorLogWriter(null);
			runner.runScript(reader);
		}
	}

	@Test
	public void test() {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			CustomMapper mapper = session.getMapper(CustomMapper.class);
			List<String> posts = mapper.selectPostAuthorName(
					sqlRegistry.getContext()
							.putObject("where", SqlCriterion.eq("author.username", "jim")));
			assertThat(posts).containsExactlyInAnyOrder("jim", "jim", "jim");
		}
	}
}
