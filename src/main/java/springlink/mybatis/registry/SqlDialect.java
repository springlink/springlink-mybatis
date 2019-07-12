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

package springlink.mybatis.registry;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.RowBounds;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import springlink.mybatis.sql.SqlCriterion;
import springlink.mybatis.sql.SqlOrderBy;
import springlink.mybatis.sql.SqlProjections;
import springlink.mybatis.sql.SqlUpdate;
import springlink.mybatis.util.Arguments;
import springlink.mybatis.util.BoundSqlBuilder;

public abstract class SqlDialect {
	public static final String RESULT_MAP_ID = "resultMap";
	public static final String SELECT_ENTITY_ID = "selectEntity";
	public static final String SELECT_PROJECTIONS_ID = "selectProjections";
	public static final String SELECT_COUNT_ID = "selectCount";
	public static final String SELECT_EXISTS_ID = "selectExists";
	public static final String UPDATE_ID = "update";
	public static final String DELETE_ID = "delete";
	public static final String INSERT_ID = "insert";

	public static final String CRITERION_KEY = "criterion";
	public static final String ORDER_BY_KEY = "orderBy";
	public static final String ROW_BOUNDS_KEY = "rowBounds";
	public static final String FOR_UPDATE_KEY = "forUpdate";
	public static final String PROJECTIONS_KEY = "projections";
	public static final String UPDATE_KEY = "update";
	public static final String VALUE_KEY = "value";

	private static final ConcurrentMap<String, SqlDialect> dialectMap = Maps.newConcurrentMap();
	private static final ThreadLocal<SqlDialect> currentDialect = new ThreadLocal<>();
	private static final AtomicReference<SqlDialect> defaultDialect = new AtomicReference<>();

	static {
		add("mysql", new MySQLDialect());
		add("h2", new H2Dialect());
		setDefault("mysql");
	}

	public static SqlDialect get(String name) {
		return Strings.isNullOrEmpty(name) ? defaultDialect.get() : dialectMap.get(name);
	}

	public static void add(String name, SqlDialect dialect) {
		Arguments.notEmpty(name, "name");
		Arguments.notNull(dialect, "dialect");
		dialectMap.put(name, dialect);
	}

	public static SqlDialect getDefault() {
		return defaultDialect.get();
	}

	public static void setDefault(String name) {
		defaultDialect.set(get(name));
	}

	public static SqlDialect getCurrent() {
		return Optional.ofNullable(currentDialect.get()).orElseGet(defaultDialect::get);
	}

	public static void setCurrent(String name) {
		currentDialect.set(get(name));
	}

	public abstract String getCriterionSql(SqlContext ctx, String path, SqlCriterion criterion);

	public abstract String getOrderBySql(SqlContext ctx, String path, SqlOrderBy orderBy);

	public abstract String getUpdateSql(SqlContext ctx, String path, SqlUpdate update);

	public abstract String getProjectionsSql(SqlContext ctx, String path, SqlProjections projections);

	public abstract void buildMapper(SqlContext ctx, MapperBuilderAssistant assistant);

	public abstract void buildLimitBoundSql(BoundSqlBuilder builder, RowBounds rowBounds);

	public abstract void buildCountBoundSql(BoundSqlBuilder builder, RowBounds rowBounds);
}
