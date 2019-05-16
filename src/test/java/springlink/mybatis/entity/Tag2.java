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

import springlink.mybatis.annotation.SqlEntity;
import springlink.mybatis.annotation.strategy.LowerCaseNameStrategy;

@SqlEntity(value = "tag", nameStrategy = LowerCaseNameStrategy.class)
public class Tag2 implements Serializable {
	private static final long serialVersionUID = 4866296630516162481L;

	private Integer id;
	private String naMe;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNaMe() {
		return naMe;
	}

	public void setNaMe(String naMe) {
		this.naMe = naMe;
	}
}
