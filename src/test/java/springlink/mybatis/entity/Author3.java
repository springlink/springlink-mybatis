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

import java.io.Serializable;

import springlink.mybatis.annotation.SqlCacheRef;
import springlink.mybatis.annotation.SqlEntity;
import springlink.mybatis.annotation.SqlProperty;

@SqlEntity(value = "author")
@SqlCacheRef(namespace = "springlink.mybatis.entity.Author")
public class Author3 implements Serializable {
	private static final long serialVersionUID = 594334867423985809L;

	@SqlProperty(aliases = "id")
	private Integer id;

	private String username;

	private String password;

	private String email;

	private String bio;

	private String favouriteSection;

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
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
}
