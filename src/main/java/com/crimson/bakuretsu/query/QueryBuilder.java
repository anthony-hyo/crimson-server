
package com.crimson.bakuretsu.query;

import com.crimson.bakuretsu.core.Model;
import com.crimson.bakuretsu.core.ModelMetadata;
import com.crimson.bakuretsu.database.DatabaseOperations;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class QueryBuilder<M extends Model<M>> {

	private final Class<M> modelClass;
	private final ModelMetadata metadata;
	private final List<Condition> conditions = new ArrayList<>();
	private final Set<String> eagerLoads = new LinkedHashSet<>();
	private final List<Order> orders = new ArrayList<>();
	private Integer limit;
	private Integer offset;

	public QueryBuilder(Class<M> modelClass) {
		this.modelClass = modelClass;
		this.metadata = ModelMetadata.get(modelClass);
	}

	public QueryBuilder<M> where(String column, String operator, Object value) {
		conditions.add(new Condition(column, operator, value));
		return this;
	}

	public QueryBuilder<M> where(String column, Object value) {
		return where(column, "=", value);
	}

	public QueryBuilder<M> orderBy(String column, String direction) {
		orders.add(new Order(column, direction));
		return this;
	}

	public QueryBuilder<M> orderByRaw(String expression) {
		orders.add(new Order(expression, ""));
		return this;
	}

	public QueryBuilder<M> limit(int limit) {
		this.limit = limit;
		return this;
	}

	public QueryBuilder<M> offset(int offset) {
		this.offset = offset;
		return this;
	}

	public long count() {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ").append(metadata.tableName);
		List<Object> parameters = new ArrayList<>();

		if (!conditions.isEmpty()) {
			sql.append(" WHERE ");
			StringJoiner whereJoiner = new StringJoiner(" AND ");

			for (Condition condition : conditions) {
				whereJoiner.add(condition.toSql());
				parameters.add(condition.value);
			}
			sql.append(whereJoiner);
		}

		try (Connection conn = Model.dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < parameters.size(); i++) {
				stmt.setObject(i + 1, parameters.get(i));
			}

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error executing count query", e);
		}

		return 0;
	}

	public QueryBuilder<M> with(String... relations) {
		for (String relation : relations) {
			if (relation.contains(".")) {
				addDeepRelations(relation);
			} else {
				eagerLoads.add(relation);
			}
		}
		return this;
	}

	private void addDeepRelations(String relationPath) {
		String[] parts = relationPath.split("\\.");
		StringBuilder currentPath = new StringBuilder();
		for (String part : parts) {
			if (!currentPath.isEmpty()) {
				currentPath.append(".");
			}
			currentPath.append(part);
			eagerLoads.add(currentPath.toString());
		}
	}

	public List<M> get() {
		List<M> results = executeQuery();

		if (!eagerLoads.isEmpty()) {
			eagerLoadRelations(results);
		}

		return results;
	}

	public Optional<M> first() {
		List<M> results = limit(1).get();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	public Optional<M> find(Object id) {
		return where(this.metadata.idColumnName, id).first();
	}

	private List<M> executeQuery() {
		StringBuilder sql = new StringBuilder("SELECT * FROM ").append(metadata.tableName);

		List<Object> parameters = new ArrayList<>();

		if (!conditions.isEmpty()) {
			sql.append(" WHERE ");
			StringJoiner whereJoiner = new StringJoiner(" AND ");

			for (Condition condition : conditions) {
				whereJoiner.add(condition.toSql());
				parameters.add(condition.value);
			}

			sql.append(whereJoiner);
		}

		if (!orders.isEmpty()) {
			sql.append(" ORDER BY ");
			StringJoiner orderJoiner = new StringJoiner(", ");

			for (Order order : orders) {
				orderJoiner.add(order.toSql());
			}

			sql.append(orderJoiner);
		}

		if (limit != null) {
			sql.append(" LIMIT ?");
			parameters.add(limit);
		}

		if (offset != null) {
			sql.append(" OFFSET ?");
			parameters.add(offset);
		}

		try (Connection conn = Model.dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {
				stmt.setObject(i + 1, parameters.get(i));
			}

			return DatabaseOperations.createInstances(modelClass, stmt.executeQuery());
		} catch (Throwable e) {
			throw new RuntimeException("Error executing query", e);
		}
	}

	private void eagerLoadRelations(List<M> models) {
		eagerLoads.forEach(relationPath -> loadNestedRelation(models, relationPath));
	}

	private void loadNestedRelation(List<M> parents, String relationPath) {
		String[] relations = relationPath.split("\\.");
		List<? extends Model<?>> currentLevel = new ArrayList<>(parents);
		Class<?> currentModelClass = modelClass;

		for (String relation : relations) {
			if (currentLevel.isEmpty()) {
				break;
			}

			loadRelationForLevel(currentLevel, currentModelClass, relation);
			currentModelClass = getRelatedModelClass(currentModelClass, relation);
			currentLevel = getNextRelationLevel(currentLevel, relation);
		}
	}

	private Class<?> getRelatedModelClass(Class<?> modelCls, String relationName) {
		@SuppressWarnings("unchecked") ModelMetadata.RelationInfo relationInfo = ModelMetadata.get((Class<? extends Model<?>>) modelCls).getRelationInfo(relationName);

		return relationInfo.relatedClass();
	}

	private void loadRelationForLevel(List<? extends Model<?>> currentLevel, Class<?> currentModelClass, String relationName) {
		@SuppressWarnings("unchecked") ModelMetadata.RelationInfo relationInfo = ModelMetadata.get((Class<? extends Model<?>>) currentModelClass).getRelationInfo(relationName);

		switch (relationInfo.type()) {
			case OneToMany:
				loadOneToMany(currentLevel, relationInfo);
				break;
			case ManyToOne:
				loadManyToOne(currentLevel, relationInfo);
				break;
			case ManyToMany:
				loadManyToMany(currentLevel, relationInfo);
				break;
			case OneToOne:
				loadOneToOne(currentLevel, relationInfo);
				break;
		}
	}

	@SuppressWarnings("unchecked")
	private <R extends Model<R>> List<R> getRelatedModel(Model<?> modelCls, String relationName) {
		try {
			ModelMetadata.RelationInfo relationInfo = ModelMetadata.get(modelCls.getClassFinal()).getRelationInfo(relationName);

			Object value = relationInfo.field().get(modelCls);

			if (value instanceof List) {
				return (List<R>) value;
			}

			if (value instanceof Model) {
				return Collections.singletonList((R) value);
			}

			return Collections.emptyList();
		} catch (Exception e) {
			throw new RuntimeException("Error accessing relation field", e);
		}
	}

	private List<? extends Model<?>> getNextRelationLevel(List<? extends Model<?>> currentLevel, String relationName) {
		return currentLevel.stream().map(model -> getRelatedModel(model, relationName)).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
	}

	private <R extends Model<R>> void loadOneToMany(List<? extends Model<?>> parents, ModelMetadata.RelationInfo relationInfo) {
		@SuppressWarnings("unchecked") Class<R> relatedClass = (Class<R>) relationInfo.relatedClass();

		List<Object> parentIds = new ArrayList<>();
		List<Model<?>> parentsToLoad = new ArrayList<>();

		for (Model<?> parent : parents) {
			try {
				// Only load if relation isn't already populated
				if (relationInfo.field().get(parent) == null) {
					Object id = getFieldValue(parent, relationInfo.localKey());
					parentIds.add(id);
					parentsToLoad.add(parent);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		Map<Object, List<R>> grouped = whereIn(relatedClass, relationInfo.foreignKey(), parentIds).stream().collect(Collectors.groupingBy(child -> getFieldValue(child, relationInfo.foreignKey())));

		parentsToLoad.forEach(parent -> {
			Object key = getFieldValue(parent, relationInfo.localKey());
			setFieldValue(parent, relationInfo.field(), grouped.getOrDefault(key, Collections.emptyList()));
		});
	}

	@SuppressWarnings("unchecked")
	private <R extends Model<R>> void loadManyToOne(List<? extends Model<?>> children, ModelMetadata.RelationInfo relationInfo) {
		Class<R> relatedClass = (Class<R>) relationInfo.relatedClass();

		// Only load if relation isn't already populated
		List<Object> foreignKeys = new ArrayList<>();
		List<Model<?>> childrenToLoad = new ArrayList<>();

		for (Model<?> child : children) {
			try {
				// Check if relation already loaded
				if (relationInfo.field().get(child) == null) {
					Object fk = getFieldValue(child, relationInfo.localKey());
					if (fk != null) {
						foreignKeys.add(fk);
						childrenToLoad.add(child);
					}
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		if (foreignKeys.isEmpty()) return;

		Map<Object, R> mapped = whereIn(relatedClass, relationInfo.foreignKey(), foreignKeys).stream().collect(Collectors.toMap(Model::getIdValue, item -> item));

		childrenToLoad.forEach(child -> {
			Object fk = getFieldValue(child, relationInfo.localKey());
			setFieldValue(child, relationInfo.field(), mapped.get(fk));
		});
	}

	@SuppressWarnings("unchecked")
	private <R extends Model<R>> void loadManyToMany(List<? extends Model<?>> parents, ModelMetadata.RelationInfo relationInfo) {
		if (relationInfo.joinTable().isEmpty() || relationInfo.joinForeignKey().isEmpty() || relationInfo.joinRelatedKey().isEmpty()) {
			throw new RuntimeException("ManyToMany requires join config");
		}

		Class<R> relatedClass = (Class<R>) relationInfo.relatedClass();
		List<Object> parentIds = new ArrayList<>();

		for (Model<?> parent : parents) {
			try {
				// Only load if relation not already populated
				if (relationInfo.field().get(parent) == null) {
					parentIds.add(parent.getIdValue());
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		if (parentIds.isEmpty()) return;

		Map<Object, List<Object>> relationsMap = fetchJoinTableRelations(relationInfo.joinTable(), relationInfo.joinForeignKey(), relationInfo.joinRelatedKey(), parentIds);

		Set<Object> relatedIds = relationsMap.values().stream().flatMap(List::stream).collect(Collectors.toSet());

		Map<Object, R> relatedEntitiesMap = whereIn(relatedClass, "id", relatedIds).stream().collect(Collectors.toMap(Model::getIdValue, e -> e));

		parents.forEach(parent -> {
			List<Object> childIds = relationsMap.getOrDefault(parent.getIdValue(), Collections.emptyList());
			List<R> children = childIds.stream().map(relatedEntitiesMap::get).filter(Objects::nonNull).collect(Collectors.toList());
			setFieldValue(parent, relationInfo.field(), children);
		});
	}

	@SuppressWarnings("unchecked")
	private <R extends Model<R>> void loadOneToOne(List<? extends Model<?>> parents, ModelMetadata.RelationInfo relationInfo) {
		Class<R> relatedClass = (Class<R>) relationInfo.relatedClass();

		List<Object> parentKeys = new ArrayList<>();
		List<Model<?>> parentsToLoad = new ArrayList<>();

		for (Model<?> parent : parents) {
			try {
				// Skip if relation already loaded
				if (relationInfo.field().get(parent) == null) {
					Object key = getFieldValue(parent, relationInfo.localKey());
					if (key != null) {
						parentKeys.add(key);
						parentsToLoad.add(parent);
					}
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		if (parentKeys.isEmpty()) return;

		List<R> relatedModels = whereIn(relatedClass, relationInfo.foreignKey(), parentKeys);
		Map<Object, R> relatedMap = relatedModels.stream().collect(Collectors.toMap(model -> getFieldValue(model, relationInfo.foreignKey()), model -> model));

		parentsToLoad.forEach(parent -> {
			Object key = getFieldValue(parent, relationInfo.localKey());
			setFieldValue(parent, relationInfo.field(), relatedMap.get(key));
		});
	}

	private Map<Object, List<Object>> fetchJoinTableRelations(String joinTable, String joinForeignKey, String joinRelatedKey, List<Object> parentIds) {
		String placeholders = String.join(",", Collections.nCopies(parentIds.size(), "?"));

		//noinspection StringBufferReplaceableByString
		String query = new StringBuilder().append("SELECT ").append(joinForeignKey).append(", ").append(joinRelatedKey).append(" FROM ").append(joinTable).append(" WHERE ").append(joinForeignKey).append(" IN (").append(placeholders).append(")").toString();

		try (Connection conn = Model.dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			for (int i = 0; i < parentIds.size(); i++) {
				stmt.setObject(i + 1, parentIds.get(i));
			}

			ResultSet rs = stmt.executeQuery();
			Map<Object, List<Object>> result = new HashMap<>();
			while (rs.next()) {
				Object parentId = rs.getObject(joinForeignKey);
				Object relatedId = rs.getObject(joinRelatedKey);
				result.computeIfAbsent(parentId, k -> new ArrayList<>()).add(relatedId);
			}
			return result;
		} catch (SQLException e) {
			throw new RuntimeException("Error fetching many-to-many relations", e);
		}
	}

	private Object getFieldValue(Model<?> model, String fieldName) {
		try {
			Field field = model.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(model);
		} catch (Exception e) {
			throw new RuntimeException("Error getting field value '" + fieldName + "' for " + model.getClass().getName() + ": " + e.getMessage(), e);
		}
	}

	private void setFieldValue(Model<?> model, Field field, Object value) {
		try {
			field.set(model, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error setting field value", e);
		}
	}

	private <R extends Model<R>> List<R> whereIn(Class<R> clazz, String column, Collection<?> values) {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		String placeholders = String.join(",", Collections.nCopies(values.size(), "?"));

		//noinspection StringBufferReplaceableByString
		String sql = new StringBuilder().append("SELECT * FROM ").append(ModelMetadata.get(clazz).tableName).append(" WHERE ").append(column).append(" IN (").append(placeholders).append(")").toString();

		try (Connection conn = Model.dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			int index = 1;

			for (Object value : values) {
				stmt.setObject(index++, value);
			}
			return DatabaseOperations.createInstances(clazz, stmt.executeQuery());
		} catch (Throwable e) {
			throw new RuntimeException("Error executing whereIn query '" + sql + "' for " + clazz.getSimpleName() + ": " + e.getMessage(), e);
		}
	}

	private record Condition(String column, String operator, Object value) {
		String toSql() {
			return "%s %s ?".formatted(column, operator);
		}
	}

	private record Order(String column, String direction) {
		Order {
			direction = direction.toUpperCase();
		}

		String toSql() {
			return "%s %s".formatted(column, direction);
		}
	}

}