
package com.crimson.bakuretsu.core;

import com.crimson.bakuretsu.annotations.*;
import com.crimson.bakuretsu.enums.RelationTypes;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModelMetadata {

	private static final Map<Class<? extends Model<?>>, ModelMetadata> METADATA_CACHE = new ConcurrentHashMap<>();

	public final boolean isCached;

	public final String tableName;
	public final String className;

	public final List<Field> fields;
	private final Map<String, RelationInfo> relationFields;

	public final Field idField;
	public final String idColumnName;

	public final Supplier<?> constructor;

	public final List<Field> insertFields;

	public final String insertQuery;
	public final String updateQuery;

	public final Map<Field, MethodHandle> fieldGetters;
	public final Map<Field, MethodHandle> fieldSetters;

	public final MethodHandle idGetter;
	public final MethodHandle idSetter;

	public ModelMetadata(Class<? extends Model<?>> clazz) {
		this.isCached = clazz.isAnnotationPresent(Cached.class);

		//region Resolve Name
		Table table = clazz.getAnnotation(Table.class);

		if (table == null) {
			throw new RuntimeException("Missing @Table annotation on " + clazz.getName());
		}

		this.tableName = table.value();
		this.className = clazz.getName();
		//endregion

		//region Scan Fields
		this.fields = Arrays.stream(clazz.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Column.class)).toList();
		//endregion

		//region Build relations
		try {
			MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());

			this.relationFields = Arrays.stream(clazz.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(Relation.class))
				.peek(f -> f.setAccessible(true))
				.collect(Collectors.toMap(
					Field::getName,
					field -> {
						Relation relation = field.getAnnotation(Relation.class);

						try {
							return new RelationInfo(
								relation,
								field,
								lookup.unreflectGetter(field),
								lookup.unreflectSetter(field)
							);
						} catch (IllegalAccessException e) {
							throw new RuntimeException("Failed to create method handles for field: " + field.getName(), e);
						}
					}
				));
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to build relations metadata", e);
		}
		//endregion

		//region Find ID Field
		this.idField = Arrays.stream(clazz.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Id.class))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("ID field not found in " + clazz.getName()));
		//endregion

		//region Resolve ColumnName
		this.idColumnName = idField.getAnnotation(Column.class).name();
		//endregion

		//region Create Constructor
		try {
			MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
			MethodHandle mh = lookup.findConstructor(clazz, MethodType.methodType(void.class));

			this.constructor = () -> {
				try {
					return mh.invoke();
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			};
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		//endregion

		//region Build relations
		this.insertFields = fields.stream().filter(f -> !f.equals(idField)).toList();
		//endregion

		//region Build Insert Query
		String columns = insertFields.stream()
			.map(f -> f.getAnnotation(Column.class).name())
			.collect(Collectors.joining(", "));

		String placeholders = "?".repeat(insertFields.size());

		this.insertQuery = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
		//endregion

		//region Build Update Query
		String setClause = insertFields.stream()
			.map(f -> f.getAnnotation(Column.class).name() + " = ?")
			.collect(Collectors.joining(", "));

		this.updateQuery = "UPDATE " + tableName + " SET " + setClause + " WHERE " + idColumnName + " = ?";
		//endregion

		//region Build Field Getters && Build Field Setters && Build ID Getter & Build ID Setter
		try {
			MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());

			this.fieldGetters = new HashMap<>();
			this.fieldSetters = new HashMap<>();

			for (Field field : fields) {
				this.fieldGetters.put(field, lookup.unreflectGetter(field));
				this.fieldSetters.put(field, lookup.unreflectSetter(field));
			}

			this.idGetter = lookup.unreflectGetter(idField);
			this.idSetter = lookup.unreflectSetter(idField);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to create field accessors", e);
		}
		//endregion
	}

	public static ModelMetadata get(Class<? extends Model<?>> clazz) {
		return METADATA_CACHE.computeIfAbsent(clazz, ModelMetadata::new);
	}

	public static String resolveColumnName(Field field) {
		return field.getAnnotation(Column.class).name();
	}

	public List<Field> getRelationFields() {
		return relationFields.values().stream()
			.map(info -> info.field)
			.toList();
	}

	public RelationInfo getRelationInfo(String relationName) {
		RelationInfo info = relationFields.get(relationName);

		if (info == null) {
			throw new RuntimeException("Relation not found: " + relationName);
		}

		return info;
	}

	public List<String> getRelationNames() {
		return new ArrayList<>(relationFields.keySet());
	}

	public List<RelationInfo> getRelationsByType(RelationTypes type) {
		return relationFields.values().stream()
			.filter(info -> info.type == type)
			.collect(Collectors.toList());
	}

	public record RelationInfo(
		RelationTypes type,

		Class<?> relatedClass,

		Field field,

		String foreignKey,
		String localKey,
		String joinTable,
		String joinForeignKey,
		String joinRelatedKey,

		MethodHandle getter,
		MethodHandle setter
	) {

		public RelationInfo(Relation relation, Field field, MethodHandle getter, MethodHandle setter) {
			this(
				relation.type(),
				relation.related(),
				field,
				relation.foreignKey(),
				relation.localKey(),
				relation.joinTable(),
				relation.joinForeignKey(),
				relation.joinRelatedKey(),
				getter,
				setter
			);
		}

		public Object getValue(Model<?> model) {
			try {
				return this.getter.invoke(model);
			} catch (Throwable e) {
				throw new RuntimeException("Error getting relation value", e);
			}
		}

		public void setValue(Model<?> model, Object value) {
			try {
				this.setter.invoke(model, value);
			} catch (Throwable e) {
				throw new RuntimeException("Error setting relation value", e);
			}
		}

	}

}