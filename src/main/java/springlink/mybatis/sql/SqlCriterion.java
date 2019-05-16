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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import springlink.mybatis.util.Arguments;
import springlink.mybatis.util.GetterResolver;

public abstract class SqlCriterion {
	private static final Junction NONE = new Junction(JunctionType.AND);
	private static final Constant TRUE_VALUE = new Constant(ConstantType.TRUE);
	private static final Constant FALSE_VALUE = new Constant(ConstantType.FALSE);

	private SqlCriterion() {
	}

	public abstract Set<String> getReferenceNames();

	public static <T> SqlCriterion lambda(String prefix, Class<T> type,
			Function<Lambda<T>, ? extends SqlCriterion> supplier) {
		return supplier.apply(new Lambda<>(prefix, type));
	}

	public static <T> SqlCriterion lambda(Class<T> type,
			Function<Lambda<T>, ? extends SqlCriterion> supplier) {
		return lambda(null, type, supplier);
	}

	public static Condition eq(String property, Object arg) {
		return new Condition(ConditionType.EQ, property, arg);
	}

	public static Condition ne(String property, Object arg) {
		return new Condition(ConditionType.NE, property, arg);
	}

	public static Condition gt(String property, Object arg) {
		return new Condition(ConditionType.GT, property, arg);
	}

	public static Condition ge(String property, Object arg) {
		return new Condition(ConditionType.GE, property, arg);
	}

	public static Condition lt(String property, Object arg) {
		return new Condition(ConditionType.LT, property, arg);
	}

	public static Condition le(String property, Object arg) {
		return new Condition(ConditionType.LE, property, arg);
	}

	public static Condition isNull(String property) {
		return new Condition(ConditionType.IS_NULL, property);
	}

	public static Condition isNotNull(String property) {
		return new Condition(ConditionType.IS_NOT_NULL, property);
	}

	public static Condition like(String property, String arg) {
		return new Condition(ConditionType.LIKE, property, arg);
	}

	public static Condition like(String property, String arg, String escape) {
		return new Condition(ConditionType.LIKE_ESC, property, arg, escape);
	}

	public static Condition between(String property, Object arg1, Object arg2) {
		return new Condition(ConditionType.BETWEEN, property, arg1, arg2);
	}

	public static Condition in(String property, Object... args) {
		return new Condition(ConditionType.IN, property, args);
	}

	public static Condition in(String property, Iterable<?> args) {
		return in(property, Iterables.toArray(args, Object.class));
	}

	public static Junction and(SqlCriterion... args) {
		return new Junction(JunctionType.AND, args);
	}

	public static Junction and(Iterable<? extends SqlCriterion> args) {
		return and(Iterables.toArray(args, SqlCriterion.class));
	}

	public static Junction or(SqlCriterion... args) {
		return new Junction(JunctionType.OR, args);
	}

	public static Junction or(Iterable<? extends SqlCriterion> args) {
		return or(Iterables.toArray(args, SqlCriterion.class));
	}

	public static Junction not(SqlCriterion arg) {
		return new Junction(JunctionType.NOT, arg);
	}

	public static Junction notAny(SqlCriterion... args) {
		return new Junction(JunctionType.NOT, or(args));
	}

	public static Junction notAny(Iterable<? extends SqlCriterion> args) {
		return notAny(Iterables.toArray(args, SqlCriterion.class));
	}

	public static Junction notAll(SqlCriterion... args) {
		return new Junction(JunctionType.NOT, and(args));
	}

	public static Junction notAll(Iterable<? extends SqlCriterion> args) {
		return notAll(Iterables.toArray(args, SqlCriterion.class));
	}

	public static Junction notLike(String property, String arg, String escape) {
		return not(like(property, arg, escape));
	}

	public static Junction notLike(String property, String arg) {
		return not(like(property, arg));
	}

