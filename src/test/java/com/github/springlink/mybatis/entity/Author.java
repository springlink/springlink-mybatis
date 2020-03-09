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

import java.io.Serializable;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.StringTypeHandler;

import com.github.springlink.mybatis.annotation.SqlCache;
import com.github.springlink.mybatis.annotation.SqlCache.Property;
import com.github.springlink.mybatis.annotation.SqlEntity;
import com.github.springlink.mybatis.annotation.SqlIgnore;
import com.github.springlink.mybatis.annotation.SqlProperty;

@SqlEntity
@SqlCache(readWrite = true, flushInterval = -1, properties = {
		@Property(name = "param1", value = "value1"),
		@Property(name = "param2", value = "value2"),
		@Property(name = "param3", value = "value3")
})
public class Author implements Serializable {
	private static final long serialVersionUID = 594334867423985809L;

	@SqlProperty(aliases = "id", jdbcType = JdbcType.INTEGER)
	private Integer id;

	@SqlIgnore(false)
	private String username;

	@SuppressWarnings("unused")
	private static String password;

	private String actualPassword;

	@SqlProperty(typeHandler = StringTypeHandler.class)
	private String email;

	private String bio;

	@SqlProperty(column = "favourite_section")
	private String favouriteSection;

	@SqlIgnore
	private String ignoredProperty;

	@SuppressWarnings("unused")
	private static String staticProperty;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return actualPassword;
	}

	public void setPassword(String password) {
		this.actualPassword = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getFavouriteSection() {
		return favouriteSection;
	}

	public void setFavouriteSection(String favouriteSection) {
		this.favouriteSection = favouriteSection;
	}

	public String getIgnoredProperty() {
		return ignoredProperty;
	}

	public void setIgnoredProperty(String ignoredProperty) {
		this.ignoredProperty = ignoredProperty;
	}
}
