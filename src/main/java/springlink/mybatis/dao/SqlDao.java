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

package springlink.mybatis.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import springlink.mybatis.metadata.SqlMetadata;
import springlink.mybatis.metadata.SqlPropertyMetadata;
import springlink.mybatis.registry.SqlRegistry;
import springlink.mybatis.sql.SqlCriterion;
import springlink.mybatis.sql.SqlOrderBy;
import springlink.mybatis.sql.SqlProjections;
import springlink.mybatis.sql.SqlUpdate;
import springlink.mybatis.util.BoundList;

public interface SqlDao {
	/**
	 * Retrieves the current registry.
	 * 
	 * @return the SqlRegistry instance
	 */
	SqlRegistry getRegistry();

	/**
	 * Retrieves the current session.
	 * 
	 * @return the SqlSession instance
	 */
	SqlSession getSession();

	/**
	 * Execute a insert operation for the entity type.
	 * 
	 * @param <T>        the entity type
	 * @param entityType the entity class
	 * @param value      an object contains entity values
	 * @return affacted rows
	 */
	<T> int insert(Class<T> entityType, T value);

	/**
	 * Execute a delete operation for the entity type.
	 * 
	 * @param entityType entity type
	 * @param criterion  conditions
	 * @return affacted rows
	 */
	int delete(Class<?> entityType, SqlCriterion criterion);

	/**
	 * Execute a delete operation for the entity type.
	 * 
	 * @param <T>        entity type
	 * @param entityType entity class
	 * @param supplier   conditions supplier
	 * @return affacted rows
	 */
	default <T> int delete(Class<T> entityType,
			Function<SqlCriterion.Lambda<T>, ? extends SqlCriterion> supplier) {
		return delete(entityType, SqlCriterion.lambda(entityType, supplier));
	}

	/**
	 * Execute a count operation for the entity type.
	 * 
	 * @param entityType entity class
	 * @param criterion  conditions
	 * @return count of the matching rows
	 */
	long count(Class<?> entityType, SqlCriterion criterion);

	/**
	 * Execute a count operation for the entity type.
	 * 
	 * @param <T>        entity type
	 * @param entityType entity class
	 * @param supplier   conditions supplier
	 * @return count of the matching rows
	 */
	default <T> long count(Class<T> entityType,
			Function<SqlCriterion.Lambda<T>, ? extends SqlCriterion> supplier) {
		return count(entityType, SqlCriterion.lambda(entityType, supplier));
	}

	/**
	 * Execute a exists operation for the entity type.
	 * 
	 * @param entityType entity class
	 * @param criterion  conditions
	 * @return whether a matching rows was found
	 */
	boolean exists(Class<?> entityType, SqlCriterion criterion);

	/**
	 * Execute a exists operation for the entity type.
	 * 
	 * @param <T>        entity type
	 * @param entityType entity class
	 * @param supplier   conditions supplier
	 * @return whether a matching rows was found
	 */
	default <T> boolean exists(Class<T> entityType,
			Function<SqlCriterion.Lambda<T>, ? extends SqlCriterion> supplier) {
		return exists(entityType, SqlCriterion.lambda(entityType, supplier));
	}

	/**
	 * Execute a update operation for the entity type.
	 * 
	 * @param <T>        entity type
	 * @param entityType entity class
	 * @param update     update descriptor
	 * @param criterion  conditions
	 * @return affacted rows
	 */
	<T> int update(Class<T> entityType, SqlUpdate update, SqlCriterion criterion);

	/**
	 * Execute a update operation for the entity type.
	 * 
	 * @param <T>               entity type
	 * @param entityType        entity class
	 * @param updateSupplier    update descriptor supplier
	 * @param criterionSupplier conditions supplier
	 * @return affacted rows
	 */
	default <T> int update(Class<T> entityType,
			Function<SqlUpdate.Lambda<T>, ? extends SqlUpdate> updateSupplier,
			Function<SqlCriterion.Lambda<T>, ? extends SqlCriterion> criterionSupplier) {
		return update(entityType, updateSupplier.apply(SqlUpdate.create(entityType)),
				SqlCriterion.lambda(entityType, criterionSupplier));
	}

