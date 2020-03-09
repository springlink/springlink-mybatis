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

import java.util.Map;

import org.apache.ibatis.type.TypeAliasRegistry;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import com.github.springlink.mybatis.metadata.SqlEntityMetadata;
import com.github.springlink.mybatis.metadata.SqlMetadata;
import com.github.springlink.mybatis.metadata.SqlPropertyMetadata;
import com.github.springlink.mybatis.sql.SqlCriterion;
import com.github.springlink.mybatis.sql.SqlOrderBy;
import com.github.springlink.mybatis.sql.SqlProjections;
import com.github.springlink.mybatis.sql.SqlReference;
import com.github.springlink.mybatis.sql.SqlUpdate;
import com.github.springlink.mybatis.util.Asserts;

public final class SqlContext {
	public static final String DEFAULT_PATH = "ctx";

	private final Map<String, EntityInfo> entityInfoMap = Maps.newHashMap();
	private final Map<String, PropertyInfo> propertyInfoMap = Maps.newHashMap();
	private final Map<String, Object> objectMap = Maps.newHashMap();

	private final SqlDialect dialect;
	private final TypeAliasRegistry typeAliasRegistry;
	private final String rootPath;

	public SqlContext(SqlDialect dialect, TypeAliasRegistry typeAliasRegistry) {
		this(dialect, typeAliasRegistry, DEFAULT_PATH);
	}

	public SqlContext(SqlDialect dialect, TypeAliasRegistry typeAliasRegistry, String rootPath) {
		Asserts.notNull(dialect, "dialect");
		Asserts.notNull(typeAliasRegistry, "typeAliasRegistry");
		Asserts.notEmpty(rootPath, "rootPath");
		this.dialect = dialect;
		this.typeAliasRegistry = typeAliasRegistry;
		this.rootPath = rootPath;
	}

	public void bind(String expression) {
		Asserts.notEmpty(expression, "expression");
		for (String element : expression.split(";")) {
			String[] parts = element.split(":", 2);
			if (parts.length == 0) {
				throw new IllegalArgumentException("Invalid bind expression: " + element);
			}
			String name = (parts.length == 2 ? Strings.emptyToNull(parts[0].trim()) : null);
			String[] defs = (parts.length == 2 ? parts[1] : parts[0]).split("#");
			if (defs.length == 0) {
				throw new IllegalArgumentException("Invalid bind expression: " + element);
			}
			String typeAlias = defs[0].trim();
			String alias = defs.length == 2 ? Strings.emptyToNull(defs[1].trim()) : null;
			if (typeAlias.isEmpty()) {
				throw new IllegalArgumentException("Invalid bind expression: " + element);
			}
			putEntity(name, typeAlias, alias);
		}
	}

	public String sql(String name) {
		Asserts.notEmpty(name, "name");
		Object obj = getObject(name);
		if (obj == null) {
			return "";
		}
		String path = getObjectPath(name);
		if (obj instanceof SqlCriterion) {
			return dialect.getCriterionSql(this, path, (SqlCriterion) obj);
		} else if (obj instanceof SqlOrderBy) {
			return dialect.getOrderBySql(this, path, (SqlOrderBy) obj);
		} else if (obj instanceof SqlUpdate) {
			return dialect.getUpdateSql(this, path, (SqlUpdate) obj);
		} else if (obj instanceof SqlProjections) {
			return dialect.getProjectionsSql(this, path, (SqlProjections) obj);
		} else {
			throw new IllegalArgumentException("Illegal object type " + obj.getClass().getName() + ": [" + name + "]");
		}
	}

	public SqlContext putEntity(String typeAlias, String alias) {
		Asserts.notEmpty(typeAlias, "typeAlias");
		return putEntity(null, typeAlias, alias);
	}

	public SqlContext putEntity(String name, String typeAlias, String alias) {
		Asserts.notEmpty(typeAlias, "typeAlias");
		return putEntity(name, typeAliasRegistry.resolveAlias(typeAlias), alias);
	}

