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

import com.github.springlink.mybatis.annotation.SqlJoinType;
import com.github.springlink.mybatis.sql.SqlCriterion;

public class SqlJoinMetadata {
	private final String name;
	private final Class<?> type;
	private final SqlJoinType joinType;
	private final SqlCriterion criterion;

	SqlJoinMetadata(String name, Class<?> type, SqlJoinType joinType, SqlCriterion criterion) {
		this.name = name;
		this.type = type;
		this.joinType = joinType;
		this.criterion = criterion;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public SqlJoinType getJoinType() {
		return joinType;
	}

	public SqlCriterion getCriterion() {
		return criterion;
	}
}