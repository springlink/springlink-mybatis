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

package springlink.mybatis.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import springlink.mybatis.annotation.SqlCache;
import springlink.mybatis.annotation.SqlCacheRef;
import springlink.mybatis.annotation.SqlEntity;
import springlink.mybatis.annotation.SqlIgnore;
import springlink.mybatis.annotation.SqlJoin;
import springlink.mybatis.annotation.SqlProperty;
import springlink.mybatis.annotation.strategy.NameStrategy;
import springlink.mybatis.sql.SqlCriterion;
import springlink.mybatis.sql.SqlReference;
import springlink.mybatis.util.Arguments;
import springlink.mybatis.util.BeanMetadata;

public final class SqlMetadata {
	private static final ConcurrentMap<Class<?>, SqlEntityMetadata> instanceCache = Maps.newConcurrentMap();

	private SqlMetadata() {
	}

	public static SqlEntityMetadata forEntityType(Class<?> entityType) {
		Arguments.notNull(entityType, "entityType");
		return instanceCache.computeIfAbsent(entityType, SqlMetadata::resolveEntity);
	}

	static SqlEntityMetadata resolveEntity(Class<?> entityType) {
		SqlEntity sqlEntity = entityType.getAnnotation(SqlEntity.class);
		if (sqlEntity == null) {
			throw new IllegalArgumentException("No @SqlEntity present: " + entityType.getName());
		}

		SqlCacheMetadata cache = resolveCache(entityType);
		String cacheRef = resolveCacheRef(entityType);
		if (cache != null && cacheRef != null) {
			throw new IllegalArgumentException("Both @SqlCache and @SqlCacheRef found: " + entityType.getName());
		}

		NameStrategy nameStrategy;
		try {
			nameStrategy = sqlEntity.nameStrategy().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("SqlNameStrategy instantiation failed", e);
		}

		List<SqlJoinMetadata> joins = resolveJoins(entityType);
		List<SqlPropertyMetadata> properties = resolveProperties(entityType, nameStrategy);

		String table = Strings.emptyToNull(sqlEntity.value());
		if (table == null) {
			table = nameStrategy.getDefaultTable(entityType);
		}
		String schema = Strings.emptyToNull(sqlEntity.schema());
		String catalog = Strings.emptyToNull(sqlEntity.catalog());
		return new SqlEntityMetadata(entityType, table, schema, catalog, cacheRef, cache, joins, properties);
	}

	static String resolveCacheRef(Class<?> entityType) {
		SqlCacheRef sqlCacheRef = entityType.getAnnotation(SqlCacheRef.class);
		if (sqlCacheRef != null) {
			Class<?> value = sqlCacheRef.value();
			String namespace = sqlCacheRef.namespace();
			if (!namespace.isEmpty() && value != Object.class) {
				throw new IllegalArgumentException("Both value and namespace are set on @SqlCacheRef: "
						+ entityType.getName());
			}
			if (!namespace.isEmpty()) {
				return namespace;
			}
			if (value != Object.class) {
				return value.getName();
			}
		}
		return null;
	}

	static SqlCacheMetadata resolveCache(Class<?> beanType) {
		SqlCache sqlCache = beanType.getAnnotation(SqlCache.class);
		if (sqlCache != null) {
			Properties properties = new Properties();
			for (SqlCache.Property sqlCacheProperty : sqlCache.properties()) {
				properties.setProperty(sqlCacheProperty.name(), sqlCacheProperty.value());
			}
			Long flushInterval = sqlCache.flushInterval() > 0 ? sqlCache.flushInterval() : null;
			Integer size = sqlCache.size() > 0 ? sqlCache.size() : null;
			return new SqlCacheMetadata(sqlCache.implementation(), sqlCache.eviction(), flushInterval, size,
					sqlCache.readWrite(), sqlCache.blocking(), properties);
		}
		return null;
	}

