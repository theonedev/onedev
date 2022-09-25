/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;

/**
 * Builds {@link ConstraintMetaData} instances for the
 * {@link ConstrainedElement} objects representing one method or property in a
 * type's inheritance hierarchy.
 *
 * @author Gunnar Morling
 */
public abstract class MetaDataBuilder {

	protected final ConstraintCreationContext constraintCreationContext;

	private final Class<?> beanClass;
	private final Set<MetaConstraint<?>> directConstraints = newHashSet();
	private final Set<MetaConstraint<?>> containerElementsConstraints = newHashSet();
	private boolean isCascading = false;

	protected MetaDataBuilder(Class<?> beanClass, ConstraintCreationContext constraintCreationContext) {
		this.beanClass = beanClass;
		this.constraintCreationContext = constraintCreationContext;
	}

	/**
	 * Whether this builder allows to add the given element or not. This is the
	 * case if the specified element relates to the same property or method with
	 * which this builder was instantiated.
	 *
	 * @param constrainedElement The element to check.
	 *
	 * @return {@code true} if the given element can be added to this
	 *         builder, {@code false} otherwise.
	 */
	public abstract boolean accepts(ConstrainedElement constrainedElement);

	/**
	 * Adds the given element to this builder. It must be checked with
	 * {@link #accepts(ConstrainedElement)} before, whether this is allowed or
	 * not.
	 *
	 * @param constrainedElement The element to add.
	 */
	public void add(ConstrainedElement constrainedElement) {
		/*
		 * Make sure child annotation can override parent annotation with same type
		 */
		for (MetaConstraint<?> constraint: adaptConstraints(constrainedElement, constrainedElement.getConstraints())) {
			if (!directConstraints.stream()
					.filter(it->it.getDescriptor().getAnnotationType() == constraint.getDescriptor().getAnnotationType())
					.findAny().isPresent()) {
				directConstraints.add(constraint);
			}
		}
//		directConstraints.addAll( adaptConstraints( constrainedElement, constrainedElement.getConstraints() ) );
		containerElementsConstraints.addAll( adaptConstraints( constrainedElement, constrainedElement.getTypeArgumentConstraints() ) );
		isCascading = isCascading || constrainedElement.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements();
	}

	/**
	 * Creates a new, read-only {@link ConstraintMetaData} object with all
	 * constraint information related to the method or property represented by
	 * this builder.
	 *
	 * @return A {@link ConstraintMetaData} object.
	 */
	public abstract ConstraintMetaData build();

	protected Set<MetaConstraint<?>> getDirectConstraints() {
		return directConstraints;
	}

	public Set<MetaConstraint<?>> getContainerElementConstraints() {
		return containerElementsConstraints;
	}

	protected boolean isCascading() {
		return isCascading;
	}

	protected Class<?> getBeanClass() {
		return beanClass;
	}

	/**
	 * Adapts the given constraints to the given bean type. In case a constraint
	 * is defined locally at the bean class the original constraint will be
	 * returned without any modifications. If a constraint is defined in the
	 * hierarchy (interface or super class) a new constraint will be returned
	 * with an origin of {@link org.hibernate.validator.internal.metadata.core.ConstraintOrigin#DEFINED_IN_HIERARCHY}. If a
	 * constraint is defined on an interface, the interface type will
	 * additionally be part of the constraint's groups (implicit grouping).
	 *
	 * @param constraints The constraints that shall be adapted. The constraints themselves
	 * will not be altered.
	 *
	 * @return A constraint adapted to the given bean type.
	 */
	protected Set<MetaConstraint<?>> adaptOriginsAndImplicitGroups(Set<MetaConstraint<?>> constraints) {
		Set<MetaConstraint<?>> adaptedConstraints = newHashSet();

		for ( MetaConstraint<?> oneConstraint : constraints ) {
			adaptedConstraints.add( adaptOriginAndImplicitGroup( oneConstraint ) );
		}
		return adaptedConstraints;
	}

	private <A extends Annotation> MetaConstraint<A> adaptOriginAndImplicitGroup(MetaConstraint<A> constraint) {
		ConstraintOrigin definedIn = definedIn( beanClass, constraint.getLocation().getDeclaringClass() );

		if ( definedIn == ConstraintOrigin.DEFINED_LOCALLY ) {
			return constraint;
		}

		Class<?> constraintClass = constraint.getLocation().getDeclaringClass();

		ConstraintDescriptorImpl<A> descriptor = new ConstraintDescriptorImpl<>(
				constraintCreationContext.getConstraintHelper(),
				constraint.getLocation().getConstrainable(),
				constraint.getDescriptor().getAnnotationDescriptor(),
				constraint.getConstraintLocationKind(),
				constraintClass.isInterface() ? constraintClass : null,
				definedIn,
				constraint.getDescriptor().getConstraintType()
		);

		return MetaConstraints.create( constraintCreationContext.getTypeResolutionHelper(),
				constraintCreationContext.getValueExtractorManager(),
				constraintCreationContext.getConstraintValidatorManager(), descriptor, constraint.getLocation() );
	}

	/**
	 * Allows specific sub-classes to customize the retrieved constraints.
	 */
	protected Set<MetaConstraint<?>> adaptConstraints(ConstrainedElement constrainedElement, Set<MetaConstraint<?>> constraints) {
		return constraints;
	}

	/**
	 * @param rootClass The root class. That is the class for which we currently
	 * create a {@code BeanMetaData}
	 * @param hierarchyClass The class on which the current constraint is defined on
	 *
	 * @return Returns {@code ConstraintOrigin.DEFINED_LOCALLY} if the
	 *         constraint was defined on the root bean,
	 *         {@code ConstraintOrigin.DEFINED_IN_HIERARCHY} otherwise.
	 */
	private ConstraintOrigin definedIn(Class<?> rootClass, Class<?> hierarchyClass) {
		if ( hierarchyClass.equals( rootClass ) ) {
			return ConstraintOrigin.DEFINED_LOCALLY;
		}
		else {
			return ConstraintOrigin.DEFINED_IN_HIERARCHY;
		}
	}

}
