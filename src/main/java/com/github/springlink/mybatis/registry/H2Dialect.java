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

package com.github.springlink.mybatis.registry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;

import com.google.common.collect.Lists;

import com.github.springlink.mybatis.metadata.SqlCacheMetadata;
import com.github.springlink.mybatis.metadata.SqlEntityMetadata;
import com.github.springlink.mybatis.metadata.SqlJoinMetadata;
import com.github.springlink.mybatis.metadata.SqlPropertyMetadata;
import com.github.springlink.mybatis.sql.SqlCriterion;
import com.github.springlink.mybatis.sql.SqlOrderBy;
import com.github.springlink.mybatis.sql.SqlProjections;
import com.github.springlink.mybatis.sql.SqlReference;
import com.github.springlink.mybatis.sql.SqlUpdate;
import com.github.springlink.mybatis.util.BoundSqlBuilder;

public class H2Dialect extends SqlDialect {
	@Override
	public String getCriterionSql(SqlContext ctx, String path, SqlCriterion criterion) {
		if (criterion instanceof SqlCriterion.Condition) {
			return getConditionSql(ctx, path, (SqlCriterion.Condition) criterion);
		}
		if (criterion instanceof SqlCriterion.Constant) {
			return getConstantSql(ctx, path, (SqlCriterion.Constant) criterion);
		}
		if (criterion instanceof SqlCriterion.Junction) {
			return getJunctionSql(ctx, path, (SqlCriterion.Junction) criterion);
		}
		throw new UnsupportedOperationException("Unknown criterion class: " + criterion.getClass().getName());
	}

	@Override
	public String getOrderBySql(SqlContext ctx, String path, SqlOrderBy orderBy) {
		return orderBy.asList().stream()
				.map(order -> getColumnSql(ctx, order.getProperty()) + (order.isDescending() ? " DESC" : " ASC"))
				.collect(Collectors.joining(","));
	}

	@Override
	public String getUpdateSql(SqlContext ctx, String path, SqlUpdate update) {
		List<String> items = Lists.newArrayList();
		List<SqlUpdate.Set> sets = update.asList();
		for (int i = 0; i < sets.size(); ++i) {
			SqlUpdate.Set set = sets.get(i);
			String property = set.getProperty();
			List<Object> args = set.getArgs();
			String column = getColumnSql(ctx, property);
			String setPath = path + ".sets[" + i + "]";
			IntFunction<String> argument = index -> {
				return getArgumentSql(ctx, property, setPath + ".args[" + index + "]", args.get(index));
			};

			StringBuilder sql = new StringBuilder();
			switch (set.getType()) {
			case SET:
				sql.append(column).append(" = ").append(argument.apply(0));
				break;
			case NULLIFY:
				sql.append(column).append(" = NULL");
				break;
			case ADD:
				sql.append(column).append(" = ").append(column).append(" + ").append(argument.apply(0));
				break;
			case SUBTRACT:
				sql.append(column).append(" = ").append(column).append(" - ").append(argument.apply(0));
				break;
			}
			items.add(sql.toString());
		}
		return String.join(",", items);
	}

	@Override
	public String getProjectionsSql(SqlContext ctx, String path, SqlProjections projections) {
		List<String> items = Lists.newArrayList();
		for (Map.Entry<String, SqlProjections.Projection> entry : projections.asMap().entrySet()) {
			String name = entry.getKey();
			SqlProjections.Projection projection = entry.getValue();
			String column = getColumnSql(ctx, projection.getProperty());

			StringBuilder sql = new StringBuilder();
			switch (projection.getType()) {
			case PROPERTY:
				sql.append(column);
				break;
			case DISTINCT:
				sql.append("DISTINCT(").append(column).append(")");
				break;
			case COUNT:
				sql.append("COUNT(").append(column).append(")");
				break;
			case COUNT_DISTINCT:
				sql.append("COUNT(DISTINCT ").append(column).append(")");
				break;
			case AVG:
				sql.append("AVG(").append(column).append(")");
				break;
			case MAX:
				sql.append("MAX(").append(column).append(")");
				break;
			case MIN:
				sql.append("MIN(").append(column).append(")");
				break;
			case SUM:
				sql.append("SUM(").append(column).append(")");
				break;
			}
			sql.append(" AS ").append(qoute(name));
			items.add(sql.toString());
		}
		return String.join(",", items);
	}

