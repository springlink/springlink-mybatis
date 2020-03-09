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

import java.util.Properties;

import org.apache.ibatis.cache.Cache;

public class SqlCacheMetadata {
	private final Class<? extends Cache> implementation;
	private final Class<? extends Cache> eviction;
	private final Long flushInterval;
	private final Integer size;
	private final boolean readWrite;
	private final boolean blocking;
	private final Properties properties;

	SqlCacheMetadata(Class<? extends Cache> implementation, Class<? extends Cache> eviction,
			Long flushInterval, Integer size, boolean readWrite, boolean blocking, Properties properties) {
		this.implementation = implementation;
		this.eviction = eviction;
		this.flushInterval = flushInterval;
		this.size = size;
		this.readWrite = readWrite;
		this.blocking = blocking;
		this.properties = properties;
	}

	public Class<? extends Cache> getImplementation() {
		return implementation;
	}

	public Class<? extends Cache> getEviction() {
		return eviction;
	}

	public Long getFlushInterval() {
		return flushInterval;
	}

	public Integer getSize() {
		return size;
	}

	public boolean isReadWrite() {
		return readWrite;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public Properties getProperties() {
		return properties;
	}
}