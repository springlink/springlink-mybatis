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

package com.github.springlink.mybatis.util;

import java.util.Map;
import java.util.function.Function;

import org.apache.ibatis.javassist.util.proxy.Proxy;
import org.apache.ibatis.javassist.util.proxy.ProxyFactory;

import com.google.common.collect.Maps;

public final class GetterResolver<T> {
	private static final Map<Class<?>, GetterResolver<?>> mockObjectCache = Maps.newConcurrentMap();

	private final T mockObject;

	private GetterResolver(T mockObject) {
		this.mockObject = mockObject;
	}

	@SuppressWarnings("unchecked")
	public static <T> GetterResolver<T> ofType(Class<T> type) {
		Asserts.notNull(type, "type");
		return (GetterResolver<T>) mockObjectCache.computeIfAbsent(type, t -> {
			ProxyFactory factory = new ProxyFactory();
			factory.setSuperclass(type);
			Class<?> proxyType = factory.createClass();
			Proxy proxyObject;
			try {
				proxyObject = (Proxy) proxyType.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException("Failed to create proxy: " + type.getName(), e);
			}
			proxyObject.setHandler((self, m, proceed, args) -> {
				String methodName = m.getName();
				if (methodName.length() <= 3 || !methodName.startsWith("get")) {
					throw new IllegalArgumentException(String.format("Invalid getter %s in %s", methodName, type.getName()));
				}
				throw new PropertyNameException(Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4));
			});
			return new GetterResolver<>(proxyObject);
		});
	}

	public String getPropertyName(Function<T, ?> getter) {
		Asserts.notNull(getter, "getter");
		try {
			getter.apply(mockObject);
		} catch (PropertyNameException fnr) {
			return fnr.propertyName;
		}
		throw new IllegalStateException("Property name not resolved");
	}

	@SuppressWarnings("serial")
	private static class PropertyNameException extends RuntimeException {
		final String propertyName;

		private PropertyNameException(String propertyName) {
			this.propertyName = propertyName;
		}
	}
}
