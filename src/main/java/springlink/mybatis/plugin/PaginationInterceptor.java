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

package springlink.mybatis.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.google.common.collect.Lists;

import springlink.mybatis.registry.SqlDialect;
import springlink.mybatis.util.ArrayBoundList;
import springlink.mybatis.util.Asserts;
import springlink.mybatis.util.BoundSqlBuilder;
import springlink.mybatis.util.MappedStatementBuilder;
import springlink.mybatis.util.PhysicalRowBounds;

@Intercepts({
		@Signature(type = Executor.class, method = "query", args = {
				MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
		})
})
public class PaginationInterceptor implements Interceptor {
	private static final String DIALECT_NAME_KEY = "dialectName";

	private static final Field pluginTarget;

	static {
		try {
			pluginTarget = Plugin.class.getDeclaredField("target");
			pluginTarget.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e);
		}
	}

	private SqlDialect dialect;

	public PaginationInterceptor() {
		this(SqlDialect.getCurrent());
	}

	public PaginationInterceptor(String dialectName) {
		this(SqlDialect.get(dialectName));
	}

	public PaginationInterceptor(SqlDialect dialect) {
		Asserts.notNull(dialect, "dialect");
		this.dialect = dialect;
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object[] args = invocation.getArgs();
		RowBounds rowBounds = (RowBounds) args[2];
		if (!(rowBounds instanceof PhysicalRowBounds)) {
			return invocation.proceed();
		}

		Executor executor = getTargetExecutor((Executor) invocation.getTarget());
		MappedStatement statement = (MappedStatement) args[0];
		Object parameter = args[1];

		MappedStatement countStatement = getCountStatement(statement, rowBounds);
		List<Object> countResult = executor.query(countStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);

		MappedStatement limitStatement = getLimitStatement(statement, rowBounds);
		List<Object> limitResult = executor.query(limitStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);

		return new ArrayBoundList<Object>(rowBounds, extractCountResult(countResult), limitResult);
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
		if (properties.contains(DIALECT_NAME_KEY)) {
			this.dialect = SqlDialect.get(properties.getProperty(DIALECT_NAME_KEY));
		}
	}

	protected int extractCountResult(List<?> result) {
		if (result.isEmpty()) {
			throw new IllegalStateException("No count result");
		}
		if (result.size() > 1) {
			throw new IllegalStateException("Multiple count results");
		}
		return ((Number) (result.get(0))).intValue();
	}

	protected Executor getTargetExecutor(Executor proxy) {
		Executor target = proxy;
		while (true) {
			try {
				InvocationHandler handler = Proxy.getInvocationHandler(target);
				if (!(handler instanceof Plugin)) {
					break;
				}
				target = (Executor) pluginTarget.get(handler);
			} catch (IllegalArgumentException e) {
				break;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		return target;
	}

	protected MappedStatement getLimitStatement(MappedStatement statement, RowBounds rowBounds) {
		Configuration config = statement.getConfiguration();
		return new MappedStatementBuilder(statement)
				.setId(statement.getId() + "!selectLimit")
				.setSqlSource(param -> {
					BoundSqlBuilder builder = new BoundSqlBuilder(config, statement.getBoundSql(param));
					dialect.buildLimitBoundSql(builder, rowBounds);
					return builder.build();
				})
				.build();
	}

	protected MappedStatement getCountStatement(MappedStatement statement, RowBounds rowBounds) {
		Configuration config = statement.getConfiguration();
		return new MappedStatementBuilder(statement)
				.setId(statement.getId() + "!selectCount")
				.setSqlSource(param -> {
					BoundSqlBuilder builder = new BoundSqlBuilder(config, statement.getBoundSql(param));
					dialect.buildCountBoundSql(builder, rowBounds);
					return builder.build();
				})
				.setResultMaps(Lists.newArrayList(
						new ResultMap.Builder(config, statement.getId() + "-Inline", Long.class, Lists.newArrayList()).build()))
				.build();
	}
}
