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

package com.github.springlink.mybatis.metadata;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import com.github.springlink.mybatis.util.Asserts;

public class SqlPropertyMetadata {
	private final String name;
	private final Set<String> aliases;
	private final Class<?> type;
	private final String column;
	private final String reference;
	private final boolean id;
	private final boolean generated;
	private final JdbcType jdbcType;
	private final Class<? extends TypeHandler<?>> typeHandler;

	SqlPropertyMetadata(String name, Set<String> aliases, Class<?> type, String column, String reference,
			boolean id, boolean generated, JdbcType jdbcType, Class<? extends TypeHandler<?>> typeHandler) {
		this.name = name;
		this.aliases = aliases;
		this.type = type;
		this.column = column;
		this.reference = reference;
		this.id = id;
		this.generated = generated;
		this.jdbcType = jdbcType != JdbcType.UNDEFINED ? jdbcType : null;
		this.typeHandler = typeHandler != UnknownTypeHandler.class ? typeHandler : null;
	}

	public String getName() {
		return name;
	}

	public Set<String> getAliases() {
		return aliases;
	}

	public Class<?> getType() {
		return type;
	}

	public String getColumn() {
		return column;
	}

	public String getReference() {
		return reference;
	}

	public boolean isId() {
		return id;
	}

	public boolean isGenerated() {
		return generated;
	}

	public JdbcType getJdbcType() {
		return jdbcType;
	}

	public Class<? extends TypeHandler<?>> getTypeHandler() {
		return typeHandler;
	}

	public List<ResultFlag> getResultFlags() {
		return id ? Collections.singletonList(ResultFlag.ID) : Collections.emptyList();
	}

	public String getParameterSql(String path) {
		Asserts.notEmpty(path, "path");
		StringBuilder sb = new StringBuilder();
		sb.append("#{").append(path);
		if (jdbcType != null && jdbcType != JdbcType.UNDEFINED) {
			sb.append(",jdbcType=" + jdbcType.name());
		}
		if (typeHandler != null && typeHandler != UnknownTypeHandler.class) {
			sb.append(",typeHandler=" + typeHandler.getName());
		}
		sb.append("}");
		return sb.toString();
	}
}