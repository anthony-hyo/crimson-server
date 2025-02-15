
package com.crimson.bakuretsu.core;

import com.crimson.bakuretsu.annotations.Cached;
import com.crimson.bakuretsu.cache.CacheManager;
import com.crimson.bakuretsu.database.DatabaseOperations;
import com.crimson.bakuretsu.enums.RelationTypes;
import com.crimson.bakuretsu.query.QueryBuilder;
import com.zaxxer.hikari.HikariDataSource;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class Model<M extends Model<M>> {

	public static HikariDataSource dataSource;

	public static <R extends Model<R>> QueryBuilder<R> query(Class<R> clazz) {
		return new QueryBuilder<>(clazz);
	}

	public static <R extends Model<R>> List<R> all(Class<R> clazz) {
		return query(clazz).get();
	}

	public static <R extends Model<R>> Optional<R> findFirst(Class<R> clazz, Object id) {
		return query(clazz).find(id);
	}

	public static <R extends Model<R>> List<R> whereIn(Class<R> clazz, String column, Collection<?> values) {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		String query = String.format("SELECT * FROM %s WHERE %s IN (%s)", ModelMetadata.get(clazz).tableName, column, String.join(",", Collections.nCopies(values.size(), "?")));

		try (Connection conn = dataSource.getConnection();
		     PreparedStatement stmt = conn.prepareStatement(query)) {

			int index = 1;
			for (Object value : values) {
				stmt.setObject(index++, value);
			}

			return DatabaseOperations.createInstances(clazz, stmt.executeQuery());
		} catch (Throwable e) {
			throw new RuntimeException("Error executing whereIn query", e);
		}
	}

	public static <R extends Model<R>> Optional<R> findById(Class<R> clazz, Object id) {
		return CacheManager.getCachedEntity(clazz, id).or(() -> {
			try {
				return DatabaseOperations.fetchEntity(clazz, id);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static <R extends Model<R>> CompletableFuture<Optional<R>> findByIdAsync(Class<R> clazz, Object id) {
		return CompletableFuture.supplyAsync(() -> findById(clazz, id));
	}

	public static <R extends Model<R>> List<R> findAll(Class<R> clazz) {
		try (Connection conn = dataSource.getConnection()) {
			String query = "SELECT * FROM " + ModelMetadata.get(clazz).tableName;
			try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
				List<R> results = new ArrayList<>();

				while (rs.next()) {
					R entity = DatabaseOperations.createInstance(clazz, rs);
					CacheManager.cacheEntity(clazz, entity.getIdValue(), entity);
					results.add(entity);
				}

				return Collections.unmodifiableList(results);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static <R extends Model<R>> List<R> whereInId(Class<R> clazz, List<Object> ids) {
		List<R> results = new ArrayList<>();
		for (Object id : ids) {
			findById(clazz, id).ifPresent(results::add);
		}
		return results;
	}

	public static <R extends Model<R>> void prefetchAsync(Class<R> clazz, List<Object> ids) {
		ids.parallelStream().forEach(id -> findByIdAsync(clazz, id));
	}

	public static <R extends Model<R>> boolean exists(Class<R> clazz, Object id) {
		return findById(clazz, id).isPresent();
	}

	//TODO: Fix and Test; or delete
	public static <R extends Model<R>> List<R> findBySQL(Class<R> clazz, String sql, Object... params) {
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}

			try (ResultSet rs = stmt.executeQuery()) {
				return DatabaseOperations.createInstances(clazz, rs);
			}
		} catch (Throwable e) {
			throw new RuntimeException("Error executing findBySQL query", e);
		}
	}

	//TODO: Fix and Test; or delete
	public static <R extends Model<R>> List<R> find(Class<R> clazz, String condition, Object... params) {
		String query = "SELECT * FROM " + ModelMetadata.get(clazz).tableName + " WHERE " + condition;

		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}

			return DatabaseOperations.createInstances(clazz, stmt.executeQuery());
		} catch (Throwable e) {
			throw new RuntimeException("Error executing find", e);
		}
	}

	//TODO: Fix and Test; or delete
	public static <R extends Model<R>> void delete(Class<R> clazz, String condition, Object... params) {
		String query = "DELETE FROM " + ModelMetadata.get(clazz).tableName + " WHERE " + condition;

		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}
			stmt.executeUpdate();
		} catch (Throwable e) {
			throw new RuntimeException("Error executing delete", e);
		}
	}

	//TODO: Fix and Test; or delete
	public static <R extends Model<R>> Optional<R> findFirst(Class<R> clazz, String condition, Object... params) {
		String query = "SELECT * FROM " + ModelMetadata.get(clazz).tableName + " WHERE " + condition + " LIMIT 1";

		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}

			List<R> results = DatabaseOperations.createInstances(clazz, stmt.executeQuery());
			return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
		} catch (Throwable e) {
			throw new RuntimeException("Error executing findFirst", e);
		}
	}

	//TODO: Fix and Test; or delete
	public static <R extends Model<R>> long count(Class<R> clazz) {
		String query = "SELECT COUNT(*) FROM " + ModelMetadata.get(clazz).tableName;

		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			return 0;
		} catch (Throwable e) {
			throw new RuntimeException("Error counting entities", e);
		}
	}

	//TODO: Fix and Test; or delete
	public static <R extends Model<R>> long count(Class<R> clazz, String condition, Object... params) {
		String query = "SELECT COUNT(*) FROM " + ModelMetadata.get(clazz).tableName + " WHERE " + condition;

		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
			}
			return 0;
		} catch (Throwable e) {
			throw new RuntimeException("Error executing count", e);
		}
	}

	//TODO: Fix and Test; or delete
	public static <R extends Model<R>> void updateAll(Class<R> clazz, String setClause, Object... params) {
		String query = "UPDATE " + ModelMetadata.get(clazz).tableName + " SET " + setClause;

		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}
			stmt.executeUpdate();
		} catch (Throwable e) {
			throw new RuntimeException("Error executing updateAll", e);
		}
	}

	//TODO: Fix and Test; or delete
	public static <R extends Model<R>> void purgeCache(Class<R> npcClass) {
	}

	//TODO: Fix and Test; or delete
	public <R extends Model<R>> Optional<R> includeOneOf(Class<R> targetEntityClass, String relationField) {
		try (Connection conn = dataSource.getConnection()) {
			Field relation = getClassFinal().getDeclaredField(relationField);
			relation.setAccessible(true);
			Object relatedId = relation.get(this);

			return relatedId == null ? Optional.empty() : findById(targetEntityClass, relatedId);
		} catch (Throwable e) {
			throw new RuntimeException("Error loading relation", e);
		}
	}

	//TODO: Fix and Test; or delete
	public <R extends Model<R>> List<R> includeManyOf(Class<R> targetEntityClass) {
		try (Connection conn = dataSource.getConnection()) {
			String foreignKeyColumn = ModelMetadata.get(targetEntityClass)
				.getRelationsByType(RelationTypes.ManyToOne).stream()
				.filter(relationInfo -> relationInfo.relatedClass().equals(getClassFinal()))
				.map(ModelMetadata.RelationInfo::foreignKey)
				.findFirst()
				.orElseThrow();

			String query = "SELECT * FROM " + ModelMetadata.get(targetEntityClass).tableName + " WHERE " + foreignKeyColumn + " = ?";

			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setObject(1, getIdValue());

				try (ResultSet rs = stmt.executeQuery()) {
					List<R> relatedEntities = new ArrayList<>();

					while (rs.next()) {
						R relatedEntity = DatabaseOperations.createInstance(targetEntityClass, rs);
						relatedEntities.add(relatedEntity);
					}

					return relatedEntities;
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException("Error loading related list", e);
		}
	}

	private void insert() {
		ModelMetadata metadata = ModelMetadata.get(getClassFinal());
		String query = metadata.insertQuery;

		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			bindParameters(stmt, false);
			stmt.executeUpdate();
			handleGeneratedKeys(stmt);
		} catch (Exception e) {
			throw new RuntimeException("Error inserting entity", e);
		}
	}

	private void update() {
		ModelMetadata metadata = ModelMetadata.get(getClassFinal());
		String query = metadata.updateQuery;

		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			bindParameters(stmt, false);
			stmt.setObject(metadata.insertFields.size() + 1, getIdValue());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException("Error updating entity", e);
		}
	}

	public void delete() {
		String query = "DELETE FROM " + getTableName() + " WHERE " + ModelMetadata.get(getClassFinal()).idColumnName + " = ?";

		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setObject(1, getIdValue());
			stmt.executeUpdate();

			if (getClassFinal().isAnnotationPresent(Cached.class)) {
				CacheManager.getCache(getClassFinal()).invalidate(getIdValue());
			}
		} catch (Exception e) {
			throw new RuntimeException("Error deleting entity", e);
		}
	}

	public void refresh() {
		findById(getClassFinal(), getIdValue())
			.ifPresent(newEntity -> {
				for (Field field : ModelMetadata.get(getClassFinal()).fields) {
					try {
						field.setAccessible(true);
						field.set(this, field.get(newEntity));
					} catch (IllegalAccessException e) {
						throw new RuntimeException("Error refreshing entity", e);
					}
				}
			});
	}

	@SuppressWarnings("unchecked")
	public void save() {
		if (isNewRecord()) {
			insert();
		} else {
			update();
		}

		CacheManager.cacheEntity(getClassFinal(), getIdValue(), (M) this);
	}

	private void bindParameters(PreparedStatement stmt, boolean includeId) throws SQLException {
		ModelMetadata metadata = ModelMetadata.get(getClassFinal());
		List<Field> fields = includeId ? metadata.fields : metadata.insertFields;

		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			try {
				MethodHandle getter = metadata.fieldGetters.get(field);
				Object value = getter.invoke(this);
				stmt.setObject(i + 1, value);
			} catch (Throwable e) {
				throw new SQLException("Error binding parameter", e);
			}
		}
	}

	private void handleGeneratedKeys(PreparedStatement stmt) throws SQLException {
		ModelMetadata metadata = ModelMetadata.get(getClassFinal());
		try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
			if (generatedKeys.next()) {
				Class<?> fieldType = metadata.idField.getType();

				Object value = generatedKeys.getObject(1);

				if (value instanceof Number number) {
					if (fieldType == long.class || fieldType == Long.class) {
						value = number.longValue();
					} else if (fieldType == int.class || fieldType == Integer.class) {
						value = number.intValue();
					}
				} else if (fieldType == UUID.class && value instanceof String) {
					value = UUID.fromString((String) value);
				}

				try {
					metadata.idSetter.invoke(this, value);
				} catch (Throwable e) {
					throw new SQLException("Error setting ID value", e);
				}
			}
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(getIdValue());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Model<?> other = (Model<?>) o;
		return Objects.equals(getIdValue(), other.getIdValue());
	}

	public Object getIdValue() {
		try {
			return ModelMetadata.get(getClassFinal()).idGetter.invoke(this);
		} catch (Throwable e) {
			e.getStackTrace();
			throw new RuntimeException("Error getting ID", e);
		}
	}

	private String getTableName() {
		return ModelMetadata.get(getClassFinal()).tableName;
	}

	private boolean isNewRecord() {
		Object id = getIdValue();
		if (id == null) {
			return true;
		}

		if (id instanceof Number) {
			return ((Number) id).longValue() == 0;
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public Class<M> getClassFinal() {
		return (Class<M>) getClass();
	}

	public static void setDataSource(HikariDataSource ds) {
		dataSource = ds;
	}

}