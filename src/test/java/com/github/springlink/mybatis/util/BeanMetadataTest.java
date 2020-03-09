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

package com.github.springlink.mybatis.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BeanMetadataTest {

	@Test
	public void testForBeanType() {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class))
				.isSameAs(BeanMetadata.forBeanType(SimpleBean.class));
	}

	@Test
	public void testGetBeanType() {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getBeanType())
				.isSameAs(SimpleBean.class);
	}

	@Test
	public void testGetPropertyNames() {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getPropertyNames())
				.containsExactlyInAnyOrder(
						"property1",
						"property2",
						"property3",
						"property4",
						"propertyWithoutGetter",
						"propertyWithoutSetter",
						"propertyWithoutField",
						"propertyWithStaticField",
						"propertyWithAnnotationOnField",
						"propertyWithAnnotationOnGetter",
						"propertyWithAnnotationOnSetter");
	}

	@Test
	public void testGetPropertyType() {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getPropertyType("property1"))
				.isSameAs(String.class);
	}

	@Test
	public void testGetPropertyField() throws NoSuchFieldException {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getPropertyField("property2"))
				.isEqualTo(SimpleBean.class.getDeclaredField("property2"));

		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getPropertyField("propertyWithoutField"))
				.isNull();

		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getPropertyField("propertyWithStaticField"))
				.isNull();
	}

	@Test
	public void testGetPropertyGetter() throws NoSuchMethodException {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getPropertyGetter("property3"))
				.isEqualTo(SimpleBean.class.getDeclaredMethod("getProperty3"));

		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getPropertyGetter("propertyWithoutGetter"))
				.isNull();
	}

	@Test
	public void testGetPropertySetter() throws NoSuchMethodException {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getPropertySetter("property4"))
				.isEqualTo(SimpleBean.class.getDeclaredMethod("setProperty4", Object.class));

		assertThat(BeanMetadata.forBeanType(SimpleBean.class).getPropertySetter("propertyWithoutSetter"))
				.isNull();
	}

	@Test
	public void testGetPropertyAnnotation() throws NoSuchFieldException, NoSuchMethodException {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class)
				.getPropertyAnnotation("propertyWithAnnotationOnField", TestAnnotation.class))
						.isEqualTo(SimpleBean.class.getDeclaredField("propertyWithAnnotationOnField")
								.getDeclaredAnnotation(TestAnnotation.class));

		assertThat(BeanMetadata.forBeanType(SimpleBean.class)
				.getPropertyAnnotation("propertyWithAnnotationOnGetter", TestAnnotation.class))
						.isEqualTo(SimpleBean.class.getDeclaredMethod("getPropertyWithAnnotationOnGetter")
								.getDeclaredAnnotation(TestAnnotation.class));

		assertThat(BeanMetadata.forBeanType(SimpleBean.class)
				.getPropertyAnnotation("propertyWithAnnotationOnSetter", TestAnnotation.class))
						.isEqualTo(SimpleBean.class.getDeclaredMethod("setPropertyWithAnnotationOnSetter", String.class)
								.getDeclaredAnnotation(TestAnnotation.class));
	}

	@Test
	public void testIsPropertyAnnotationPresent() {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class)
				.isPropertyAnnotationPresent("propertyWithAnnotationOnSetter", TestAnnotation.class))
						.isTrue();

		assertThat(BeanMetadata.forBeanType(SimpleBean.class)
				.isPropertyAnnotationPresent("property4", TestAnnotation.class))
						.isFalse();
	}

	@Test
	public void testHasProperty() {
		assertThat(BeanMetadata.forBeanType(SimpleBean.class).hasProperty("property4"))
				.isTrue();

		assertThat(BeanMetadata.forBeanType(SimpleBean.class).hasProperty("property5"))
				.isFalse();
	}
}
