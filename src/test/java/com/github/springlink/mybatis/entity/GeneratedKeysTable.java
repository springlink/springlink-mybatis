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

import com.github.springlink.mybatis.annotation.SqlEntity;
import com.github.springlink.mybatis.annotation.SqlProperty;

@SqlEntity
public class GeneratedKeysTable implements Serializable {
	private static final long serialVersionUID = 3817547546287534586L;

	@SqlProperty(generated = true)
	private Integer id1;

	//@SqlGenerated
	private Integer id2;

	public Integer getId1() {
		return id1;
	}

	public void setId1(Integer id1) {
		this.id1 = id1;
	}

	public Integer getId2() {
		return id2;
	}

	public void setId2(Integer id2) {
		this.id2 = id2;
	}
}
