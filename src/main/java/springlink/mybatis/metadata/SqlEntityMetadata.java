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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import springlink.mybatis.util.Arguments;

public class SqlEntityMetadata {
	private final Class<?> type;
	private final String table;
	private final String schema;
	private final String catalog;
	private final String cacheRef;
	private final SqlCacheMetadata cache;
	private final List<SqlJoinMetadata> joins;
	private final List<SqlPropertyMetadata> properties;

	private final Map<String, SqlJoinMetadata> joinNameMap;
	private final Map<String, SqlPropertyMetadata> propertyNameMap;
	private final Map<String, SqlPropertyMetadata> propertyAliasMap;

	SqlEntityMetadata(Class<?> type, String table, String schema, String catalog,
			String cacheRef, SqlCacheMetadata cache, List<SqlJoinMetadata> joins,
			List<SqlPropertyMetadata> properties) {
		this.type = type;
		this.table = table;
		this.schema = schema;
		this.catalog = catalog;
		this.cacheRef = cacheRef;
		this.cache = cache;
		this.joins = joins;
		this.properties = properties;

		Map<String, SqlJoinMetadata> joinNameMap = Maps.newHashMap();
		for (SqlJoinMetadata join : joins) {
			joinNameMap.put(join.getName(), join);
		}
		this.joinNameMap = Collections.unmodifiableMap(joinNameMap);

		Map<String, SqlPropertyMetadata> propertyNameMap = Maps.newHashMap();
		Map<String, SqlPropertyMetadata> propertyAliasMap = Maps.newHashMap();
		for (SqlPropertyMetadata property : properties) {
			propertyNameMap.put(property.getName(), property);
			for (String alias : property.getAliases()) {
				propertyAliasMap.put(alias, property);
			}
		}
		this.propertyNameMap = Collections.unmodifiableMap(propertyNameMap);
		this.propertyAliasMap = Collections.unmodifiableMap(propertyAliasMap);
	}

	public Class<?> getType() {
		return type;
	}

	public String getTable() {
		return table;
	}

	public String getSchema() {
		return schema;
	}

	public String getCatalog() {
		return catalog;
	}

	public String getCacheRef() {
		return cacheRef;
	}

	public SqlCacheMetadata getCache() {
		return cache;
	}

	public List<SqlJoinMetadata> getJoins() {
		return joins;
	}

	public List<SqlPropertyMetadata> getProperties() {
		return properties;
	}

	public Set<String> getJoinNames() {
		return joinNameMap.keySet();
	}

	public SqlJoinMetadata getJoin(String name) {
		Arguments.notEmpty(name, "name");
		return joinNameMap.get(name);
	}

	public Set<String> getPropertyNames() {
		return propertyNameMap.keySet();
	}

	public Set<String> getPropertyAliases() {
		return propertyAliasMap.keySet();
	}

	public SqlPropertyMetadata getProperty(String name) {
		Arguments.notEmpty(name, "name");
		if (name.startsWith("#")) {
			return propertyAliasMap.get(name.substring(1));
		} else {
			return propertyNameMap.get(name);
		}
	}
}