	public static Junction notBetween(String property, Object arg1, Object arg2) {
		return not(between(property, arg1, arg2));
	}

	public static Junction notIn(String property, Object... args) {
		return not(in(property, args));
	}

	public static Junction notIn(String property, Iterable<?> args) {
		return not(in(property, args));
	}

	public static SqlCriterion none() {
		return NONE;
	}

	public static SqlCriterion trueValue() {
		return TRUE_VALUE;
	}

	public static SqlCriterion falseValue() {
		return FALSE_VALUE;
	}

	public enum ConditionType {
		EQ, // equal
		NE, // not equal
		GT, // greater than
		GE, // greater than or equal
		LT, // less than
		LE, // less than or equal
		IS_NULL, // is null
		IS_NOT_NULL, // is not null
		LIKE, // like
		LIKE_ESC, // like with escape
		BETWEEN, // between
		IN, // in
	}

	public enum ConstantType {
		TRUE, // true expression
		FALSE, // false expression
	}

	public enum JunctionType {
		AND, // and
		OR, // or
		NOT, // not
	}

	public static class Condition extends SqlCriterion {
		private final ConditionType type;
		private final String property;
		private final List<Object> args;

		private Condition(ConditionType type, String property, Object... args) {
			Arguments.notNull(type, "type");
			Arguments.notEmpty(property, "property");
			this.type = type;
			this.property = property;
			this.args = Collections.unmodifiableList(Lists.newArrayList(args));
		}

		public ConditionType getType() {
			return type;
		}

		public String getProperty() {
			return property;
		}

		public List<Object> getArgs() {
			return args;
		}

		@Override
		public Set<String> getReferenceNames() {
			Set<SqlReference> refs = Sets.newHashSet(SqlReference.of(property));
			for (Object arg : args) {
				if (arg instanceof SqlReference) {
					refs.add(((SqlReference) arg));
				}
			}
			return Collections.unmodifiableSet(refs.stream()
					.map(SqlReference::getName).filter(Objects::nonNull).collect(Collectors.toSet()));
		}

		@Override
		public String toString() {
			switch (type) {
			case EQ:
				return String.format("%s=%s", property, args.get(0));
			case NE:
				return String.format("%s!=%s", property, args.get(0));
			case GT:
				return String.format("%s>%s", property, args.get(0));
			case GE:
				return String.format("%s>=%s", property, args.get(0));
			case LT:
				return String.format("%s<%s", property, args.get(0));
			case LE:
				return String.format("%s<=%s", property, args.get(0));
			case IS_NULL:
				return String.format("%s IS NULL", property);
			case IS_NOT_NULL:
				return String.format("%s IS NOT NULL", property);
			case LIKE:
				return String.format("%s LIKE %s", property, args.get(0));
			case LIKE_ESC:
				return String.format("%s LIKE %s ESCAPE %s", property, args.get(0), args.get(1));
			case BETWEEN:
				return String.format("%s BETWEEN %s AND %s", property, args.get(0), args.get(1));
			case IN:
				return String.format("%s IN(%s)", property,
						args.stream().map(String::valueOf).collect(Collectors.joining(",")));
			default:
				throw new IllegalArgumentException("Unknown condition type: " + type.name());
			}
		}
	}

	public static class Constant extends SqlCriterion {
		private final ConstantType type;
		private final List<Object> args;

		private Constant(ConstantType type, Object... args) {
			Arguments.notNull(type, "type");
			this.type = type;
			this.args = Collections.unmodifiableList(Lists.newArrayList(args));
		}

		public ConstantType getType() {
			return type;
		}

		public List<Object> getArgs() {
			return args;
		}

		@Override
		public Set<String> getReferenceNames() {
			return Collections.emptySet();
		}

		@Override
		public String toString() {
			switch (type) {
			case FALSE:
				return "FALSE";
			case TRUE:
				return "TRUE";
			default:
				throw new IllegalArgumentException("Unknown constant type: " + type.name());
			}
		}
	}

