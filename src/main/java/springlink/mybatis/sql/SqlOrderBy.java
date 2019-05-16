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

public class SqlOrderBy {
	private final String prefix;
	private final List<Order> orders;

	private SqlOrderBy(String prefix, List<Order> orders) {
		this.prefix = normalizePrefix(prefix);
		this.orders = orders;
	}

	public static SqlOrderBy create() {
		return new SqlOrderBy(null, Lists.newArrayList());
	}

	public static <T> Lambda<T> create(Class<T> type) {
		return create().lambda(type);
	}

	public SqlOrderBy prefix(String prefix) {
		return new SqlOrderBy(prefix, orders);
	}

	public <T> Lambda<T> prefix(String prefix, Class<T> type) {
		return prefix(prefix).lambda(type);
	}

	public <T> Lambda<T> lambda(Class<T> type) {
		return new Lambda<>(this, type);
	}

	public SqlOrderBy asc(String property, boolean toggle) {
		return addOrder(property, !toggle);
	}

	public SqlOrderBy asc(String property) {
		return addOrder(property, false);
	}

	public SqlOrderBy desc(String property, boolean toggle) {
		return addOrder(property, toggle);
	}

	public SqlOrderBy desc(String property) {
		return addOrder(property, true);
	}

	public List<Order> getOrders() {
		return Collections.unmodifiableList(orders);
	}

	public List<Order> asList() {
		return orders;
	}

	@Override
	public String toString() {
		return "[" + orders.stream().map(Order::toString).collect(Collectors.joining(", ")) + "]";
	}

	private SqlOrderBy addOrder(String property, boolean descending) {
		orders.add(new Order(prefix + property, descending));
		return this;
	}

	private String normalizePrefix(String prefix) {
		return Strings.isNullOrEmpty(prefix) ? "" : (prefix.endsWith(".") ? prefix : prefix + ".");
	}

	public static class Order {
		private final String property;
		private final boolean descending;

		private Order(String property, boolean descending) {
			Arguments.notEmpty(property, "property");
			this.property = property;
			this.descending = descending;
		}

		public String getProperty() {
			return property;
		}

		public boolean isDescending() {
			return descending;
		}

		@Override
		public String toString() {
			return property + (descending ? " DESC" : " ASC");
		}
	}

	public static class Lambda<T> extends SqlOrderBy {
		private final GetterResolver<T> resolver;

		private Lambda(SqlOrderBy orderBy, Class<T> type) {
			super(orderBy.prefix, orderBy.orders);
			this.resolver = GetterResolver.ofType(type);
		}

		public Lambda<T> asc(Function<T, ?> getter, boolean toggle) {
			return addOrder(getter, !toggle);
		}

		public Lambda<T> asc(Function<T, ?> getter) {
			return addOrder(getter, false);
		}

		public Lambda<T> desc(Function<T, ?> getter, boolean toggle) {
			return addOrder(getter, toggle);
		}

		public Lambda<T> desc(Function<T, ?> getter) {
			return addOrder(getter, true);
		}

		private Lambda<T> addOrder(Function<T, ?> getter, boolean descending) {
			super.addOrder(resolver.getPropertyName(getter), descending);
			return this;
		}
	}
}