	@Override
	public void buildMapper(SqlContext ctx, MapperBuilderAssistant assistant) {
		SqlEntityMetadata em = ctx.getEntity();
		buildResultMap(assistant, em);
		buildCache(assistant, em);
		buildSelectEntityStatement(ctx, assistant);
		buildSelectProjectionsStatement(ctx, assistant);
		buildSelectCountStatement(ctx, assistant);
		buildSelectExistsStatement(ctx, assistant);
		buildUpdateStatement(ctx, assistant);
		buildDeleteStatement(ctx, assistant);
		buildInsertStatement(ctx, assistant);
	}

	@Override
	public void buildLimitBoundSql(BoundSqlBuilder builder, RowBounds rowBounds) {
		Configuration cfg = builder.getConfiguration();
		builder.setSql("SELECT * FROM (" + builder.getSql() + ") __subquery LIMIT ?, ?");
		builder.addParameterMapping(new ParameterMapping.Builder(cfg, "_rowBounds.offset", int.class).build());
		builder.addParameterMapping(new ParameterMapping.Builder(cfg, "_rowBounds.limit", int.class).build());
		builder.putAdditionalParameter("_rowBounds", rowBounds);
	}

	@Override
	public void buildCountBoundSql(BoundSqlBuilder builder, RowBounds rowBounds) {
		builder.setSql("SELECT COUNT(*) FROM (" + builder.getSql() + ") __subquery");
		builder.putAdditionalParameter("_rowBounds", rowBounds);
	}

	protected String qoute(String content) {
		return "`" + content + "`";
	}

	protected String join(String separator, String... parts) {
		return Stream.of(parts).filter(Objects::nonNull).collect(Collectors.joining(separator));
	}

	protected void buildResultMap(MapperBuilderAssistant assistant, SqlEntityMetadata em) {
		assistant.addResultMap(
				RESULT_MAP_ID /* id */,
				em.getType() /* type */,
				null /* extend */,
				null /* discriminator */,
				em.getProperties().stream()
						.map(pm -> assistant.buildResultMapping(
								pm.getType() /* resultType */,
								pm.getName() /* property */,
								pm.getColumn() /* column */,
								pm.getType() /* javaType */,
								pm.getJdbcType() /* jdbcType */,
								null /* nestedSelect */,
								null /* nestedResultMap */,
								null /* notNullColumn */,
								null /* columnPrefix */,
								pm.getTypeHandler() /* typeHandler */,
								pm.getResultFlags() /* flags */))
						.collect(Collectors.toList()) /* resultMappings */,
				false /* autoMapping */);
	}

	protected void buildCache(MapperBuilderAssistant assistant, SqlEntityMetadata em) {
		if (em.getCacheRef() != null) {
			assistant.useCacheRef(em.getCacheRef());
		} else {
			SqlCacheMetadata cm = em.getCache();
			if (cm != null) {
				assistant.useNewCache(cm.getImplementation(), cm.getEviction(), cm.getFlushInterval(), cm.getSize(),
						cm.isReadWrite(), cm.isBlocking(), cm.getProperties());
			}
		}
	}

