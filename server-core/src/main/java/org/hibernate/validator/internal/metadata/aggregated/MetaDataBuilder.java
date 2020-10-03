/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Builds {@link ConstraintMetaData} instances for the
 * {@link ConstrainedElement} objects representing one method or property in a
 * type's inheritance hierarchy.
 *
 * @author Gunnar Morling
 */
public abstract class MetaDataBuilder {

	private static final Log log = LoggerFactory.make();

	protected final ConstraintHelper constraintHelper;

	private final Class<?> beanClass;
	private final Set<MetaConstraint<?>> constraints = newHashSet();
	private final Map<Class<?>, Class<?>> groupConversions = newHashMap();
	private boolean isCascading = false;
	private UnwrapMode unwrapMode = UnwrapMode.AUTOMATIC;

	protected MetaDataBuilder(Class<?> beanClass, ConstraintHelper constraintHelper) {
		this.beanClass = beanClass;
		this.constraintHelper = constraintHelper;
	}

	/**
	 * Whether this builder allows to add the given element or not. This is the
	 * case if the specified element relates to the same property or method with
	 * which this builder was instantiated.
	 *
	 * @param constrainedElement The element to check.
	 *
	 * @return <code>true</code> if the given element can be added to this
	 *         builder, <code>false</code> otherwise.
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
		for (MetaConstraint<?> constraint: constrainedElement.getConstraints()) {
			if (!constraints.stream()
					.filter(it->it.getDescriptor().getAnnotationType() == constraint.getDescriptor().getAnnotationType())
					.findAny().isPresent()) {
				constraints.add(constraint);
			}
		}
//		constraints.addAll( constrainedElement.getConstraints() );
		
		isCascading = isCascading || constrainedElement.isCascading();
		unwrapMode = constrainedElement.unwrapMode();

		addGroupConversions( constrainedElement.getGroupConversions() );
	}

	/**
	 * Creates a new, read-only {@link ConstraintMetaData} object with all
	 * constraint information related to the method or property represented by
	 * this builder.
	 *
	 * @return A {@link ConstraintMetaData} object.
	 */
	public abstract ConstraintMetaData build();

	private void addGroupConversions(Map<Class<?>, Class<?>> groupConversions) {
		for ( Entry<Class<?>, Class<?>> oneConversion : groupConversions.entrySet() ) {
			if ( this.groupConversions.containsKey( oneConversion.getKey() ) ) {
				throw log.getMultipleGroupConversionsForSameSourceException(
						oneConversion.getKey(),
						CollectionHelper.<Class<?>>asSet(
								groupConversions.get( oneConversion.getKey() ),
								oneConversion.getValue()
						)
				);
			}
			else {
				this.groupConversions.put( oneConversion.getKey(), oneConversion.getValue() );
			}
		}
	}

	protected Map<Class<?>, Class<?>> getGroupConversions() {
		return groupConversions;
	}

	protected Set<MetaConstraint<?>> getConstraints() {
		return constraints;
	}

	protected boolean isCascading() {
		return isCascading;
	}

	protected Class<?> getBeanClass() {
		return beanClass;
	}

	public UnwrapMode unwrapMode() {
		return unwrapMode;
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

		ConstraintDescriptorImpl<A> descriptor = new ConstraintDescriptorImpl<A>(
				constraintHelper,
				constraint.getLocation().getMember(),
				constraint.getDescriptor().getAnnotation(),
				constraint.getElementType(),
				constraintClass.isInterface() ? constraintClass : null,
				definedIn,
				constraint.getDescriptor().getConstraintType()
		);

		return new MetaConstraint<A>(
				descriptor,
				constraint.getLocation()
		);
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
