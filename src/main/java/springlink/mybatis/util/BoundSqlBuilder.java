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

package springlink.mybatis.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class BoundSqlBuilder {
	private final Configuration configuration;
	private String sql;
	private Object parameterObject;
	private final List<ParameterMapping> parameterMappings = Lists.newArrayList();
	private final Map<String, Object> additionalParameters = Maps.newHashMap();

	@SuppressWarnings("unchecked")
	public BoundSqlBuilder(Configuration configuration, BoundSql boundSql) {
		this.configuration = configuration;
		MetaObject metaBoundSql = configuration.newMetaObject(boundSql);
		this.sql = boundSql.getSql();
		this.parameterObject = boundSql.getParameterObject();
		this.parameterMappings.addAll(boundSql.getParameterMappings());
		this.additionalParameters.putAll((Map<String, Object>) metaBoundSql.getValue("additionalParameters"));
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public String getSql() {
		return sql;
	}

	public Object getParameterObject() {
		return parameterObject;
	}

	public List<ParameterMapping> getParameterMappings() {
		return parameterMappings;
	}

	public Map<String, Object> getAdditionalParameters() {
		return additionalParameters;
	}

	public BoundSqlBuilder setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public BoundSqlBuilder setParameterObject(Object parameterObject) {
		this.parameterObject = parameterObject;
		return this;
	}

	public BoundSqlBuilder addParameterMapping(ParameterMapping parameterMapping) {
		this.parameterMappings.add(parameterMapping);
		return this;
	}

	public BoundSqlBuilder addParameterMappings(Collection<? extends ParameterMapping> parameterMappings) {
		this.parameterMappings.addAll(parameterMappings);
		return this;
	}

	public BoundSqlBuilder putAdditionalParameter(String name, Object value) {
		this.additionalParameters.put(name, value);
		return this;
	}

	public BoundSqlBuilder putAdditionalParameters(Map<String, ?> additionalParameters) {
		this.additionalParameters.putAll(additionalParameters);
		return this;
	}

	public BoundSql build() {
		BoundSql boundSql = new BoundSql(configuration, sql, parameterMappings, parameterObject);
		for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
			boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
		}
		return boundSql;
	}
}
