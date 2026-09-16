package io.onedev.server.persistence.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import io.onedev.server.persistence.IdGenerator;

/** Allocates OneDev identifiers while preserving IDs supplied by imports and system records. */
@IdGeneratorType(IdGenerator.class)
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface EntityId {
}
