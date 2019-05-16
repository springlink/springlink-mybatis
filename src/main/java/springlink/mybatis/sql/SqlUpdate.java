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

package springlink.mybatis.sql;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import springlink.mybatis.util.Arguments;
import springlink.mybatis.util.GetterResolver;

public class SqlUpdate {
	private final String prefix;
	private final List<Set> sets;

	private SqlUpdate(String prefix, List<Set> sets) {
		this.prefix = normalizePrefix(prefix);
		this.sets = sets;
	}

	public static SqlUpdate create() {
		return new SqlUpdate(null, Lists.newArrayList());
	}

	public static <T> Lambda<T> create(Class<T> type) {
		return create().lambda(type);
	}

	public SqlUpdate prefix(String prefix) {
		return new SqlUpdate(prefix, sets);
	}

	public <T> Lambda<T> prefix(String prefix, Class<T> type) {
		return prefix(prefix).lambda(type);
	}

	public <T> Lambda<T> lambda(Class<T> type) {
		return new Lambda<>(this, type);
	}

	public SqlUpdate set(String property, Object arg) {
		return addSet(SetType.SET, property, arg);
	}

	public SqlUpdate nullify(String property) {
		return addSet(SetType.NULLIFY, property);
	}

	public SqlUpdate add(String property, Object arg) {
		return addSet(SetType.ADD, property, arg);
	}

	public SqlUpdate subtract(String property, Object arg) {
		return addSet(SetType.SUBTRACT, property, arg);
	}

	public List<Set> asList() {
		return sets;
	}

	@Override
	public String toString() {
		return "[" + sets.stream().map(Set::toString).collect(Collectors.joining(", ")) + "]";
	}

	private SqlUpdate addSet(SetType type, String property, Object... args) {
		sets.add(new Set(type, prefix + property, args));
		return this;
	}

	private String normalizePrefix(String prefix) {
		return Strings.isNullOrEmpty(prefix) ? "" : (prefix.endsWith(".") ? prefix : prefix + ".");
	}

	public enum SetType {
		SET,
		NULLIFY,
		ADD,
		SUBTRACT;
	}

	public static class Set {
		private final SetType type;
		private final String property;
		private final List<Object> args;

		private Set(SetType type, String property, Object... args) {
			Arguments.notNull(type, "type");
			Arguments.notEmpty(property, "property");
			this.type = type;
			this.property = property;
			this.args = Collections.unmodifiableList(Lists.newArrayList(args));
		}

		public SetType getType() {
			return type;
		}

		public String getProperty() {
			return property;
		}

		public List<Object> getArgs() {
			return args;
		}

		@Override
		public String toString() {
			switch (type) {
			case SET:
				return String.format("%s=%s", property, args.get(0));
			case NULLIFY:
				return String.format("%s=NULL", property);
			case ADD:
				return String.format("%s=%s+%s", property, property, args.get(0));
			case SUBTRACT:
				return String.format("%s=%s-%s", property, property, args.get(0));
			default:
				throw new IllegalArgumentException("Unknown set type: " + type.name());
			}
		}
	}

	public static class Lambda<T> extends SqlUpdate {
		private final GetterResolver<T> resolver;

		private Lambda(SqlUpdate update, Class<T> type) {
			super(update.prefix, update.sets);
			this.resolver = GetterResolver.ofType(type);
		}

		public Lambda<T> set(Function<T, ?> getter, Object arg) {
			return addSet(SetType.SET, getter, arg);
		}

		public Lambda<T> nullify(Function<T, ?> getter) {
			return addSet(SetType.NULLIFY, getter);
		}

		public Lambda<T> add(Function<T, ?> getter, Object arg) {
			return addSet(SetType.ADD, getter, arg);
		}

		public Lambda<T> subtract(Function<T, ?> getter, Object arg) {
			return addSet(SetType.SUBTRACT, getter, arg);
		}

		private Lambda<T> addSet(SetType type, Function<T, ?> getter, Object... args) {
			super.addSet(type, resolver.getPropertyName(getter), args);
			return this;
		}
	}
}