	/**
	 * Execute a update operation for the entity type.
	 * 
	 * @param <T>         entity type
	 * @param entityType  entity class
	 * @param entity      an object contains entity values
	 * @param ignoreNulls decide how to handle null values, set true if you want to
	 *                    ignore null properties, or an nullify operation will be
	 *                    applied
	 * @param criterion   conditions
	 * @return affacted rows
	 */
	default <T> int updateEntity(Class<T> entityType, T entity, boolean ignoreNulls, SqlCriterion criterion) {
		if (entity == null) {
			return 0;
		}
		SqlUpdate update = SqlUpdate.create();
		MetaObject metaEntity = getSession().getConfiguration().newMetaObject(entity);
		for (SqlPropertyMetadata pm : SqlMetadata.forEntityType(entityType).getProperties()) {
			if (pm.getReference() == null) {
				String propName = pm.getName();
				Object propValue = metaEntity.getValue(propName);
				if (propValue != null) {
					update.set(propName, propValue);
				} else if (!ignoreNulls) {
					update.nullify(propName);
				}
			}
		}
		return update(entityType, update, criterion);
	}

	/**
	 * Execute a update operation for the entity type.
	 * 
	 * @param <T>               entity type
	 * @param entityType        entity class
	 * @param entity            an object contains entity values
	 * @param ignoreNulls       decide how to handle null values, set true if you
	 *                          want to ignore null properties, or an nullify
	 *                          operation will be applied
	 * @param criterionSupplier conditions supplier
	 * @return affacted rows
	 */
	default <T> int updateEntity(Class<T> entityType, T entity, boolean ignoreNulls,
			Function<SqlCriterion.Lambda<T>, ? extends SqlCriterion> criterionSupplier) {
		return updateEntity(entityType, entity, ignoreNulls,
				SqlCriterion.lambda(entityType, criterionSupplier));
	}

	/**
	 * Execute a update operation for the entity type, ignoring null properties.
	 * 
	 * @param <T>        entity type
	 * @param entityType entity class
	 * @param entity     an object contains entity values
	 * @param criterion  conditions
	 * @return affacted rows
	 */
	default <T> int updateEntity(Class<T> entityType, T entity, SqlCriterion criterion) {
		return updateEntity(entityType, entity, true, criterion);
	}

	/**
	 * Execute a update operation for the entity type, ignoring null properties.
	 * 
	 * @param <T>               entity type
	 * @param entityType        entity class
	 * @param entity            an object contains entity values
	 * @param criterionSupplier conditions supplier
	 * @return affacted rows
	 */
	default <T> int updateEntity(Class<T> entityType, T entity,
			Function<SqlCriterion.Lambda<T>, ? extends SqlCriterion> criterionSupplier) {
		return updateEntity(entityType, entity, SqlCriterion.lambda(entityType, criterionSupplier));
	}

	/**
	 * Create a selector for the entity type.
	 * 
	 * @param <T>        entity type
	 * @param entityType entity class
	 * @return selector
	 */
	<T> Selector<T> select(Class<T> entityType);

	interface Selector<T> {
		/**
		 * Retrieve the current entity class.
		 * 
		 * @return entity class
		 */
		Class<T> getEntityType();

		/**
		 * Retrieve the current registry.
		 * 
		 * @return SqlRegistry instance
		 */
		SqlRegistry getRegistry();

		/**
		 * Retrieve the current session.
		 * 
		 * @return SqlSession instance
		 */
		SqlSession getSession();

		/**
		 * Retrieve the current conditions.
		 * 
		 * @return conditions
		 */
		SqlCriterion getCriterion();

