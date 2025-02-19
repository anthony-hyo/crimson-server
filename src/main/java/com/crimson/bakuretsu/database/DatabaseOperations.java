
package com.crimson.bakuretsu.database;

import com.crimson.bakuretsu.cache.CacheManager;
import com.crimson.bakuretsu.core.Model;
import com.crimson.bakuretsu.core.ModelMetadata;
import com.crimson.exceptions.FieldSetException;
import com.crimson.exceptions.InvalidFieldValueException;
import org.joda.time.LocalDateTime;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DatabaseOperations {

	private DatabaseOperations() {
	}

	public static <R extends Model<R>> Optional<R> fetchEntity(Class<R> clazz, Object id) throws Throwable {
		ModelMetadata metadata = ModelMetadata.get(clazz);

		String query = String.format("SELECT * FROM %s WHERE %s = ? LIMIT 1", metadata.tableName, metadata.idColumnName);

		try (Connection conn = Model.dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setObject(1, id);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					R entity = createInstance(clazz, rs);
					CacheManager.cacheEntity(clazz, id, entity);
					return Optional.of(entity);
				}
			}
		}

		return Optional.empty();
	}

	public static <R extends Model<?>> List<R> createInstances(Class<R> clazz, ResultSet rs) throws Throwable {
		List<R> instances = new ArrayList<>();

		while (rs.next()) {
			instances.add(createInstance(clazz, rs));
		}

		return Collections.unmodifiableList(instances);
	}

	public static <R extends Model<?>> R createInstance(Class<R> clazz, ResultSet rs) throws Throwable {
		ModelMetadata metadata = ModelMetadata.get(clazz);
		R entity = clazz.cast(metadata.constructor.get());

		for (Field field : metadata.fields) {
			setFieldFromResultSet(metadata, entity, field, rs);
		}

		return entity;
	}

	private static <R extends Model<?>> void setFieldFromResultSet(ModelMetadata metadata, R entity, Field field, ResultSet rs) throws Throwable {
		try {
			MethodHandle setter = metadata.fieldSetters.get(field);
			setter.invoke(entity, convertValue(field, rs));
		} catch (Throwable e) {
			throw new FieldSetException(String.format("Failed to set field '%s' for entity '%s'. Column: '%s'.\nError: %s", field.getName(), metadata.className, ModelMetadata.resolveColumnName(field), e.getMessage()), e);
		}
	}

	private static Object convertValue(Field field, ResultSet rs) throws SQLException {
		String columnName = ModelMetadata.resolveColumnName(field);
		Object value = rs.getObject(columnName);

		Class<?> targetType = field.getType();

		if (value == null) {
			if (targetType.isPrimitive()) {
				throw new InvalidFieldValueException("Cannot assign null to primitive type: " + targetType.getName());
			}
			return null;
		}

		if (targetType.isAssignableFrom(value.getClass())) {
			return value;
		}

		return switch (targetType.getName()) {
			case "boolean", "java.lang.Boolean" -> rs.getBoolean(columnName);
			case "java.util.UUID" -> UUID.fromString((String) value);
			case "java.lang.Integer", "int" -> rs.getInt(columnName);
			case "java.lang.Long", "long" -> rs.getLong(columnName);
			case "java.time.LocalDate" -> rs.getDate(columnName).toLocalDate();
			case "java.time.LocalDateTime" -> LocalDateTime.parse((String) value);
			case "java.lang.Double", "double" -> rs.getDouble(columnName);
			case "java.lang.Float", "float" -> rs.getFloat(columnName);
			case "java.math.BigDecimal" -> rs.getBigDecimal(columnName);
			//TODO: JSON ARRAY Type
			//TODO: JSON ELEMENT Type
			default -> value;
		};
	}

}
