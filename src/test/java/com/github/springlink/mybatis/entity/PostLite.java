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

import java.io.Serializable;
import java.util.Date;

import com.github.springlink.mybatis.annotation.SqlEntity;
import com.github.springlink.mybatis.annotation.SqlJoin;
import com.github.springlink.mybatis.annotation.SqlProperty;
import com.github.springlink.mybatis.sql.SqlCriterion;

@SqlEntity
public class PostLite implements Serializable {
	private static final long serialVersionUID = 2166397361550550806L;

	@SqlJoin(Blog.class)
	private static final SqlCriterion joinBlog = eq("blogId", of("joinBlog.#id"));

	@SqlJoin(Author.class)
	private static final SqlCriterion joinAuthor = eq("authorId", of("joinAuthor.#id"));

	@SqlProperty(id = true, aliases = "id")
	private Integer id;

	private Integer blogId;

	private Integer authorId;

	@SqlProperty(aliases = { "CreateDate" })
	private Date createdOn;

	private String section;

	private String subject;

	private String body;

	private Boolean draft;

	private Integer star;

	@SqlProperty(aliases = "ct", reference = "createdOn")
	private Date createTime;

	@SqlProperty(reference = "joinAuthor.username")
	private String authorName;

	@SqlProperty(reference = "joinBlog.title")
	private String blogTitle;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBlogId() {
		return blogId;
	}

	public void setBlogId(Integer blogId) {
		this.blogId = blogId;
	}

	public Integer getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Integer authorId) {
		this.authorId = authorId;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Boolean getDraft() {
		return draft;
	}

	public void setDraft(Boolean draft) {
		this.draft = draft;
	}

	public Integer getStar() {
		return star;
	}

	public void setStar(Integer star) {
		this.star = star;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getBlogTitle() {
		return blogTitle;
	}

	public void setBlogTitle(String blogTitle) {
		this.blogTitle = blogTitle;
	}
}