	protected void buildSelectEntityStatement(SqlContext ctx, MapperBuilderAssistant assistant) {
		Configuration config = assistant.getConfiguration();
		LanguageDriver langDrv = config.getLanguageDriver(null);
		String script = String.format("<script>SELECT %s FROM %s %s %s %s %s</script>",
				getColumnsSql(ctx), getJoinedTableSql(ctx), getWhereSql(ctx), getOrderBySql(ctx),
				getLimitSql(ctx), getForUpdateSql(ctx));
		assistant.addMappedStatement(
				SELECT_ENTITY_ID /* id */,
				langDrv.createSqlSource(config, script, Map.class) /* sqlSource */,
				StatementType.PREPARED /* statementType */,
				SqlCommandType.SELECT /* sqlCommandType */,
				null /* fetchSize */,
				null /* timeout */,
				null /* parameterMap */,
				Map.class /* parameterType */,
				RESULT_MAP_ID /* resultMap */,
				null /* resultType */,
				null /* resultSetType */,
				false /* flushCache */,
				true /* useCache */,
				false /* resultOrdered */,
				NoKeyGenerator.INSTANCE /* keyGenerator */,
				null /* keyProperty */,
				null /* keyColumn */,
				null /* databaseId */,
				langDrv /* lang */);
	}

	protected void buildSelectProjectionsStatement(SqlContext ctx, MapperBuilderAssistant assistant) {
		Configuration config = assistant.getConfiguration();
		LanguageDriver langDrv = config.getLanguageDriver(null);
		String script = String.format("<script>SELECT %s FROM %s %s %s %s %s</script>",
				getProjectionsSql(ctx), getJoinedTableSql(ctx), getWhereSql(ctx), getOrderBySql(ctx),
				getLimitSql(ctx), getForUpdateSql(ctx));
		assistant.addMappedStatement(
				SELECT_PROJECTIONS_ID /* id */,
				langDrv.createSqlSource(config, script, Map.class) /* sqlSource */,
				StatementType.PREPARED /* statementType */,
				SqlCommandType.SELECT /* sqlCommandType */,
				null /* fetchSize */,
				null /* timeout */,
				null /* parameterMap */,
				Map.class /* parameterType */,
				null /* resultMap */,
				Map.class /* resultType */,
				null /* resultSetType */,
				false /* flushCache */,
				true /* useCache */,
				false /* resultOrdered */,
				NoKeyGenerator.INSTANCE /* keyGenerator */,
				null /* keyProperty */,
				null /* keyColumn */,
				null /* databaseId */,
				langDrv /* lang */);
	}

	protected void buildSelectCountStatement(SqlContext ctx, MapperBuilderAssistant assistant) {
		Configuration config = assistant.getConfiguration();
		LanguageDriver langDrv = config.getLanguageDriver(null);
		String script = String.format("<script>SELECT COUNT(*) FROM %s %s</script>",
				getJoinedTableSql(ctx), getWhereSql(ctx));
		assistant.addMappedStatement(
				SELECT_COUNT_ID /* id */,
				langDrv.createSqlSource(config, script, Map.class) /* sqlSource */,
				StatementType.PREPARED /* statementType */,
				SqlCommandType.SELECT /* sqlCommandType */,
				null /* fetchSize */,
				null /* timeout */,
				null /* parameterMap */,
				Map.class /* parameterType */,
				null /* resultMap */,
				Long.class /* resultType */,
				null /* resultSetType */,
				false /* flushCache */,
				true /* useCache */,
				false /* resultOrdered */,
				NoKeyGenerator.INSTANCE /* keyGenerator */,
				null /* keyProperty */,
				null /* keyColumn */,
				null /* databaseId */,
				langDrv /* lang */);
	}

	protected void buildSelectExistsStatement(SqlContext ctx, MapperBuilderAssistant assistant) {
		Configuration config = assistant.getConfiguration();
		LanguageDriver langDrv = config.getLanguageDriver(null);
		String script = String.format("<script>SELECT EXISTS(SELECT 1 FROM %s %s)</script>",
				getJoinedTableSql(ctx), getWhereSql(ctx));
		assistant.addMappedStatement(
				SELECT_EXISTS_ID /* id */,
				langDrv.createSqlSource(config, script, Map.class) /* sqlSource */,
				StatementType.PREPARED /* statementType */,
				SqlCommandType.SELECT /* sqlCommandType */,
				null /* fetchSize */,
				null /* timeout */,
				null /* parameterMap */,
				Map.class /* parameterType */,
				null /* resultMap */,
				Boolean.class /* resultType */,
				null /* resultSetType */,
				false /* flushCache */,
				true /* useCache */,
				false /* resultOrdered */,
				NoKeyGenerator.INSTANCE /* keyGenerator */,
				null /* keyProperty */,
				null /* keyColumn */,
				null /* databaseId */,
				langDrv /* lang */);
	}

