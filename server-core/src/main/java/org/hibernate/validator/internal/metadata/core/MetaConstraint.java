/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Set;
import java.util.Stack;

import org.hibernate.validator.internal.engine.ValidationContext;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintTree;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

/**
 * Instances of this class abstract the constraint type  (class, method or field constraint) and give access to
 * meta data about the constraint. This allows a unified handling of constraints in the validator implementation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class MetaConstraint<A extends Annotation> {

	private static ThreadLocal<Stack<MetaConstraint<?>>> stack =  new ThreadLocal<Stack<MetaConstraint<?>>>() {

		@Override
		protected Stack<MetaConstraint<?>> initialValue() {
			return new Stack<MetaConstraint<?>>();
		}
	
	};
	
	public static void push(MetaConstraint<?> constraint) {
		stack.get().push(constraint);
	}

	public static void pop() {
		stack.get().pop();
	}

	public static MetaConstraint<?> get() {
		if (!stack.get().isEmpty()) 
			return stack.get().peek();
		else
			return null;
	}
	
	/**
	 * The constraint tree created from the constraint annotation.
	 */
	private final ConstraintTree<A> constraintTree;

	/**
	 * The constraint descriptor.
	 */
	private final ConstraintDescriptorImpl<A> constraintDescriptor;

	/**
	 * The location at which this constraint is defined.
	 */
	private final ConstraintLocation location;

	/**
	 * @param constraintDescriptor The constraint descriptor for this constraint
	 * @param location meta data about constraint placement
	 */
	public MetaConstraint(ConstraintDescriptorImpl<A> constraintDescriptor, ConstraintLocation location) {
		this.constraintTree = new ConstraintTree<A>( constraintDescriptor );
		this.constraintDescriptor = constraintDescriptor;
		this.location = location;
	}

	/**
	 * @return Returns the list of groups this constraint is part of. This might include the default group even when
	 *         it is not explicitly specified, but part of the redefined default group list of the hosting bean.
	 */
	public final Set<Class<?>> getGroupList() {
		return constraintDescriptor.getGroups();
	}

	public final ConstraintDescriptorImpl<A> getDescriptor() {
		return constraintDescriptor;
	}

	public final ElementType getElementType() {
		return constraintDescriptor.getElementType();
	}

	public boolean validateConstraint(ValidationContext<?> executionContext, ValueContext<?, ?> valueContext) {
		push(this);
		try {
			valueContext.setElementType( getElementType() );
			valueContext.setDeclaredTypeOfValidatedElement( location.getTypeForValidatorResolution() );
	
			boolean validationResult = constraintTree.validateConstraints( executionContext, valueContext );
			executionContext.markConstraintProcessed( valueContext.getCurrentBean(), valueContext.getPropertyPath(), this );
	
			return validationResult;
		} finally {
			pop();
		}
	}

	public ConstraintLocation getLocation() {
		return location;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		MetaConstraint<?> that = (MetaConstraint<?>) o;

		if ( constraintDescriptor != null ? !constraintDescriptor.equals( that.constraintDescriptor ) : that.constraintDescriptor != null ) {
			return false;
		}
		if ( location != null ? !location.equals( that.location ) : that.location != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = constraintDescriptor != null ? constraintDescriptor.hashCode() : 0;
		result = 31 * result + ( location != null ? location.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "MetaConstraint" );
		sb.append( "{constraintType=" ).append( constraintDescriptor.getAnnotation().annotationType().getName() );
		sb.append( ", location=" ).append( location );
		sb.append( "}" );
		return sb.toString();
	}
}
