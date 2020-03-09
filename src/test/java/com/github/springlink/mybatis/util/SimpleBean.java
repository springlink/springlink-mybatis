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

public class SimpleBean extends BaseBean {
	private Long property2;

	private Boolean property3;

	private Object property4;

	@SuppressWarnings("unused")
	private String propertyWithoutGetter;

	private String propertyWithoutSetter;

	@SuppressWarnings("unused")
	private static String propertyWithStaticField;

	@TestAnnotation("field")
	private String propertyWithAnnotationOnField;

	private String propertyWithAnnotationOnGetter;

	private String propertyWithAnnotationOnSetter;

	public String getProperty1() {
		return property1;
	}

	public void setProperty1(String property1) {
		this.property1 = property1;
	}

	public Long getProperty2() {
		return property2;
	}

	public void setProperty2(Long property2) {
		this.property2 = property2;
	}

	public Boolean getProperty3() {
		return property3;
	}

	public void setProperty3(Boolean property3) {
		this.property3 = property3;
	}

	public Object getProperty4() {
		return property4;
	}

	public void setProperty4(Object property4) {
		this.property4 = property4;
	}

	public String getPropertyWithoutSetter() {
		return propertyWithoutSetter;
	}

	public void setPropertyWithoutGetter(String propertyWithoutGetter) {
		this.propertyWithoutGetter = propertyWithoutGetter;
	}

	@TestAnnotation("getter")
	public String getPropertyWithAnnotationOnField() {
		return propertyWithAnnotationOnField;
	}

	@TestAnnotation("setter")
	public void setPropertyWithAnnotationOnField(String propertyWithAnnotationOnField) {
		this.propertyWithAnnotationOnField = propertyWithAnnotationOnField;
	}

	@TestAnnotation("getter")
	public String getPropertyWithAnnotationOnGetter() {
		return propertyWithAnnotationOnGetter;
	}

	@TestAnnotation("setter")
	public void setPropertyWithAnnotationOnGetter(String propertyWithAnnotationOnGetter) {
		this.propertyWithAnnotationOnGetter = propertyWithAnnotationOnGetter;
	}

	public String getPropertyWithAnnotationOnSetter() {
		return propertyWithAnnotationOnSetter;
	}

	@TestAnnotation("setter")
	public void setPropertyWithAnnotationOnSetter(String propertyWithAnnotationOnSetter) {
		this.propertyWithAnnotationOnSetter = propertyWithAnnotationOnSetter;
	}

	public String getPropertyWithStaticField() {
		return null;
	}

	public void setPropertyWithStaticField(String propertyWithStaticField) {
	}
}