	protected void buildDeleteStatement(SqlContext ctx, MapperBuilderAssistant assistant) {
		Configuration config = assistant.getConfiguration();
		LanguageDriver langDrv = config.getLanguageDriver(null);
		String script = String.format("<script>DELETE FROM %s %s</script>",
				getTableSql(ctx), getWhereSql(ctx));
		assistant.addMappedStatement(
				DELETE_ID /* id */,
				langDrv.createSqlSource(config, script, Map.class) /* sqlSource */,
				StatementType.PREPARED /* statementType */,
				SqlCommandType.DELETE /* sqlCommandType */,
				null /* fetchSize */,
				null /* timeout */,
				null /* parameterMap */,
				Map.class /* parameterType */,
				null /* resultMap */,
				null /* resultType */,
				null /* resultSetType */,
				true /* flushCache */,
				false /* useCache */,
				false /* resultOrdered */,
				NoKeyGenerator.INSTANCE /* keyGenerator */,
				null /* keyProperty */,
				null /* keyColumn */,
				null /* databaseId */,
				langDrv /* lang */);
	}

	protected void buildUpdateStatement(SqlContext ctx, MapperBuilderAssistant assistant) {
		Configuration config = assistant.getConfiguration();
		LanguageDriver langDrv = config.getLanguageDriver(null);
		String script = String.format("<script>UPDATE %s %s %s</script>",
				getTableSql(ctx), getSetSql(ctx), getWhereSql(ctx));
		assistant.addMappedStatement(
				UPDATE_ID /* id */,
				langDrv.createSqlSource(config, script, Map.class) /* sqlSource */,
				StatementType.PREPARED /* statementType */,
				SqlCommandType.UPDATE /* sqlCommandType */,
				null /* fetchSize */,
				null /* timeout */,
				null /* parameterMap */,
				Map.class /* parameterType */,
				null /* resultMap */,
				null /* resultType */,
				null /* resultSetType */,
				true /* flushCache */,
				false /* useCache */,
				false /* resultOrdered */,
				NoKeyGenerator.INSTANCE /* keyGenerator */,
				null /* keyProperty */,
				null /* keyColumn */,
				null /* databaseId */,
				langDrv /* lang */);
	}

	protected void buildInsertStatement(SqlContext ctx, MapperBuilderAssistant assistant) {
		boolean useGeneratedKeys = false;
		List<String> keyColumns = Lists.newArrayList();
		List<String> keyProperties = Lists.newArrayList();

		String valuePath = ctx.getRootPath() + ".objects." + VALUE_KEY;
		List<String> columns = Lists.newArrayList();
		List<String> values = Lists.newArrayList();
		for (SqlPropertyMetadata pm : ctx.getEntity().getProperties()) {
			if (pm.getReference() == null) {
				columns.add(qoute(pm.getColumn()));
				if (pm.isGenerated()) {
					keyColumns.add(pm.getColumn());
					keyProperties.add(valuePath + "." + pm.getName());
					useGeneratedKeys = true;

					values.add("default");
				} else {
					values.add(pm.getParameterSql(valuePath + "." + pm.getName()));
				}
			}
		}

		Configuration config = assistant.getConfiguration();
		LanguageDriver langDrv = config.getLanguageDriver(null);
		String script = String.format("<script>INSERT INTO %s(%s) VALUES(%s)</script>",
				getTableSql(ctx, null), String.join(",", columns), String.join(",", values));
		assistant.addMappedStatement(
				INSERT_ID /* id */,
				langDrv.createSqlSource(config, script, Map.class) /* sqlSource */,
				StatementType.PREPARED /* statementType */,
				SqlCommandType.INSERT /* sqlCommandType */,
				null /* fetchSize */,
				null /* timeout */,
				null /* parameterMap */,
				Map.class /* parameterType */,
				null /* resultMap */,
				null /* resultType */,
				null /* resultSetType */,
				true /* flushCache */,
				false /* useCache */,
				false /* resultOrdered */,
				useGeneratedKeys ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE /* keyGenerator */,
				useGeneratedKeys ? String.join(",", keyProperties) : null /* keyProperty */,
				useGeneratedKeys ? String.join(",", keyColumns) : null /* keyColumn */,
				null /* databaseId */,
				langDrv /* lang */);
	}