		/**
		 * Retrieve the current orders.
		 * 
		 * @return orders
		 */
		SqlOrderBy getOrderBy();

		/**
		 * Retrieve whether forUpdate mode is activated.
		 * 
		 * @return whether forUpdate mode is activated
		 */
		boolean isForUpdate();

		/**
		 * Change the current conditions.
		 * 
		 * @param criterion conditions
		 * @return the selector itself
		 */
		Selector<T> where(SqlCriterion criterion);

		/**
		 * Change the current conditions.
		 * 
		 * @param supplier conditions supplier
		 * @return the selector itself
		 */
		default Selector<T> where(Function<SqlCriterion.Lambda<T>, ? extends SqlCriterion> supplier) {
			return where(SqlCriterion.lambda(getEntityType(), supplier));
		}

		/**
		 * Change the current orders.
		 * 
		 * @param order orders
		 * @return the selector itself
		 */
		Selector<T> orderBy(SqlOrderBy order);

		/**
		 * Change the current orders.
		 * 
		 * @param supplier orders supplier
		 * @return the selector itself
		 */
		default Selector<T> orderBy(Function<SqlOrderBy.Lambda<T>, ? extends SqlOrderBy> supplier) {
			return orderBy(supplier.apply(SqlOrderBy.create(getEntityType())));
		}

		/**
		 * Change the current forUpdate mode
		 * 
		 * @param forUpdate whether to activate forUpdate mode
		 * @return the selector itself
		 */
		Selector<T> forUpdate(boolean forUpdate);

		/**
		 * Activate forUpdate mode.
		 * 
		 * @return the selector itself
		 */
		default Selector<T> forUpdate() {
			return forUpdate(true);
		}

		/**
		 * Execute a select operation with one row expected.
		 * @return optional entity object
		 */
		Optional<T> asOne();

		/**
		 * Execute a select operation with one row expected.
		 * @param <R> return type
		 * @param projections projections
		 * @return a map, or a single value for single projection
		 */
		<R> Optional<R> asOne(SqlProjections projections);

		/**
		 * Execute a select operation with one row expected.
		 * @param <R> return type
		 * @param supplier projections supplier
		 * @return a map, or a single value for a single projection
		 */
		default <R> Optional<R> asOne(Function<SqlProjections.Lambda<T>, ? extends SqlProjections> supplier) {
			return asOne(supplier.apply(SqlProjections.create(getEntityType())));
		}

		/**
		 * Execute a select operation.
		 * @return entity list
		 */
		List<T> asList();

		/**
		 * Execute a select operation.
		 * @param <R> return type
		 * @param projections projections
		 * @return a map list, or a value list for single projection
		 */
		<R> List<R> asList(SqlProjections projections);

		/**
		 * Execute a select operation.
		 * @param <R> return type
		 * @param supplier projections supplier
		 * @return a map list, or a value list for single projection
		 */
		default <R> List<R> asList(Function<SqlProjections.Lambda<T>, ? extends SqlProjections> supplier) {
			return asList(supplier.apply(SqlProjections.create(getEntityType())));
		}

		List<T> asList(RowBounds rowBounds);

		<R> List<R> asList(RowBounds rowBounds, SqlProjections projections);

		default <R> List<R> asList(RowBounds rowBounds,
				Function<SqlProjections.Lambda<T>, ? extends SqlProjections> supplier) {
			return asList(rowBounds, supplier.apply(SqlProjections.create(getEntityType())));
		}

		BoundList<T> asBoundList(RowBounds rowBounds);

		<R> BoundList<R> asBoundList(RowBounds rowBounds, SqlProjections projections);

		default <R> BoundList<R> asBoundList(RowBounds rowBounds,
				Function<SqlProjections.Lambda<T>, ? extends SqlProjections> supplier) {
			return asBoundList(rowBounds, supplier.apply(SqlProjections.create(getEntityType())));
		}

		<K> Map<K, T> asMap(String mapKey);
	}
}
