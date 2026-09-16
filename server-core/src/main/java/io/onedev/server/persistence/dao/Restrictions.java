package io.onedev.server.persistence.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import jakarta.persistence.criteria.Expression;

public final class Restrictions {
    private Restrictions() {}
    public static Criterion eq(String property, Object value) {
        return (builder, paths) -> builder.equal(paths.apply(property), value);
    }
    public static Criterion eqProperty(String first, String second) {
        return (builder, paths) -> builder.equal(paths.apply(first), paths.apply(second));
    }
    public static Criterion isNull(String property) {
        return (builder, paths) -> builder.isNull(paths.apply(property));
    }
    public static Criterion in(String property, Collection<?> values) {
        return (builder, paths) -> paths.apply(property).in(values);
    }
    public static Criterion in(String property, Object[] values) {
        return in(property, Arrays.asList(values));
    }
    public static Criterion not(Criterion criterion) {
        return (builder, paths) -> builder.not(criterion.toPredicate(builder, paths));
    }
    public static Criterion and(Criterion... criteria) {
        return (builder, paths) -> builder.and(Arrays.stream(criteria)
                .map(it -> it.toPredicate(builder, paths)).toArray(jakarta.persistence.criteria.Predicate[]::new));
    }
    public static Criterion or(Criterion... criteria) {
        return (builder, paths) -> builder.or(Arrays.stream(criteria)
                .map(it -> it.toPredicate(builder, paths)).toArray(jakarta.persistence.criteria.Predicate[]::new));
    }
    public static Criterion ilike(String property, String value) {
        return ilike(property, value, MatchMode.EXACT);
    }
    public static Criterion ilike(String property, String value, MatchMode mode) {
        String pattern = mode == MatchMode.ANYWHERE ? "%" + value + "%" : value;
        return (builder, paths) -> builder.like(builder.lower(paths.apply(property).as(String.class)),
                pattern.toLowerCase(Locale.ROOT));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Criterion gt(String property, Comparable<?> value) {
        return (builder, paths) -> builder.greaterThan((Expression) paths.apply(property), (Comparable) value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Criterion ge(String property, Comparable<?> value) {
        return (builder, paths) -> builder.greaterThanOrEqualTo((Expression) paths.apply(property), (Comparable) value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Criterion lt(String property, Comparable<?> value) {
        return (builder, paths) -> builder.lessThan((Expression) paths.apply(property), (Comparable) value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Criterion le(String property, Comparable<?> value) {
        return (builder, paths) -> builder.lessThanOrEqualTo((Expression) paths.apply(property), (Comparable) value);
    }
}