	protected String getTableSql(SqlContext ctx, String name) {
		return qoute(ctx.getEntity(name).getTable());
	}

	protected String getColumnSql(SqlContext ctx, String property) {
		return join(".", ctx.getColumnAlias(property), qoute(ctx.getProperty(property).getColumn()));
	}

	protected String getArgumentSql(SqlContext ctx, String property, String path, Object arg) {
		if (arg instanceof SqlReference) {
			return getColumnSql(ctx, ((SqlReference) arg).toString());
		} else {
			return ctx.getProperty(property).getParameterSql(path);
		}
	}

	protected String getConditionSql(SqlContext ctx, String path, SqlCriterion.Condition condition) {
		String property = condition.getProperty();
		List<Object> args = condition.getArgs();
		String column = getColumnSql(ctx, property);
		IntFunction<String> argument = index -> {
			return getArgumentSql(ctx, property, path + ".args[" + index + "]", args.get(index));
		};
		StringBuilder sql = new StringBuilder();
		switch (condition.getType()) {
		case EQ:
			sql.append(column).append(" = ").append(argument.apply(0));
			break;
		case NE:
			sql.append(column).append(" != ").append(argument.apply(0));
			break;
		case GT:
			sql.append(column).append(" > ").append(argument.apply(0));
			break;
		case GE:
			sql.append(column).append(" >= ").append(argument.apply(0));
			break;
		case LT:
			sql.append(column).append(" < ").append(argument.apply(0));
			break;
		case LE:
			sql.append(column).append(" <= ").append(argument.apply(0));
			break;
		case IS_NOT_NULL:
			sql.append(column).append(" IS NOT NULL");
			break;
		case IS_NULL:
			sql.append(column).append(" IS NULL");
			break;
		case LIKE:
			sql.append(column).append(" LIKE ").append(argument.apply(0));
			break;
		case LIKE_ESC:
			sql.append(column).append(" LIKE ").append(argument.apply(0)).append(" ESCAPE ").append(argument.apply(1));
			break;
		case IN:
			sql.append(column).append(" IN(")
					.append(IntStream.range(0, args.size()).mapToObj(argument::apply).collect(Collectors.joining(",")))
					.append(")");
			break;
		case BETWEEN:
			sql.append(column).append(" BETWEEN ").append(argument.apply(0)).append(" AND ").append(argument.apply(1));
			break;
		default:
			throw new UnsupportedOperationException("Unknown condition type: " + condition.getType().name());
		}
		return sql.toString();
	}

	protected String getConstantSql(SqlContext ctx, String path, SqlCriterion.Constant constant) {
		switch (constant.getType()) {
		case FALSE:
			return " (1=0) ";
		case TRUE:
			return " (1=1) ";
		default:
			throw new UnsupportedOperationException("Unknown constant type: " + constant.getType().name());
		}
	}

