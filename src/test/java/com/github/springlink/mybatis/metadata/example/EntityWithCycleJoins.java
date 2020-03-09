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

package com.github.springlink.mybatis.metadata.example;

import com.github.springlink.mybatis.annotation.SqlJoin;
import com.github.springlink.mybatis.sql.SqlCriterion;
import com.github.springlink.mybatis.sql.SqlReference;

public class EntityWithCycleJoins {
	@SqlJoin(EntityWithCycleJoins.class)
	private static final SqlCriterion join1 = SqlCriterion.eq("join1.name", SqlReference.of("join2.name"));

	@SqlJoin(EntityWithCycleJoins.class)
	private static final SqlCriterion join2 = SqlCriterion.eq("join2.name", SqlReference.of("join3.name"));

	@SqlJoin(EntityWithCycleJoins.class)
	private static final SqlCriterion join3 = SqlCriterion.eq("join3.name", SqlReference.of("join1.name"));
}