	public static class Junction extends SqlCriterion {
		private final JunctionType type;
		private final List<SqlCriterion> criteria;

		private Junction(JunctionType type, SqlCriterion... criteria) {
			Arguments.notNull(type, "type");
			this.type = type;
			this.criteria = Collections.unmodifiableList(Stream.of(criteria)
					.filter(Objects::nonNull).collect(Collectors.toList()));
		}

		public JunctionType getType() {
			return type;
		}

		public List<SqlCriterion> getCriteria() {
			return criteria;
		}

		@Override
		public Set<String> getReferenceNames() {
			Set<String> refs = Sets.newHashSet();
			for (SqlCriterion criterion : criteria) {
				refs.addAll(criterion.getReferenceNames());
			}
			return Collections.unmodifiableSet(refs);
		}

		@Override
		public String toString() {
			switch (type) {
			case AND:
				return "(" + criteria.stream().map(String::valueOf).collect(Collectors.joining(" AND ")) + ")";
			case OR:
				return "(" + criteria.stream().map(String::valueOf).collect(Collectors.joining(" OR ")) + ")";
			case NOT:
				return "NOT " + criteria.get(0) + ")";
			default:
				throw new IllegalArgumentException("Unknown junction type: " + type.name());
			}
		}
	}

	public static class Lambda<T> {
		private final String prefix;
		private final GetterResolver<T> resolver;

		private Lambda(String prefix, Class<T> type) {
			this.prefix = normalizePrefix(prefix);
			this.resolver = GetterResolver.ofType(type);
		}

		public Condition eq(Function<T, ?> getter, Object arg) {
			return SqlCriterion.eq(prefix + resolver.getPropertyName(getter), arg);
		}

		public Condition ne(Function<T, ?> getter, Object arg) {
			return SqlCriterion.ne(prefix + resolver.getPropertyName(getter), arg);
		}

		public Condition gt(Function<T, ?> getter, Object arg) {
			return SqlCriterion.gt(prefix + resolver.getPropertyName(getter), arg);
		}

		public Condition ge(Function<T, ?> getter, Object arg) {
			return SqlCriterion.ge(prefix + resolver.getPropertyName(getter), arg);
		}

		public Condition lt(Function<T, ?> getter, Object arg) {
			return SqlCriterion.lt(prefix + resolver.getPropertyName(getter), arg);
		}

		public Condition le(Function<T, ?> getter, Object arg) {
			return SqlCriterion.le(prefix + resolver.getPropertyName(getter), arg);
		}

		public Condition isNull(Function<T, ?> getter) {
			return SqlCriterion.isNull(prefix + resolver.getPropertyName(getter));
		}

		public Condition isNotNull(Function<T, ?> getter) {
			return SqlCriterion.isNotNull(prefix + resolver.getPropertyName(getter));
		}

		public Condition like(Function<T, ?> getter, String arg) {
			return SqlCriterion.like(prefix + resolver.getPropertyName(getter), arg);
		}

		public Condition like(Function<T, ?> getter, String arg, String escape) {
			return SqlCriterion.like(prefix + resolver.getPropertyName(getter), arg, escape);
		}

		public Condition between(Function<T, ?> getter, Object arg1, Object arg2) {
			return SqlCriterion.between(prefix + resolver.getPropertyName(getter), arg1, arg2);
		}

		public Condition in(Function<T, ?> getter, Object... args) {
			return SqlCriterion.in(prefix + resolver.getPropertyName(getter), args);
		}

		public Condition in(Function<T, ?> getter, Iterable<?> args) {
			return SqlCriterion.in(prefix + resolver.getPropertyName(getter), args);
		}

		private String normalizePrefix(String prefix) {
			return Strings.isNullOrEmpty(prefix) ? "" : (prefix.endsWith(".") ? prefix : prefix + ".");
		}
	}
}
