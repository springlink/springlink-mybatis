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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public final class BeanMetadata {
	private static final ConcurrentMap<Class<?>, BeanMetadata> instanceCache = Maps.newConcurrentMap();

	private final Class<?> beanType;
	private final Map<String, PropertyInfo> properties;

	private BeanMetadata(Class<?> beanType, Map<String, PropertyInfo> properties) {
		this.beanType = beanType;
		this.properties = ImmutableMap.copyOf(properties);
	}

	public static BeanMetadata forBeanType(Class<?> beanType) {
		Asserts.notNull(beanType, "beanType");
		return instanceCache.computeIfAbsent(beanType, type -> {
			BeanInfo info;
			try {
				info = Introspector.getBeanInfo(type, Object.class);
			} catch (IntrospectionException e) {
				throw new IllegalStateException("Introspection failed: " + type.getName(), e);
			}
			Map<String, PropertyInfo> props = Maps.newHashMap();
			for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
				String propName = descriptor.getName();
				Class<?> propType = descriptor.getPropertyType();
				Field propField = findPropertyField(type, propName);
				Method propGetter = descriptor.getReadMethod();
				Method propSetter = descriptor.getWriteMethod();
				props.put(propName, new PropertyInfo(propType, propField, propGetter, propSetter));
			}
			return new BeanMetadata(type, props);
		});
	}

	public Class<?> getBeanType() {
		return beanType;
	}

	public String[] getPropertyNames() {
		return properties.keySet().toArray(new String[0]);
	}

	public Class<?> getPropertyType(String name) {
		return getPropertyInfo(name).map(prop -> prop.type).orElse(null);
	}

	public Field getPropertyField(String name) {
		return getPropertyInfo(name).map(prop -> prop.field).orElse(null);
	}

	public Method getPropertyGetter(String name) {
		return getPropertyInfo(name).map(prop -> prop.getter).orElse(null);
	}

	public Method getPropertySetter(String name) {
		return getPropertyInfo(name).map(prop -> prop.setter).orElse(null);
	}

	public <T extends Annotation> T getPropertyAnnotation(String name, Class<T> annotationType) {
		return getPropertyInfo(name).map(attr -> {
			for (AnnotatedElement elem : new AnnotatedElement[] { attr.field, attr.getter, attr.setter }) {
				if (elem != null) {
					T annotation = elem.getDeclaredAnnotation(annotationType);
					if (annotation != null) {
						return annotation;
					}
				}
			}
			return null;
		}).orElse(null);
	}

	public boolean isPropertyAnnotationPresent(String name, Class<? extends Annotation> annotationType) {
		return getPropertyAnnotation(name, annotationType) != null;
	}

	public boolean hasProperty(String name) {
		return getPropertyInfo(name).isPresent();
	}

	private Optional<PropertyInfo> getPropertyInfo(String name) {
		return Optional.ofNullable(properties.get(name));
	}

	private static Field findPropertyField(Class<?> beanType, String name) {
		for (Class<?> t = beanType; t != Object.class; t = t.getSuperclass()) {
			try {
				Field field = t.getDeclaredField(name);
				if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				return field;
			} catch (NoSuchFieldException e) {
				// noop
			}
		}
		return null;
	}

	private static class PropertyInfo {
		final Class<?> type;
		final Field field;
		final Method getter;
		final Method setter;

		PropertyInfo(Class<?> type, Field field, Method getter, Method setter) {
			this.type = type;
			this.field = field;
			this.getter = getter;
			this.setter = setter;
		}
	}
}
