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

public final class Asserts {
	private Asserts() {
	}

	public static <T> T notNull(T obj, String name) {
		if (obj == null) {
			throw new IllegalArgumentException("Argument [" + name + "] cannot be null");
		}
		return obj;
	}

	public static <T extends CharSequence> T notEmpty(T text, String name) {
		if (text == null || text.length() == 0) {
			throw new IllegalArgumentException("Argument [" + name + "] cannot be null or empty");
		}
		return text;
	}
}
