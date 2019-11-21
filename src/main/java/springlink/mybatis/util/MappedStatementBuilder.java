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

import java.util.List;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

public class MappedStatementBuilder {
	private String id;
	private Configuration configuration;
	private SqlSource sqlSource;
	private SqlCommandType sqlCommandType;
	private String resource;
	private ParameterMap parameterMap;
	private List<ResultMap> resultMaps;
	private Integer fetchSize;
	private Integer timeout;
	private StatementType statementType;
	private ResultSetType resultSetType;
	private Cache cache;
	private boolean flushCacheRequired;
	private boolean useCache;
	private boolean resultOrdered;
	private KeyGenerator keyGenerator;
	private String[] keyProperties;
	private String[] keyColumns;
	private String databaseId;
	private LanguageDriver lang;
	private String[] resultSets;

	public MappedStatementBuilder(MappedStatement statement) {
		this.id = statement.getId();
		this.configuration = statement.getConfiguration();
		this.sqlSource = statement.getSqlSource();
		this.sqlCommandType = statement.getSqlCommandType();
		this.resource = statement.getResource();
		this.parameterMap = statement.getParameterMap();
		this.resultMaps = statement.getResultMaps();
		this.fetchSize = statement.getFetchSize();
		this.timeout = statement.getTimeout();
		this.statementType = statement.getStatementType();
		this.resultSetType = statement.getResultSetType();
		this.cache = statement.getCache();
		this.flushCacheRequired = statement.isFlushCacheRequired();
		this.useCache = statement.isUseCache();
		this.resultOrdered = statement.isResultOrdered();
		this.keyGenerator = statement.getKeyGenerator();
		this.keyProperties = statement.getKeyProperties();
		this.keyColumns = statement.getKeyColumns();
		this.databaseId = statement.getDatabaseId();
		this.lang = statement.getLang();
		this.resultSets = statement.getResultSets();
	}

	public MappedStatementBuilder setId(String id) {
		this.id = id;
		return this;
	}

	public MappedStatementBuilder setConfiguration(Configuration configuration) {
		this.configuration = configuration;
		return this;
	}

	public MappedStatementBuilder setSqlSource(SqlSource sqlSource) {
		this.sqlSource = sqlSource;
		return this;
	}

	public MappedStatementBuilder setResource(String resource) {
		this.resource = resource;
		return this;
	}

	public MappedStatementBuilder setParameterMap(ParameterMap parameterMap) {
		this.parameterMap = parameterMap;
		return this;
	}

	public MappedStatementBuilder setResultMaps(List<ResultMap> resultMaps) {
		this.resultMaps = resultMaps;
		return this;
	}

	public MappedStatementBuilder setFetchSize(Integer fetchSize) {
		this.fetchSize = fetchSize;
		return this;
	}

	public MappedStatementBuilder setTimeout(Integer timeout) {
		this.timeout = timeout;
		return this;
	}

	public MappedStatementBuilder setStatementType(StatementType statementType) {
		this.statementType = statementType;
		return this;
	}

	public MappedStatementBuilder setResultSetType(ResultSetType resultSetType) {
		this.resultSetType = resultSetType;
		return this;
	}

	public MappedStatementBuilder setCache(Cache cache) {
		this.cache = cache;
		return this;
	}

	public MappedStatementBuilder setFlushCacheRequired(boolean flushCacheRequired) {
		this.flushCacheRequired = flushCacheRequired;
		return this;
	}

	public MappedStatementBuilder setUseCache(boolean useCache) {
		this.useCache = useCache;
		return this;
	}

	public MappedStatementBuilder setResultOrdered(boolean resultOrdered) {
		this.resultOrdered = resultOrdered;
		return this;
	}

	public MappedStatementBuilder setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
		return this;
	}

	public MappedStatementBuilder setKeyProperties(String[] keyProperties) {
		this.keyProperties = keyProperties;
		return this;
	}

	public MappedStatementBuilder setKeyColumns(String[] keyColumns) {
		this.keyColumns = keyColumns;
		return this;
	}

	public MappedStatementBuilder setDatabaseId(String databaseId) {
		this.databaseId = databaseId;
		return this;
	}

	public MappedStatementBuilder setLang(LanguageDriver driver) {
		this.lang = driver;
		return this;
	}

	public MappedStatementBuilder setResultSets(String[] resultSets) {
		this.resultSets = resultSets;
		return this;
	}

	public MappedStatement build() {
		return new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
				.cache(cache)
				.databaseId(databaseId)
				.fetchSize(fetchSize)
				.flushCacheRequired(flushCacheRequired)
				.keyColumn(arrayToDelimitedString(keyColumns))
				.keyGenerator(keyGenerator)
				.keyProperty(arrayToDelimitedString(keyProperties))
				.lang(lang)
				.parameterMap(parameterMap)
				.resource(resource)
				.resultMaps(resultMaps)
				.resultOrdered(resultOrdered)
				.resultSets(arrayToDelimitedString(resultSets))
				.resultSetType(resultSetType)
				.statementType(statementType)
				.timeout(timeout)
				.useCache(useCache)
				.build();
	}

	private String arrayToDelimitedString(String[] in) {
		return in != null && in.length > 0 ? String.join(",", in) : null;
	}
}
