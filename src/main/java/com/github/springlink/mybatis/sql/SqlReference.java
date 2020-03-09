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

package com.github.springlink.mybatis.sql;

import java.util.Objects;

import com.google.common.base.Strings;

import com.github.springlink.mybatis.util.Asserts;

public class SqlReference {
	private final String name;
	private final String property;

	private final int hashCode;

	private SqlReference(String joinName, String property) {
		this.name = joinName;
		this.property = property;
		this.hashCode = Objects.hash(this.name, this.property);
	}

	public String getName() {
		return name;
	}

	public String getProperty() {
		return property;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SqlReference)) {
			return false;
		}
		SqlReference other = (SqlReference) obj;
		return Objects.equals(name, other.name)
				&& Objects.equals(property, other.property);
	}

	@Override
	public String toString() {
		return name == null ? property : name + "." + property;
	}

	public static SqlReference of(String property) {
		Asserts.notEmpty(property, "property");
		int split = property.indexOf('.');
		if (split < 0) {
			return new SqlReference(null, property);
		} else {
			return new SqlReference(
					Strings.emptyToNull(property.substring(0, split)),
					property.substring(split + 1));
		}
	}
}
