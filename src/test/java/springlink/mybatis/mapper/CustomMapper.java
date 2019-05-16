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

package springlink.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import springlink.mybatis.registry.SqlContext;

public interface CustomMapper {
	@Select("${ctx.bind('Post#t; blog:Blog#a; author:Author#b')}"
			+ " SELECT b.username"
			+ "  FROM post t"
			+ "  LEFT JOIN blog a ON t.blog_id = a.id"
			+ "  LEFT JOIN author b ON t.author_id = b.id"
			+ " WHERE ${ctx.sql('where')}")
	List<String> selectPostAuthorName(@Param(SqlContext.DEFAULT_PATH) SqlContext context);
}
