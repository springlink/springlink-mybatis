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

package com.github.springlink.mybatis.entity;

import static com.github.springlink.mybatis.sql.SqlCriterion.eq;
import static com.github.springlink.mybatis.sql.SqlReference.of;

import com.github.springlink.mybatis.annotation.SqlEntity;
import com.github.springlink.mybatis.annotation.SqlJoin;
import com.github.springlink.mybatis.annotation.SqlProperty;
import com.github.springlink.mybatis.sql.SqlCriterion;

@SqlEntity("post")
public class PostOfSally extends PostLite {
	private static final long serialVersionUID = -9165874549112453266L;
	
	@SqlJoin(Author.class)
	private static final SqlCriterion joinAuthorNamed = SqlCriterion.and(
			eq("authorId", of("joinAuthorNamed.#id")),
			eq("joinAuthorNamed.username", "sally"));
	
	@SqlProperty(reference = "joinAuthorNamed.username")
	private String authorNamedSally;

	public String getAuthorNamedSally() {
		return authorNamedSally;
	}

	public void setAuthorNamedSally(String authorNamedSally) {
		this.authorNamedSally = authorNamedSally;
	}
}
