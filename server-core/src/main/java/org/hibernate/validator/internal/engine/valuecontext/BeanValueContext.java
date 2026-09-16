/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valuecontext;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.path.MutablePath;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.stereotypes.Lazy;

/**
 * @author Marko Bekhta
 */
public final class BeanValueContext<T, V> extends ValueContext<T, V> {

	/**
	 * The metadata of the current bean.
	 */
	private BeanMetaData<?> currentBeanMetaData;

	/**
	 * When we check whether the bean was validated we need to check that it was validated for the requested group.
	 * This set tracks the groups we've already processed this bean for.
	 */
	@Lazy
	private Set<Class<?>> alreadyProcessedGroups;

	/**
	 * To track when the constraint is in multiple groups, and it was already processed for some other group.
	 */
	@Lazy
	private final Map<Object, Map<String, Map<MetaConstraint<?>, Boolean>>> processedConstraints;

    private Map<MetaConstraint<?>, Boolean> constraintsForCurrentPath() {
        return processedConstraints.computeIfAbsent(currentBean, bean -> new java.util.HashMap<>())
                .computeIfAbsent(propertyPath.asString(), path -> new IdentityHashMap<>());
    }

	BeanValueContext(ValueContext<?, ?> parentContext, ExecutableParameterNameProvider parameterNameProvider, T currentBean, BeanMetaData<T> currentBeanMetaData, MutablePath propertyPath) {
		super( parentContext, parameterNameProvider, currentBean, currentBeanMetaData, propertyPath );
		this.currentBeanMetaData = currentBeanMetaData;
        // Share only within this validation's context tree, never across calls or threads.
        // OneDev revisits cascaded beans during its property/cascade/class phases.
        this.processedConstraints = parentContext instanceof BeanValueContext<?, ?> parent
                ? parent.processedConstraints : new IdentityHashMap<>();
	}

	@SuppressWarnings("unchecked")
	public BeanMetaData<T> getCurrentBeanMetaData() {
		return (BeanMetaData<T>) currentBeanMetaData;
	}

	public void reset(Object currentBean, MutablePath propertyPath, BeanMetaData<?> currentBeanMetaData) {
		this.currentBeanMetaData = currentBeanMetaData;
		this.currentValidatable = currentBeanMetaData;
		this.currentBean = currentBean;
		this.propertyPath = propertyPath;
		this.alreadyProcessedGroups = null;
		this.currentGroup = null;
		this.previousGroup = null;
	}

	@Override
	public boolean isBeanAlreadyValidated(Object value, Class<?> group) {
		ValueContext<?, ?> curr = this;
		while ( curr != null ) {
			if ( curr.currentBean == value ) {
				return curr.isProcessedForGroup( group );
			}
			curr = curr.parentContext;
		}
		return false;
	}

	@Override
	public void markCurrentGroupAsProcessed() {
		// if we just validate the default/single group it doesn't make sense to track it beyond the "current group" value
		if ( this.previousGroup != null && this.previousGroup != this.currentGroup ) {
			if ( this.alreadyProcessedGroups == null ) {
				this.alreadyProcessedGroups = new HashSet<>();
				this.alreadyProcessedGroups.add( this.previousGroup );
			}
			this.alreadyProcessedGroups.add( this.currentGroup );
		}
	}

	@Override
	protected boolean isProcessedForGroup(Class<?> group) {
		return group == this.currentGroup || ( this.alreadyProcessedGroups != null && alreadyProcessedGroups.contains( group ) );
	}

	@Override
	public void markConstraintProcessed(MetaConstraint<?> metaConstraint) {
		constraintsForCurrentPath().put(metaConstraint, Boolean.TRUE);
	}

	@Override
	public boolean hasMetaConstraintBeenProcessed(MetaConstraint<?> metaConstraint) {
		return constraintsForCurrentPath().containsKey(metaConstraint);
	}
}
