package io.onedev.server.persistence.dao;

import java.io.Serializable;
import java.util.function.Function;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/** A serializable restriction translated into Jakarta Criteria when executed. */
@FunctionalInterface
public interface Criterion extends Serializable {
    Predicate toPredicate(CriteriaBuilder builder, Function<String, Path<?>> paths);
}
