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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import com.github.springlink.mybatis.util.Asserts;
import com.github.springlink.mybatis.util.GetterResolver;

public class SqlProjections {
	private static final String UNIQUE_NAME = "value";

	private final String prefix;
	private final Map<String, Projection> projections;

	private SqlProjections(String prefix, Map<String, Projection> projections) {
		this.prefix = normalizePrefix(prefix);
		this.projections = projections;
	}

	public static SqlProjections create() {
		return new SqlProjections(null, Maps.newHashMap());
	}

	public static <T> Lambda<T> create(Class<T> type) {
		return create().lambda(type);
	}

	public SqlProjections prefix(String prefix) {
		return new SqlProjections(prefix, projections);
	}

	public <T> Lambda<T> prefix(String prefix, Class<T> type) {
		return prefix(prefix).lambda(type);
	}

	public <T> Lambda<T> lambda(Class<T> type) {
		return new Lambda<>(this, type);
	}

	public SqlProjections property(String property) {
		return property(UNIQUE_NAME, property);
	}

	public SqlProjections property(String name, String property) {
		return addProjection(name, ProjectionType.PROPERTY, property);
	}

	public SqlProjections distinct(String property) {
		return distinct(UNIQUE_NAME, property);
	}

	public SqlProjections distinct(String name, String property) {
		return addProjection(name, ProjectionType.DISTINCT, property);
	}

	public SqlProjections count(String property) {
		return count(UNIQUE_NAME, property);
	}

	public SqlProjections count(String name, String property) {
		return addProjection(name, ProjectionType.COUNT, property);
	}

	public SqlProjections countDistinct(String property) {
		return countDistinct(UNIQUE_NAME, property);
	}

	public SqlProjections countDistinct(String name, String property) {
		return addProjection(name, ProjectionType.COUNT_DISTINCT, property);
	}

	public SqlProjections max(String property) {
		return max(UNIQUE_NAME, property);
	}

	public SqlProjections max(String name, String property) {
		return addProjection(name, ProjectionType.MAX, property);
	}

	public SqlProjections min(String property) {
		return min(UNIQUE_NAME, property);
	}

	public SqlProjections min(String name, String property) {
		return addProjection(name, ProjectionType.MIN, property);
	}

	public SqlProjections sum(String property) {
		return sum(UNIQUE_NAME, property);
	}

	public SqlProjections sum(String name, String property) {
		return addProjection(name, ProjectionType.SUM, property);
	}

	public SqlProjections avg(String property) {
		return avg(UNIQUE_NAME, property);
	}

	public SqlProjections avg(String name, String property) {
		return addProjection(name, ProjectionType.AVG, property);
	}

	public Map<String, Projection> asMap() {
		return projections;
	}

	@Override
	public String toString() {
		return "{" + projections.entrySet().stream()
				.map(entry -> entry.getKey() + ": " + entry.getValue().toString())
				.collect(Collectors.joining(", ")) + "}";
	}

	private SqlProjections addProjection(String name, ProjectionType type, String property) {
		projections.put(name, new Projection(type, prefix + property));
		return this;
	}

	private String normalizePrefix(String prefix) {
		return Strings.isNullOrEmpty(prefix) ? "" : (prefix.endsWith(".") ? prefix : prefix + ".");
	}

	public enum ProjectionType {
		PROPERTY,
		DISTINCT,
		COUNT,
		COUNT_DISTINCT,
		MAX,
		MIN,
		SUM,
		AVG;
	}

	public static class Projection {
		private final ProjectionType type;
		private final String property;

		private Projection(ProjectionType type, String property) {
			Asserts.notNull(type, "type");
			Asserts.notEmpty(property, "property");
			this.type = type;
			this.property = property;
		}

		public ProjectionType getType() {
			return type;
		}

		public String getProperty() {
			return property;
		}

		@Override
		public String toString() {
			switch (type) {
			case PROPERTY:
				return property;
			case DISTINCT:
				return "DISTINCT " + property;
			case COUNT:
				return "COUNT(" + property + ")";
			case COUNT_DISTINCT:
				return "COUNT(DISTINCT " + property + ")";
			case MAX:
				return "MAX(" + property + ")";
			case MIN:
				return "MIN(" + property + ")";
			case SUM:
				return "SUM(" + property + ")";
			case AVG:
				return "AVG(" + property + ")";
			default:
				throw new IllegalArgumentException("Unknown projection type: " + type.name());
			}
		}
	}

	public static class Lambda<T> extends SqlProjections {
		private final GetterResolver<T> resolver;

		private Lambda(SqlProjections projections, Class<T> type) {
			super(projections.prefix, projections.projections);
			this.resolver = GetterResolver.ofType(type);
		}

		public SqlProjections property(Function<T, ?> getter) {
			return property(UNIQUE_NAME, getter);
		}

		public SqlProjections property(String name, Function<T, ?> getter) {
			return addProjection(name, ProjectionType.PROPERTY, getter);
		}

		public SqlProjections distinct(Function<T, ?> getter) {
			return distinct(UNIQUE_NAME, getter);
		}

		public SqlProjections distinct(String name, Function<T, ?> getter) {
			return addProjection(name, ProjectionType.DISTINCT, getter);
		}

		public SqlProjections count(Function<T, ?> getter) {
			return count(UNIQUE_NAME, getter);
		}

		public SqlProjections count(String name, Function<T, ?> getter) {
			return addProjection(name, ProjectionType.COUNT, getter);
		}

		public SqlProjections countDistinct(Function<T, ?> getter) {
			return countDistinct(UNIQUE_NAME, getter);
		}

		public SqlProjections countDistinct(String name, Function<T, ?> getter) {
			return addProjection(name, ProjectionType.COUNT_DISTINCT, getter);
		}

		public SqlProjections max(Function<T, ?> getter) {
			return max(UNIQUE_NAME, getter);
		}

		public SqlProjections max(String name, Function<T, ?> getter) {
			return addProjection(name, ProjectionType.MAX, getter);
		}

		public SqlProjections min(Function<T, ?> getter) {
			return min(UNIQUE_NAME, getter);
		}

		public SqlProjections min(String name, Function<T, ?> getter) {
			return addProjection(name, ProjectionType.MIN, getter);
		}

		public SqlProjections sum(Function<T, ?> getter) {
			return sum(UNIQUE_NAME, getter);
		}

		public SqlProjections sum(String name, Function<T, ?> getter) {
			return addProjection(name, ProjectionType.SUM, getter);
		}

		public SqlProjections avg(Function<T, ?> getter) {
			return avg(UNIQUE_NAME, getter);
		}

		public SqlProjections avg(String name, Function<T, ?> getter) {
			return addProjection(name, ProjectionType.AVG, getter);
		}

		private Lambda<T> addProjection(String name, ProjectionType type, Function<T, ?> getter) {
			super.addProjection(name, type, resolver.getPropertyName(getter));
			return this;
		}
	}
}
