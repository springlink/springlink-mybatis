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

package com.github.springlink.mybatis.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface SqlCache {
	Class<? extends Cache> implementation() default PerpetualCache.class;

	Class<? extends Cache> eviction() default LruCache.class;

	long flushInterval() default 0;

	int size() default 1024;

	boolean readWrite() default true;

	boolean blocking() default false;

	Property[] properties() default {};

	@Retention(RUNTIME)
	@Target({})
	@interface Property {
		String name();

		String value();
	}
}
