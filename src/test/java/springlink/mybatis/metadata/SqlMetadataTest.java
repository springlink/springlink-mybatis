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

package springlink.mybatis.metadata;

import org.junit.Test;

import springlink.mybatis.metadata.example.EntityWithCycleJoins;
import springlink.mybatis.metadata.example.EntityWithDuplicatedJoins;
import springlink.mybatis.metadata.example.EntityWithDuplicatedPropertyAliases;
import springlink.mybatis.metadata.example.EntityWithIllegalCacheAnnotation1;
import springlink.mybatis.metadata.example.EntityWithIllegalCacheAnnotation2;
import springlink.mybatis.metadata.example.EntityWithIncompatibleJoinField;
import springlink.mybatis.metadata.example.EntityWithNonFinalJoinField;
import springlink.mybatis.metadata.example.EntityWithNonStaticJoinField;
import springlink.mybatis.metadata.example.EntityWithUnresolvableJoins;

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
}
