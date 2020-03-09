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

package com.github.springlink.mybatis.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import com.google.common.collect.Maps;

import com.github.springlink.mybatis.registry.SqlContext;
import com.github.springlink.mybatis.registry.SqlDialect;
import com.github.springlink.mybatis.registry.SqlRegistry;
import com.github.springlink.mybatis.sql.SqlCriterion;
import com.github.springlink.mybatis.sql.SqlOrderBy;
import com.github.springlink.mybatis.sql.SqlProjections;
import com.github.springlink.mybatis.sql.SqlUpdate;
import com.github.springlink.mybatis.util.ArrayBoundList;
import com.github.springlink.mybatis.util.Asserts;
import com.github.springlink.mybatis.util.BoundList;

public class DefaultSqlDao implements SqlDao {
	private final SqlRegistry registry;
	private final SqlSession session;

	public DefaultSqlDao(DefaultSqlDao dao) {
		this(dao.registry, dao.session);
	}

	public DefaultSqlDao(SqlRegistry registry, SqlSession session) {
		Asserts.notNull(registry, "registry");
		Asserts.notNull(session, "session");
		this.registry = registry;
		this.session = session;
	}

	@Override
	public SqlRegistry getRegistry() {
		return registry;
	}

	@Override
	public SqlSession getSession() {
		return session;
	}

	@Override
	public <T> Selector<T> select(Class<T> entityType) {
		Asserts.notNull(entityType, "entityType");
		return new SelectorImpl<>(entityType);
	}

	@Override
	public <T> int insert(Class<T> entityType, T value) {
		Asserts.notNull(entityType, "entityType");
		if (value == null) {
			return 0;
		}
		return session.insert(
				applyNamespace(entityType, SqlDialect.INSERT_ID),
				getParameterObject(entityType, ctx -> {
					ctx.putObject(SqlDialect.VALUE_KEY, value);
				}));
	}

	@Override
	public int delete(Class<?> entityType, SqlCriterion criterion) {
		Asserts.notNull(entityType, "entityType");
		return session.delete(
				applyNamespace(entityType, SqlDialect.DELETE_ID),
				getParameterObject(entityType, ctx -> {
					ctx.putObject(SqlDialect.CRITERION_KEY, processCriterion(entityType, criterion));
				}));
	}

	@Override
	public long count(Class<?> entityType, SqlCriterion criterion) {
		Asserts.notNull(entityType, "entityType");
		return session.selectOne(
				applyNamespace(entityType, SqlDialect.SELECT_COUNT_ID),
				getParameterObject(entityType, ctx -> {
					ctx.putObject(SqlDialect.CRITERION_KEY, processCriterion(entityType, criterion));
				}));
	}

	@Override
	public boolean exists(Class<?> entityType, SqlCriterion criterion) {
		Asserts.notNull(entityType, "entityType");
		return session.selectOne(
				applyNamespace(entityType, SqlDialect.SELECT_EXISTS_ID),
				getParameterObject(entityType, ctx -> {
					ctx.putObject(SqlDialect.CRITERION_KEY, processCriterion(entityType, criterion));
				}));
	}

	@Override
	public <T> int update(Class<T> entityType, SqlUpdate update, SqlCriterion criterion) {
		Asserts.notNull(entityType, "entityType");
		if (update == null || update.asList().isEmpty()) {
			return 0;
		}
		return session.update(
				applyNamespace(entityType, SqlDialect.UPDATE_ID),
				getParameterObject(entityType, ctx -> {
					ctx.putObject(SqlDialect.CRITERION_KEY, processCriterion(entityType, criterion));
					ctx.putObject(SqlDialect.UPDATE_KEY, processUpdate(entityType, update));
				}));
	}

	protected <T> T selectOne(Selector<T> selector) {
		return session.selectOne(
				applyNamespace(selector.getEntityType(), SqlDialect.SELECT_ENTITY_ID),
				getParameterObject(selector.getEntityType(), ctx -> {
					ctx.putObject(SqlDialect.CRITERION_KEY, processCriterion(selector));
					ctx.putObject(SqlDialect.ORDER_BY_KEY, processOrderBy(selector));
					ctx.putObject(SqlDialect.FOR_UPDATE_KEY, selector.isForUpdate());
				}));
	}

	protected Object selectOne(Selector<?> selector, SqlProjections projections) {
		Asserts.notNull(projections, "projections");
		return extractResult(session.selectOne(
				applyNamespace(selector.getEntityType(), SqlDialect.SELECT_PROJECTIONS_ID),
				getParameterObject(selector.getEntityType(), ctx -> {
					ctx.putObject(SqlDialect.CRITERION_KEY, processCriterion(selector));
					ctx.putObject(SqlDialect.ORDER_BY_KEY, processOrderBy(selector));
					ctx.putObject(SqlDialect.FOR_UPDATE_KEY, selector.isForUpdate());
					ctx.putObject(SqlDialect.PROJECTIONS_KEY, projections);
				})), projections);
	}

	protected <T> List<T> selectList(Selector<T> selector, RowBounds rowBounds) {
		return session.selectList(
				applyNamespace(selector.getEntityType(), SqlDialect.SELECT_ENTITY_ID),
				getParameterObject(selector.getEntityType(), ctx -> {
					ctx.putObject(SqlDialect.CRITERION_KEY, processCriterion(selector));
					ctx.putObject(SqlDialect.ORDER_BY_KEY, processOrderBy(selector));
					ctx.putObject(SqlDialect.FOR_UPDATE_KEY, selector.isForUpdate());
					ctx.putObject(SqlDialect.ROW_BOUNDS_KEY, rowBounds);
				}));
	}

