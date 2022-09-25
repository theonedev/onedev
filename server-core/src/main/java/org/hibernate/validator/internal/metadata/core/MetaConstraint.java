/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintTree;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.validationcontext.ValidationContext;
import org.hibernate.validator.internal.engine.valuecontext.BeanValueContext;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Instances of this class abstract the constraint type  (class, method or field constraint) and give access to
 * meta data about the constraint. This allows a unified handling of constraints in the validator implementation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
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
	 * The location at which this constraint is defined.
	 */
	private final ConstraintLocation location;

	/**
	 * The path used to navigate from the outermost container to the innermost container and extract the value for
	 * validation.
	 */
	@Immutable
	private final ValueExtractionPathNode valueExtractionPath;

	private final int hashCode;

	/**
	 * Indicates if the constraint is defined for one group only: used to optimize already validated constraints
	 * tracking.
	 */
	private final boolean isDefinedForOneGroupOnly;

	/**
	 * @param constraintDescriptor The constraint descriptor for this constraint
	 * @param location meta data about constraint placement
	 * @param valueExtractionPath the potential {@link ValueExtractor}s used to extract the value to validate
	 * @param validatedValueType the type of the validated element
	 */
	MetaConstraint(ConstraintValidatorManager constraintValidatorManager, ConstraintDescriptorImpl<A> constraintDescriptor,
			ConstraintLocation location, List<ContainerClassTypeParameterAndExtractor> valueExtractionPath,
			Type validatedValueType) {
		this.constraintTree = ConstraintTree.of( constraintValidatorManager, constraintDescriptor, validatedValueType );
		this.location = location;
		this.valueExtractionPath = getValueExtractionPath( valueExtractionPath );
		this.hashCode = buildHashCode( constraintDescriptor, location );
		this.isDefinedForOneGroupOnly = constraintDescriptor.getGroups().size() <= 1;
	}

	private static ValueExtractionPathNode getValueExtractionPath(List<ContainerClassTypeParameterAndExtractor> valueExtractionPath) {
		switch ( valueExtractionPath.size() ) {
			case 0: return null;
			case 1: return new SingleValueExtractionPathNode( valueExtractionPath.iterator().next() );
			default: return new LinkedValueExtractionPathNode( null, valueExtractionPath );
		}
	}

	/**
	 * @return Returns the list of groups this constraint is part of. This might include the default group even when
	 *         it is not explicitly specified, but part of the redefined default group list of the hosting bean.
	 */
	public final Set<Class<?>> getGroupList() {
		return constraintTree.getDescriptor().getGroups();
	}

	public final boolean isDefinedForOneGroupOnly() {
		return isDefinedForOneGroupOnly;
	}

	public final ConstraintDescriptorImpl<A> getDescriptor() {
		return constraintTree.getDescriptor();
	}

	public final ConstraintLocationKind getConstraintLocationKind() {
		return constraintTree.getDescriptor().getConstraintLocationKind();
	}

	public boolean validateConstraint(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext) {
		push(this);
		try {
			boolean success = true;
			// constraint requiring value extraction to get the value to validate
			if ( valueExtractionPath != null ) {
				Object valueToValidate = valueContext.getCurrentValidatedValue();
				if ( valueToValidate != null ) {
					TypeParameterValueReceiver receiver = new TypeParameterValueReceiver( validationContext, valueContext, valueExtractionPath );
					ValueExtractorHelper.extractValues( valueExtractionPath.getValueExtractorDescriptor(), valueToValidate, receiver );
					success = receiver.isSuccess();
				}
			}
			// regular constraint
			else {
				success = doValidateConstraint( validationContext, valueContext );
			}
			return success;
		} finally {
			pop();
		}
	}

	private boolean doValidateConstraint(ValidationContext<?> executionContext, ValueContext<?, ?> valueContext) {
		valueContext.setConstraintLocationKind( getConstraintLocationKind() );
		boolean validationResult = constraintTree.validateConstraints( executionContext, valueContext );

		return validationResult;
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

		if ( !constraintTree.getDescriptor().equals( that.constraintTree.getDescriptor() ) ) {
			return false;
		}

		if ( !location.equals( that.location ) ) {
			return false;
		}

		return true;
	}

	private static int buildHashCode(ConstraintDescriptorImpl<?> constraintDescriptor, ConstraintLocation location) {
		final int prime = 31;
		int result = 1;
		result = prime * result + constraintDescriptor.hashCode();
		result = prime * result + location.hashCode();
		return result;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "MetaConstraint" );
		sb.append( "{constraintType=" ).append( StringHelper.toShortString( constraintTree.getDescriptor().getAnnotation().annotationType() ) );
		sb.append( ", location=" ).append( location );
		sb.append( ", valueExtractionPath=" ).append( valueExtractionPath );
		sb.append( "}" );
		return sb.toString();
	}

	private final class TypeParameterValueReceiver implements ValueExtractor.ValueReceiver {

		private final ValidationContext<?> validationContext;
		private final ValueContext<?, Object> valueContext;
		private boolean success = true;
		private ValueExtractionPathNode currentValueExtractionPathNode;

		public TypeParameterValueReceiver(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext, ValueExtractionPathNode currentValueExtractionPathNode) {
			this.validationContext = validationContext;
			this.valueContext = valueContext;
			this.currentValueExtractionPathNode = currentValueExtractionPathNode;
		}

		@Override
		public void value(String nodeName, Object object) {
			doValidate( object, nodeName );
		}

		@Override
		public void iterableValue(String nodeName, Object value) {
			valueContext.markCurrentPropertyAsIterable();
			doValidate( value, nodeName );
		}

		@Override
		public void indexedValue(String nodeName, int index, Object value) {
			valueContext.markCurrentPropertyAsIterableAndSetIndex( index );
			doValidate( value, nodeName );
		}

		@Override
		public void keyedValue(String nodeName, Object key, Object value) {
			valueContext.markCurrentPropertyAsIterableAndSetKey( key );
			doValidate( value, nodeName );
		}

		private void doValidate(Object value, String nodeName) {
			BeanValueContext.ValueState<Object> originalValueState = valueContext.getCurrentValueState();

			Class<?> containerClass = currentValueExtractionPathNode.getContainerClass();
			if ( containerClass != null ) {
				valueContext.setTypeParameter( containerClass, currentValueExtractionPathNode.getTypeParameterIndex() );
			}

			if ( nodeName != null ) {
				valueContext.appendTypeParameterNode( nodeName );
			}

			valueContext.setCurrentValidatedValue( value );

			if ( currentValueExtractionPathNode.hasNext() ) {
				if ( value != null ) {
					currentValueExtractionPathNode = currentValueExtractionPathNode.getNext();

					ValueExtractorDescriptor valueExtractorDescriptor = currentValueExtractionPathNode.getValueExtractorDescriptor();
					ValueExtractorHelper.extractValues( valueExtractorDescriptor, value, this );

					currentValueExtractionPathNode = currentValueExtractionPathNode.getPrevious();
				}
			}
			else {
				success &= doValidateConstraint( validationContext, valueContext );
			}

			// reset the value context to the state before this call
			valueContext.resetValueState( originalValueState );
		}

		public boolean isSuccess() {
			return success;
		}
	}

	static final class ContainerClassTypeParameterAndExtractor {

		private final Class<?> containerClass;
		private final TypeVariable<?> typeParameter;
		private final Integer typeParameterIndex;
		private final ValueExtractorDescriptor valueExtractorDescriptor;

		ContainerClassTypeParameterAndExtractor(Class<?> containerClass, TypeVariable<?> typeParameter, Integer typeParameterIndex, ValueExtractorDescriptor valueExtractorDescriptor) {
			this.containerClass = containerClass;
			this.typeParameter = typeParameter;
			this.typeParameterIndex = typeParameterIndex;
			this.valueExtractorDescriptor = valueExtractorDescriptor;
		}

		@Override
		public String toString() {
			return "ContainerClassTypeParameterAndExtractor [containerClass=" + containerClass +
					", typeParameter=" + typeParameter +
					", typeParameterIndex=" + typeParameterIndex +
					", valueExtractorDescriptor=" + valueExtractorDescriptor + "]";
		}
	}

	private interface ValueExtractionPathNode {
		boolean hasNext();
		ValueExtractionPathNode getPrevious();
		ValueExtractionPathNode getNext();
		Class<?> getContainerClass();
		TypeVariable<?> getTypeParameter();
		Integer getTypeParameterIndex();
		ValueExtractorDescriptor getValueExtractorDescriptor();
	}

	private static final class SingleValueExtractionPathNode implements ValueExtractionPathNode {

		private final Class<?> containerClass;
		private final TypeVariable<?> typeParameter;
		private final Integer typeParameterIndex;
		private final ValueExtractorDescriptor valueExtractorDescriptor;

		public SingleValueExtractionPathNode(ContainerClassTypeParameterAndExtractor typeParameterAndExtractor) {
			this.containerClass = typeParameterAndExtractor.containerClass;
			this.typeParameter = typeParameterAndExtractor.typeParameter;
			this.typeParameterIndex = typeParameterAndExtractor.typeParameterIndex;
			this.valueExtractorDescriptor = typeParameterAndExtractor.valueExtractorDescriptor;
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public ValueExtractionPathNode getPrevious() {
			throw new NoSuchElementException();
		}

		@Override
		public ValueExtractionPathNode getNext() {
			throw new NoSuchElementException();
		}

		@Override
		public Class<?> getContainerClass() {
			return containerClass;
		}

		@Override
		public TypeVariable<?> getTypeParameter() {
			return typeParameter;
		}

		@Override
		public Integer getTypeParameterIndex() {
			return typeParameterIndex;
		}

		@Override
		public ValueExtractorDescriptor getValueExtractorDescriptor() {
			return valueExtractorDescriptor;
		}

		@Override
		public String toString() {
			return "SingleValueExtractionPathNode [containerClass=" + containerClass +
					", typeParameter=" + typeParameter +
					", valueExtractorDescriptor=" + valueExtractorDescriptor + "]";
		}
	}

	private static final class LinkedValueExtractionPathNode implements ValueExtractionPathNode {

		private final ValueExtractionPathNode previous;
		private final ValueExtractionPathNode next;
		private final Class<?> containerClass;
		private final TypeVariable<?> typeParameter;
		private final Integer typeParameterIndex;
		private final ValueExtractorDescriptor valueExtractorDescriptor;

		private LinkedValueExtractionPathNode( ValueExtractionPathNode previous, List<ContainerClassTypeParameterAndExtractor> elements) {
			ContainerClassTypeParameterAndExtractor first = elements.get( 0 );
			this.containerClass = first.containerClass;
			this.typeParameter = first.typeParameter;
			this.typeParameterIndex = first.typeParameterIndex;
			this.valueExtractorDescriptor = first.valueExtractorDescriptor;
			this.previous = previous;

			if ( elements.size() == 1 ) {
				this.next = null;
			}
			else {
				this.next = new LinkedValueExtractionPathNode( this, elements.subList( 1, elements.size() ) );
			}
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public ValueExtractionPathNode getPrevious() {
			return previous;
		}

		@Override
		public ValueExtractionPathNode getNext() {
			return next;
		}

		@Override
		public Class<?> getContainerClass() {
			return containerClass;
		}

		@Override
		public TypeVariable<?> getTypeParameter() {
			return typeParameter;
		}

		@Override
		public Integer getTypeParameterIndex() {
			return typeParameterIndex;
		}

		@Override
		public ValueExtractorDescriptor getValueExtractorDescriptor() {
			return valueExtractorDescriptor;
		}

		@Override
		public String toString() {
			return "LinkedValueExtractionPathNode [containerClass=" + containerClass +
					", typeParameter=" + typeParameter +
					", valueExtractorDescriptor=" + valueExtractorDescriptor + "]";
		}
	}
}
