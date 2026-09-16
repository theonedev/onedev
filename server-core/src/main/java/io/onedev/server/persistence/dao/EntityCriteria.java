package io.onedev.server.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.query.Query;

import io.onedev.server.model.AbstractEntity;

/** Serializable query description, independent of a Hibernate session. */
public class EntityCriteria<T extends AbstractEntity> implements Serializable {
    private final Class<T> entityClass;
    private final String alias;
    private final List<Criterion> restrictions = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Association> associations = new ArrayList<>();
    private boolean cacheable;
    private CacheMode cacheMode;
    private String cacheRegion;

    private record Association(String path, JoinType joinType, EntityCriteria<?> criteria) implements Serializable {}

    private EntityCriteria(Class<T> entityClass, String alias) {
        this.entityClass = entityClass;
        this.alias = alias;
    }

    public static <T extends AbstractEntity> EntityCriteria<T> of(Class<T> type) {
        return of(type, null);
    }

    public static <T extends AbstractEntity> EntityCriteria<T> of(Class<T> type, String alias) {
        return new EntityCriteria<>(type, alias);
    }

    public EntityCriteria<T> add(Criterion criterion) { restrictions.add(criterion); return this; }
    public EntityCriteria<T> addOrder(Order order) { orders.add(order); return this; }
    public void setCacheable(boolean cacheable) { this.cacheable = cacheable; }
    public void setCacheMode(CacheMode cacheMode) { this.cacheMode = cacheMode; }
    public void setCacheRegion(String cacheRegion) { this.cacheRegion = cacheRegion; }
    public String getAlias() { return alias; }

    public EntityCriteria<T> createAlias(String path, String alias, JoinType joinType) {
        associations.add(new Association(path, joinType, new EntityCriteria<>(entityClass, alias)));
        return this;
    }

    public EntityCriteria<T> createCriteria(String path) { return createCriteria(path, JoinType.INNER); }

    public EntityCriteria<T> createCriteria(String path, JoinType joinType) {
        var child = new EntityCriteria<>(entityClass, null);
        associations.add(new Association(path, joinType, child));
        return child;
    }

    private Path<?> path(From<?, ?> from, String name, Map<String, From<?, ?>> aliases) {
        String[] parts = name.split("\\.");
        Path<?> result = aliases.getOrDefault(parts[0], from);
        int first = aliases.containsKey(parts[0]) ? 1 : 0;
        for (int i = first; i < parts.length; i++) result = result.get(parts[i]);
        return result;
    }

    private void collect(CriteriaBuilder builder, From<?, ?> from, Map<String, From<?, ?>> aliases,
            List<Predicate> predicates, List<jakarta.persistence.criteria.Order> queryOrders) {
        if (alias != null) aliases.put(alias, from);
        // Register aliases before evaluating restrictions, which may refer to them.
        Map<Association, From<?, ?>> joins = new HashMap<>();
        for (var association : associations) {
            String[] parts = association.path().split("\\.");
            From<?, ?> join = aliases.getOrDefault(parts[0], from);
            int first = aliases.containsKey(parts[0]) ? 1 : 0;
            for (int i = first; i < parts.length; i++) join = join.join(parts[i], association.joinType());
            joins.put(association, join);
            if (association.criteria().alias != null) aliases.put(association.criteria().alias, join);
        }
        Function<String, Path<?>> paths = name -> path(from, name, aliases);
        for (var restriction : restrictions) predicates.add(restriction.toPredicate(builder, paths));
        for (var order : orders) queryOrders.add(order.ascending()
                ? builder.asc(paths.apply(order.property())) : builder.desc(paths.apply(order.property())));
        for (var association : associations)
            association.criteria().collect(builder, joins.get(association), aliases, predicates, queryOrders);
    }

    private <R> Query<R> build(Session session, CriteriaQuery<R> criteria, Root<T> root, boolean count) {
        var predicates = new ArrayList<Predicate>();
        var queryOrders = new ArrayList<jakarta.persistence.criteria.Order>();
        collect(session.getCriteriaBuilder(), root, new HashMap<>(), predicates, queryOrders);
        criteria.where(predicates.toArray(Predicate[]::new));
        if (!count) criteria.orderBy(queryOrders);
        Query<R> query = session.createQuery(criteria).setCacheable(cacheable);
        if (cacheMode != null) query.setCacheMode(cacheMode);
        if (cacheRegion != null) query.setCacheRegion(cacheRegion);
        return query;
    }

    public Query<T> getExecutableCriteria(Session session) {
        var criteria = session.getCriteriaBuilder().createQuery(entityClass);
        Root<T> root = criteria.from(entityClass);
        criteria.select(root);
        return build(session, criteria, root, false);
    }

    public long count(Session session) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(Long.class);
        Root<T> root = criteria.from(entityClass);
        criteria.select(builder.count(root));
        return build(session, criteria, root, true).getSingleResult();
    }
}