	protected String getJunctionSql(SqlContext ctx, String path, SqlCriterion.Junction junction) {
		UnaryOperator<String> encloser = s -> s.isEmpty() ? "" : "(" + s + ")";
		List<SqlCriterion> criteria = junction.getCriteria();
		switch (junction.getType()) {
		case AND:
			return IntStream.range(0, criteria.size())
					.mapToObj(i -> getCriterionSql(ctx, path + ".criteria[" + i + "]", criteria.get(i)))
                    .filter(sql -> !sql.isEmpty())
					.collect(Collectors.collectingAndThen(Collectors.joining(" AND "), encloser));
		case OR:
			return IntStream.range(0, criteria.size())
					.mapToObj(i -> getCriterionSql(ctx, path + ".criteria[" + i + "]", criteria.get(i)))
                    .filter(sql -> !sql.isEmpty())
					.collect(Collectors.collectingAndThen(Collectors.joining(" OR "), encloser));
		case NOT:
            return Optional.of(getCriterionSql(ctx, path + ".criteria[0]", criteria.get(0)))
                    .filter(sql -> !sql.isEmpty())
                    .map(sql -> "NOT(" + sql + ")")
                    .orElse("");
		default:
			throw new UnsupportedOperationException("Unknown junction type: " + junction.getType().name());
		}
	}
	
	protected String getTableSql(SqlContext ctx) {
		return join(" ", getTableSql(ctx, null), ctx.getTableAlias());
	}

	protected String getJoinedTableSql(SqlContext ctx) {
		StringBuilder sqlBuilder = new StringBuilder(join(" ", getTableSql(ctx, null), ctx.getTableAlias()));
		for (SqlJoinMetadata join : ctx.getEntity().getJoins()) {
			switch (join.getJoinType()) {
			case FULL_OUTER:
				sqlBuilder.append(" FULL JOIN ");
				break;
			case INNER:
				sqlBuilder.append(" INNER JOIN ");
				break;
			case LEFT_OUTER:
				sqlBuilder.append(" LEFT JOIN ");
				break;
			case RIGHT_OUTER:
				sqlBuilder.append(" RIGHT JOIN ");
				break;
			}
			String criterionName = "joinCriterion_" + join.getName();
			ctx.putObject(criterionName, join.getCriterion());
			sqlBuilder.append(String.format("%s %s ON %s",
					getTableSql(ctx, join.getName()), ctx.getTableAlias(join.getName()), ctx.sql(criterionName)));
		}
		return sqlBuilder.toString();
	}

	protected String getWhereSql(SqlContext ctx) {
		return String.format("<trim prefix=\" WHERE\">${%s.sql('%s')}</trim>", ctx.getRootPath(), CRITERION_KEY);
	}

	protected String getOrderBySql(SqlContext ctx) {
		return String.format("<trim prefix=\" ORDER BY\">${%s.sql('%s')}</trim>", ctx.getRootPath(), ORDER_BY_KEY);
	}

	protected String getLimitSql(SqlContext ctx) {
		return String.format("<if test=\"%1$s != null\"> LIMIT #{%1$s.offset}, #{%1$s.limit}</if>",
				ctx.getObjectPath(ROW_BOUNDS_KEY));
	}

	protected String getForUpdateSql(SqlContext ctx) {
		return String.format("<if test=\"%s\"> FOR UPDATE</if>", ctx.getObjectPath(FOR_UPDATE_KEY));
	}

	protected String getProjectionsSql(SqlContext ctx) {
		return String.format("${%s.sql('%s')}", ctx.getRootPath(), PROJECTIONS_KEY);
	}

	protected String getColumnsSql(SqlContext ctx) {
		return ctx.getEntity().getProperties().stream()
				.map(pm -> String.format("%s AS %s", getColumnSql(ctx, pm.getName()), qoute(pm.getColumn())))
				.collect(Collectors.joining(","));
	}

	protected String getSetSql(SqlContext ctx) {
		return String.format("<trim prefix=\" SET\">${%s.sql('%s')}</trim>", ctx.getRootPath(), UPDATE_KEY);
	}
}
