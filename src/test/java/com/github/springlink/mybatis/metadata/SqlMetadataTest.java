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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.github.springlink.mybatis.metadata.example.EntityWithCycleJoins;
import com.github.springlink.mybatis.metadata.example.EntityWithDuplicatedJoins;
import com.github.springlink.mybatis.metadata.example.EntityWithDuplicatedPropertyAliases;
import com.github.springlink.mybatis.metadata.example.EntityWithIllegalCacheAnnotation1;
import com.github.springlink.mybatis.metadata.example.EntityWithIllegalCacheAnnotation2;
import com.github.springlink.mybatis.metadata.example.EntityWithIncompatibleJoinField;
import com.github.springlink.mybatis.metadata.example.EntityWithNonFinalJoinField;
import com.github.springlink.mybatis.metadata.example.EntityWithNonStaticJoinField;
import com.github.springlink.mybatis.metadata.example.EntityWithSequentialJoins;
import com.github.springlink.mybatis.metadata.example.EntityWithSequentialJoinsReversed;
import com.github.springlink.mybatis.metadata.example.EntityWithUnresolvableJoins;

public class SqlMetadataTest {
	@Test(expected = IllegalArgumentException.class)
	public void testDuplicatedJoins() {
		SqlMetadata.resolveEntity(EntityWithDuplicatedJoins.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIncompatibleJoinField() {
		SqlMetadata.resolveEntity(EntityWithIncompatibleJoinField.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonFinalJoinField() {
		SqlMetadata.resolveEntity(EntityWithNonFinalJoinField.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonStaticJoinField() {
		SqlMetadata.resolveEntity(EntityWithNonStaticJoinField.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCacheAnnotation1() {
		SqlMetadata.resolveEntity(EntityWithIllegalCacheAnnotation1.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCacheAnnotation2() {
		SqlMetadata.resolveEntity(EntityWithIllegalCacheAnnotation2.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCycleJoins() {
		SqlMetadata.resolveEntity(EntityWithCycleJoins.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnresolvableJoins() {
		SqlMetadata.resolveEntity(EntityWithUnresolvableJoins.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDuplicatedPropertyAliases() {
		SqlMetadata.resolveEntity(EntityWithDuplicatedPropertyAliases.class);
	}

	@Test
	public void testSequentialJoins() {
		SqlEntityMetadata em = SqlMetadata.resolveEntity(EntityWithSequentialJoins.class);
		SqlJoinMetadata jm1 = em.getJoins().get(0);
		SqlJoinMetadata jm2 = em.getJoins().get(1);
		assertThat(jm1.getName()).isEqualTo("join1");
		assertThat(jm2.getName()).isEqualTo("join2");
		
		em = SqlMetadata.resolveEntity(EntityWithSequentialJoinsReversed.class);
		jm1 = em.getJoins().get(0);
		jm2 = em.getJoins().get(1);
		assertThat(jm1.getName()).isEqualTo("join2");
		assertThat(jm2.getName()).isEqualTo("join1");
	}
}
