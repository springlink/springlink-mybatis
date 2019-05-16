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

package springlink.mybatis.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

import org.apache.ibatis.session.RowBounds;

public class ArrayBoundList<E> extends AbstractList<E> implements BoundList<E>, RandomAccess {
	private final int offset;
	private final int limit;
	private final int total;
	private final Object[] elements;

	public ArrayBoundList(RowBounds rowBounds, int total, List<E> elements) {
		this(rowBounds.getOffset(), rowBounds.getLimit(), total, elements);
	}

	public ArrayBoundList(int offset, int limit, int total, List<E> elements) {
		Arguments.notNull(elements, "elements");
		this.elements = elements.toArray(new Object[elements.size()]);
		this.offset = Math.max(offset, 0);
		this.limit = Math.max(limit, 0);
		this.total = Math.max(total, 0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public E get(int index) {
		return (E) elements[index];
	}

	@Override
	public int size() {
		return elements.length;
	}

	@Override
	public int offset() {
		return offset;
	}

	@Override
	public int limit() {
		return limit;
	}

	@Override
	public int total() {
		return total;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.asList(elements), limit, offset, total);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ArrayBoundList)) {
			return false;
		}
		ArrayBoundList<?> other = (ArrayBoundList<?>) obj;
		return Objects.equals(offset, other.offset)
				&& Objects.equals(limit, other.limit)
				&& Objects.equals(total, other.total)
				&& Objects.equals(Arrays.asList(elements), Arrays.asList(other.elements));
	}
}
