
package com.crimson.bakuretsu.annotations;

import com.crimson.bakuretsu.core.Model;
import com.crimson.bakuretsu.enums.RelationTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Relation {
	RelationTypes type();

	Class<? extends Model<?>> related();

	String joinTable() default "";

	String joinForeignKey() default "";

	String joinRelatedKey() default "";

	String foreignKey() default "";

	String localKey() default "";
}
