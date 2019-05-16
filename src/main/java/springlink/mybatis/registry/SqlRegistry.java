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

import java.io.IOException;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeAliasRegistry;

import com.google.common.reflect.ClassPath;

import springlink.mybatis.annotation.SqlEntity;
import springlink.mybatis.metadata.SqlJoinMetadata;
import springlink.mybatis.metadata.SqlMetadata;
import springlink.mybatis.util.Arguments;

public class SqlRegistry {
	private static final String TABLE_ALIAS = "t";
	private static final String JOIN_TABLE_ALIAS_PREFIX = "j";

	private final Configuration configuration;
	private final SqlDialect dialect;

	public SqlRegistry(Configuration configuration, SqlDialect dialect) {
		Arguments.notNull(configuration, "configuration");
		Arguments.notNull(dialect, "dialect");
		this.configuration = configuration;
		this.dialect = dialect;
	}

	public void addPackage(String packageName, ClassLoader classLoader) throws IOException, ClassNotFoundException {
		Arguments.notEmpty(packageName, "packageName");
		Arguments.notNull(classLoader, "classLoader");
		ClassPath classPath = ClassPath.from(classLoader);
		for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
			Class<?> cls = classLoader.loadClass(classInfo.getName());
			if (cls.isAnnotationPresent(SqlEntity.class)) {
				addType(cls);
			}
		}
	}

	public void addType(Class<?> entityType) {
		Arguments.notNull(entityType, "entityType");
		String namespace = entityType.getName();
		String resource = "SqlRegistry[" + namespace + "]";
		MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, resource);
		assistant.setCurrentNamespace(namespace);
		dialect.buildMapper(getContext(entityType), assistant);
		TypeAliasRegistry typeAliasRegistry = configuration.getTypeAliasRegistry();
		typeAliasRegistry.registerAlias(entityType);
	}

	public SqlContext getContext(Class<?> entityType) {
		Arguments.notNull(entityType, "entityType");
		return getContext(entityType, SqlContext.DEFAULT_PATH);
	}

	public SqlContext getContext(Class<?> entityType, String rootPath) {
		Arguments.notNull(entityType, "entityType");
		SqlContext ctx = getContext(rootPath);
		ctx.putEntity(entityType, TABLE_ALIAS);
		int joinIndex = 0;
		for (SqlJoinMetadata jm : SqlMetadata.forEntityType(entityType).getJoins()) {
			ctx.putEntity(jm.getName(), jm.getType(), JOIN_TABLE_ALIAS_PREFIX + (++joinIndex));
		}
		return ctx;
	}

	public SqlContext getContext() {
		return getContext(SqlContext.DEFAULT_PATH);
	}

	public SqlContext getContext(String rootPath) {
		return new SqlContext(dialect, configuration.getTypeAliasRegistry(), rootPath);
	}
}