	public SqlContext putEntity(Class<?> entityType, String alias) {
		Asserts.notNull(entityType, "entityType");
		return putEntity(null, entityType, alias);
	}

	public SqlContext putEntity(String name, Class<?> entityType, String alias) {
		Asserts.notNull(entityType, "entityType");
		entityInfoMap.put(Strings.nullToEmpty(name), new EntityInfo(SqlMetadata.forEntityType(entityType), alias));
		return this;
	}

	public SqlContext putObject(String name, Object obj) {
		Asserts.notEmpty(name, "name");
		objectMap.put(name, obj);
		return this;
	}

	public SqlContext putObjects(Map<String, ?> objs) {
		objectMap.putAll(objs);
		return this;
	}

	public String getObjectPath(String name) {
		Asserts.notEmpty(name, "name");
		return rootPath + ".objects." + name;
	}

	public Object getObject(String name) {
		Asserts.notEmpty(name, "name");
		return objectMap.get(name);
	}

	public Map<String, Object> getObjects() {
		return objectMap;
	}

	public SqlDialect getDialect() {
		return dialect;
	}

	public TypeAliasRegistry getTypeAliasRegistry() {
		return typeAliasRegistry;
	}

	public String getRootPath() {
		return rootPath;
	}

	public SqlEntityMetadata getEntity() {
		return getEntity(null);
	}

	public SqlEntityMetadata getEntity(String name) {
		return getEntityInfo(name).entity;
	}

	public String getTableAlias() {
		return getTableAlias(null);
	}

	public String getTableAlias(String name) {
		return getEntityInfo(name).alias;
	}

	public SqlPropertyMetadata getProperty(String property) {
		Asserts.notEmpty(property, "property");
		return getPropertyInfo(property).property;
	}

	public SqlEntityMetadata getPropertyEntity(String property) {
		Asserts.notEmpty(property, "property");
		return getPropertyInfo(property).entity;
	}

	public String getColumnAlias(String property) {
		Asserts.notEmpty(property, "property");
		return getPropertyInfo(property).alias;
	}

	private EntityInfo getEntityInfo(String name) {
		String key = Strings.nullToEmpty(name);
		EntityInfo info = entityInfoMap.get(key);
		if (info == null) {
			if (key.isEmpty()) {
				throw new IllegalArgumentException("No default entity specified");
			} else {
				throw new IllegalArgumentException("No such entity specified: " + name);
			}
		}
		return info;
	}

	private PropertyInfo getPropertyInfo(String property) {
		PropertyInfo info = propertyInfoMap.computeIfAbsent(property, p -> {
			SqlReference ref = SqlReference.of(property);
			SqlPropertyMetadata pm = null;
			String name = "";
			EntityInfo er = getEntityInfo(null);
			while (ref != null && ref.getName() == null) {
				pm = er.entity.getProperty(ref.getProperty());
				ref = pm.getReference() != null ? SqlReference.of(pm.getReference()) : null;
			}
			if (ref != null) {
				name = ref.getName();
				er = getEntityInfo(name);
				while (ref != null && ref.getName().equals(name)) {
					pm = er.entity.getProperty(ref.getProperty());
					ref = pm.getReference() != null ? SqlReference.of(pm.getReference()) : null;
				}
			}
			return ref == null ? new PropertyInfo(er.entity, er.alias, pm) : null;
		});
		if (info == null) {
			throw new IllegalArgumentException("Unresolvable property: " + property);
		}
		return info;
	}

	private static class EntityInfo {
		final SqlEntityMetadata entity;
		final String alias;

		private EntityInfo(SqlEntityMetadata entity, String alias) {
			this.entity = entity;
			this.alias = alias;
		}
	}

	private static class PropertyInfo extends EntityInfo {
		final SqlPropertyMetadata property;

		private PropertyInfo(SqlEntityMetadata entity, String alias, SqlPropertyMetadata property) {
			super(entity, alias);
			this.property = property;
		}
	}
}
