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

package springlink.mybatis.entity;

import static springlink.mybatis.sql.SqlCriterion.eq;
import static springlink.mybatis.sql.SqlReference.of;

import springlink.mybatis.annotation.SqlEntity;
import springlink.mybatis.annotation.SqlJoin;
import springlink.mybatis.annotation.SqlProperty;
import springlink.mybatis.sql.SqlCriterion;

@SqlEntity
public class Post extends PostLite {
	private static final long serialVersionUID = -9165874549112453266L;

	@SqlJoin(value = Author.class, name = "authorOfBlog")
	private static final SqlCriterion joinBlogAuthor = eq("authorOfBlog.id", of("joinBlog.authorId"));

	@SqlProperty(reference = "authorOfBlog.username")
	private String blogAuthorName;

	public String getBlogAuthorName() {
		return blogAuthorName;
	}

	public void setBlogAuthorName(String blogAuthorName) {
		this.blogAuthorName = blogAuthorName;
	}
}