	static List<SqlPropertyMetadata> resolveProperties(Class<?> entityType, NameStrategy nameStrategy) {
		BeanMetadata metaBeanType = BeanMetadata.forBeanType(entityType);
		List<SqlPropertyMetadata> properties = Lists.newArrayList();
		for (String name : metaBeanType.getPropertyNames()) {
			SqlIgnore sqlIgnore = metaBeanType.getPropertyAnnotation(name, SqlIgnore.class);
			if (sqlIgnore != null && sqlIgnore.value()) {
				continue;
			}
			Class<?> type = metaBeanType.getPropertyType(name);
			SqlProperty sqlProperty = metaBeanType.getPropertyAnnotation(name, SqlProperty.class);
			if (sqlProperty != null) {
				String column = sqlProperty.column();
				if (column.isEmpty()) {
					column = nameStrategy.getDefaultColumn(entityType, name);
				}
				properties.add(new SqlPropertyMetadata(name, ImmutableSet.copyOf(sqlProperty.aliases()), type, column,
						Strings.emptyToNull(sqlProperty.reference()), sqlProperty.id(), sqlProperty.generated(),
						sqlProperty.jdbcType(), sqlProperty.typeHandler()));
			} else {
				properties.add(new SqlPropertyMetadata(name, ImmutableSet.of(), type,
						nameStrategy.getDefaultColumn(entityType, name), null, false, false, null, null));
			}
		}
		return properties;
	}

	static List<SqlJoinMetadata> resolveJoins(Class<?> entityType) {
		Map<String, SqlJoinMetadata> joinMap = Maps.newHashMap();
		for (Class<?> cls = entityType; cls != Object.class; cls = cls.getSuperclass()) {
			for (Field field : cls.getDeclaredFields()) {
				SqlJoin sqlJoin = field.getDeclaredAnnotation(SqlJoin.class);
				if (sqlJoin == null) {
					continue;
				}
				int modifier = field.getModifiers();
				if (!Modifier.isStatic(modifier) || !Modifier.isFinal(modifier)
						|| !SqlCriterion.class.isAssignableFrom(field.getType())) {
					throw new IllegalArgumentException("Invalid join declaration, field must be static, final and"
							+ " instanceof SqlCriterion: " + entityType.getName() + "." + field.getName());
				}
				SqlCriterion criterion;
				try {
					field.setAccessible(true);
					criterion = (SqlCriterion) field.get(null);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Failed to read field:" + field.getName(), e);
				}
				String name = Strings.emptyToNull(sqlJoin.name());
				if (name == null) {
					name = field.getName();
				}
				if (!joinMap.containsKey(name)) {
					joinMap.put(name, new SqlJoinMetadata(name, sqlJoin.value(), sqlJoin.type(), criterion));
				}
			}
		}
		MutableGraph<String> joinNameGraph = GraphBuilder.directed()
				.allowsSelfLoops(false)
				.expectedNodeCount(joinMap.size())
				.build();
		for (Map.Entry<String, SqlJoinMetadata> entry : joinMap.entrySet()) {
			String joinName = entry.getKey();
			SqlJoinMetadata join = entry.getValue();
			for (String ref : join.getCriterion().getReferenceNames()) {
				String joinRefName = SqlReference.of(ref).getName();
				if (joinRefName != null && !joinRefName.equals(joinName)) {
					if (!joinMap.containsKey(joinRefName)) {
						throw new IllegalArgumentException(String.format("Unresolvable join [%s]->[%s] in [%s]",
								joinName, joinRefName, entityType.getName()));
					}
					joinNameGraph.putEdge(joinName, joinRefName);
				}
			}
			joinNameGraph.addNode(joinName);
		}
		List<SqlJoinMetadata> sortedJoins = Lists.newArrayList();
		while (!joinNameGraph.nodes().isEmpty()) {
			boolean found = false;
			for (String joinName : joinNameGraph.nodes()) {
				if (joinNameGraph.inDegree(joinName) == 0) {
					sortedJoins.add(joinMap.get(joinName));
					joinNameGraph.removeNode(joinName);
					found = true;
					break;
				}
			}
			if (!found) {
				throw new IllegalArgumentException("Cycle join reference found: " + entityType.getName());
			}
		}
		return sortedJoins;
	}
}