	protected List<Object> selectList(Selector<?> selector, RowBounds rowBounds, SqlProjections projections) {
		Asserts.notNull(projections, "projections");
		return extractResultList(session.selectList(
				applyNamespace(selector.getEntityType(), SqlDialect.SELECT_PROJECTIONS_ID),
				getParameterObject(selector.getEntityType(), ctx -> {
					ctx.putObject(SqlDialect.CRITERION_KEY, processCriterion(selector));
					ctx.putObject(SqlDialect.ORDER_BY_KEY, processOrderBy(selector));
					ctx.putObject(SqlDialect.FOR_UPDATE_KEY, selector.isForUpdate());
					ctx.putObject(SqlDialect.ROW_BOUNDS_KEY, rowBounds);
					ctx.putObject(SqlDialect.PROJECTIONS_KEY, projections);
				})), projections);
	}

	protected <K, T> Map<K, T> selectMap(Selector<T> selector, String mapKey) {
		return session.selectMap(
				applyNamespace(selector.getEntityType(), SqlDialect.SELECT_ENTITY_ID),
				getParameterObject(selector.getEntityType(), ctx -> {
					ctx.putObject(SqlDialect.CRITERION_KEY, processCriterion(selector));
					ctx.putObject(SqlDialect.FOR_UPDATE_KEY, selector.isForUpdate());
				}), mapKey);
	}

	protected String applyNamespace(Class<?> entityType, String statementId) {
		return entityType.getName() + "." + statementId;
	}

	protected Map<String, Object> getParameterObject(Class<?> entityType, Consumer<SqlContext> consumer) {
		SqlContext ctx = registry.getContext(entityType);
		consumer.accept(ctx);
		Map<String, Object> parameter = Maps.newHashMap();
		parameter.put(ctx.getRootPath(), ctx);
		return parameter;
	}

	protected Object extractResult(Map<String, Object> result, SqlProjections projections) {
		return projections.asMap().size() == 1 ? result.values().iterator().next() : result;
	}

	protected List<Object> extractResultList(List<Map<String, Object>> resultList, SqlProjections projections) {
		return resultList.stream().map(result -> extractResult(result, projections)).collect(Collectors.toList());
	}

	protected SqlCriterion processCriterion(Class<?> entityType, SqlCriterion criterion) {
		return criterion;
	}

	protected SqlOrderBy processOrderBy(Class<?> entityType, SqlOrderBy orderBy) {
		return orderBy;
	}

	protected SqlUpdate processUpdate(Class<?> entityType, SqlUpdate update) {
		return update;
	}

	private SqlCriterion processCriterion(Selector<?> selector) {
		return processCriterion(selector.getEntityType(), selector.getCriterion());
	}

	private SqlOrderBy processOrderBy(Selector<?> selector) {
		return processOrderBy(selector.getEntityType(), selector.getOrderBy());
	}

	private class SelectorImpl<T> implements Selector<T> {
		private final Class<T> entityType;
		private SqlCriterion criterion;
		private SqlOrderBy orderBy;
		private boolean forUpdate;

		SelectorImpl(Class<T> entityType) {
			this.entityType = entityType;
		}

		@Override
		public Class<T> getEntityType() {
			return entityType;
		}

		@Override
		public SqlRegistry getRegistry() {
			return DefaultSqlDao.this.getRegistry();
		}

		@Override
		public SqlSession getSession() {
			return DefaultSqlDao.this.getSession();
		}

		@Override
		public SqlCriterion getCriterion() {
			return criterion;
		}

		@Override
		public SqlOrderBy getOrderBy() {
			return orderBy;
		}

		@Override
		public boolean isForUpdate() {
			return forUpdate;
		}

		@Override
		public Selector<T> where(SqlCriterion criterion) {
			this.criterion = criterion;
			return this;
		}

		@Override
		public Selector<T> orderBy(SqlOrderBy orderBy) {
			this.orderBy = orderBy;
			return this;
		}

		@Override
		public Selector<T> forUpdate(boolean forUpdate) {
			this.forUpdate = forUpdate;
			return this;
		}

		@Override
		public Optional<T> asOne() {
			return Optional.ofNullable(selectOne(this));
		}

		@Override
		@SuppressWarnings("unchecked")
		public <R> Optional<R> asOne(SqlProjections projections) {
			return Optional.ofNullable(selectOne(this, projections)).map(value -> (R) value);
		}

		@Override
		public List<T> asList() {
			return selectList(this, null);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <R> List<R> asList(SqlProjections projections) {
			return (List<R>) selectList(this, null, projections);
		}

		@Override
		public List<T> asList(RowBounds rowBounds) {
			return selectList(this, rowBounds);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <R> List<R> asList(RowBounds rowBounds, SqlProjections projections) {
			return (List<R>) selectList(this, rowBounds, projections);
		}

		@Override
		public BoundList<T> asBoundList(RowBounds rowBounds) {
			return new ArrayBoundList<>(rowBounds, (int) count(entityType, criterion), asList(rowBounds));
		}

		@Override
		public <R> BoundList<R> asBoundList(RowBounds rowBounds, SqlProjections projections) {
			return new ArrayBoundList<>(rowBounds, (int) count(entityType, criterion), asList(rowBounds, projections));
		}

		@Override
		public <K> Map<K, T> asMap(String mapKey) {
			return selectMap(this, mapKey);
		}
	}
}
