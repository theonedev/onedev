/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidator;
import javax.validation.ValidationException;

import org.hibernate.validator.internal.engine.validationcontext.ValidationContext;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.GetterConstraintLocation;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.validation.validator.InterpolativeValidator;
import io.onedev.server.annotation.Interpolative;

/**
 * Due to constraint composition a single constraint annotation can lead to a whole constraint tree being validated.
 * This class encapsulates such a tree.
 *
 * @author Hardy Ferentschik
 * @author Federico Mancini
 * @author Dag Hovland
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public abstract class ConstraintTree<A extends Annotation> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The constraint descriptor for the constraint represented by this constraint tree.
	 */
	protected final ConstraintDescriptorImpl<A> descriptor;

	private final Type validatedValueType;

	private volatile ConstraintValidator<A, ?> defaultInitializedConstraintValidator;

	protected ConstraintTree(ConstraintValidatorManager constraintValidatorManager, ConstraintDescriptorImpl<A> descriptor, Type validatedValueType) {
		this.descriptor = descriptor;
		this.validatedValueType = validatedValueType;

		if ( constraintValidatorManager.isPredefinedScope() ) {
			this.defaultInitializedConstraintValidator = constraintValidatorManager.getInitializedValidator( validatedValueType,
					descriptor,
					constraintValidatorManager.getDefaultConstraintValidatorFactory(),
					constraintValidatorManager.getDefaultConstraintValidatorInitializationContext() );
		}
	}

	public static <U extends Annotation> ConstraintTree<U> of(ConstraintValidatorManager constraintValidatorManager,
			ConstraintDescriptorImpl<U> composingDescriptor, Type validatedValueType) {
		if ( composingDescriptor.getComposingConstraintImpls().isEmpty() ) {
			return new SimpleConstraintTree<>( constraintValidatorManager, composingDescriptor, validatedValueType );
		}
		else {
			return new ComposingConstraintTree<>( constraintValidatorManager, composingDescriptor, validatedValueType );
		}
	}

	public final boolean validateConstraints(ValidationContext<?> validationContext, ValueContext<?, ?> valueContext) {
		List<ConstraintValidatorContextImpl> violatedConstraintValidatorContexts = new ArrayList<>( 5 );
		validateConstraints( validationContext, valueContext, violatedConstraintValidatorContexts );
		if ( !violatedConstraintValidatorContexts.isEmpty() ) {
			for ( ConstraintValidatorContextImpl constraintValidatorContext : violatedConstraintValidatorContexts ) {
				for ( ConstraintViolationCreationContext constraintViolationCreationContext : constraintValidatorContext.getConstraintViolationCreationContexts() ) {
					validationContext.addConstraintFailure(
							valueContext, constraintViolationCreationContext, constraintValidatorContext.getConstraintDescriptor()
					);
				}
			}
			return false;
		}
		return true;
	}

	protected abstract void validateConstraints(ValidationContext<?> validationContext, ValueContext<?, ?> valueContext, Collection<ConstraintValidatorContextImpl> violatedConstraintValidatorContexts);

	public final ConstraintDescriptorImpl<A> getDescriptor() {
		return descriptor;
	}

	public final Type getValidatedValueType() {
		return this.validatedValueType;
	}

	private ValidationException getExceptionForNullValidator(Type validatedValueType, String path) {
		if ( descriptor.getConstraintType() == ConstraintDescriptorImpl.ConstraintType.CROSS_PARAMETER ) {
			return LOG.getValidatorForCrossParameterConstraintMustEitherValidateObjectOrObjectArrayException(
					descriptor.getAnnotationType()
			);
		}
		else {
			String className = validatedValueType.toString();
			if ( validatedValueType instanceof Class ) {
				Class<?> clazz = (Class<?>) validatedValueType;
				if ( clazz.isArray() ) {
					className = clazz.getComponentType().toString() + "[]";
				}
				else {
					className = clazz.getName();
				}
			}
			return LOG.getNoValidatorFoundForTypeException( descriptor.getAnnotationType(), className, path );
		}
	}

	protected final ConstraintValidator<A, ?> getInitializedConstraintValidator(ValidationContext<?> validationContext, ValueContext<?, ?> valueContext) {
		ConstraintValidator<A, ?> validator;

		if ( validationContext.getConstraintValidatorManager().isPredefinedScope() ) {
			validator = defaultInitializedConstraintValidator;
		}
		else {
			if ( validationContext.getConstraintValidatorFactory() == validationContext.getConstraintValidatorManager().getDefaultConstraintValidatorFactory()
					&& validationContext.getConstraintValidatorInitializationContext() == validationContext.getConstraintValidatorManager()
							.getDefaultConstraintValidatorInitializationContext() ) {
				validator = defaultInitializedConstraintValidator;

				if ( validator == null ) {
					synchronized ( this ) {
						validator = defaultInitializedConstraintValidator;
						if ( validator == null ) {
							validator = validationContext.getConstraintValidatorManager().getInitializedValidator(
									validatedValueType,
									descriptor,
									validationContext.getConstraintValidatorManager().getDefaultConstraintValidatorFactory(),
									validationContext.getConstraintValidatorManager().getDefaultConstraintValidatorInitializationContext() );

							defaultInitializedConstraintValidator = validator;
						}
					}
				}
			}
			else {
				// For now, we don't cache the result in the ConstraintTree if we don't use the default constraint validator
				// factory. Creating a lot of CHM for that cache might not be a good idea and we prefer being conservative
				// for now. Note that we have the ConstraintValidatorManager cache that mitigates the situation.
				// If you come up with a use case where it makes sense, please reach out to us.
				validator = validationContext.getConstraintValidatorManager().getInitializedValidator(
						validatedValueType,
						descriptor,
						validationContext.getConstraintValidatorFactory(),
						validationContext.getConstraintValidatorInitializationContext()
				);
			}
		}

		if ( validator == null ) {
			throw getExceptionForNullValidator( validatedValueType, valueContext.getPropertyPath().asString() );
		}

		return validator;
	}

	/**
	 * @return an {@link Optional#empty()} if there is no violation or a corresponding {@link ConstraintValidatorContextImpl}
	 * 		otherwise.
	 */
	@SuppressWarnings("unchecked")
	protected final <V> Optional<ConstraintValidatorContextImpl> validateSingleConstraint(
			ValueContext<?, ?> valueContext,
			ConstraintValidatorContextImpl constraintValidatorContext,
			ConstraintValidator<A, V> validator) {
		boolean isValid;
		try {
			V validatedValue = (V) valueContext.getCurrentValidatedValue();
			if (validatedValue != null && !(validator instanceof InterpolativeValidator)) {
				try {
					if (MetaConstraint.get().getLocation() instanceof GetterConstraintLocation) {
						GetterConstraintLocation location = (GetterConstraintLocation) MetaConstraint.get().getLocation();
						Method method = ReflectionUtils.findMethod(location.getDeclaringClass(), 
								location.getConstrainable().getName()); 
						Interpolative interpolative = method.getAnnotation(Interpolative.class);
						if (interpolative != null) {
							String exampleVar = interpolative.exampleVar();
							if (valueContext.getCurrentValidatedValue() instanceof Collection) {
								List<String> list = new ArrayList<>();
								for (String each: (Collection<String>)validatedValue) { 
									list.add(new VariableInterpolator(it->exampleVar).interpolate(each));				
								}
								validatedValue = (V) list;
							} else {
								validatedValue = (V) new VariableInterpolator(it->exampleVar)
										.interpolate((String) valueContext.getCurrentValidatedValue());
							}
						}
					}
					isValid = validator.isValid( validatedValue, constraintValidatorContext );
				} catch (ExplicitException e) {
					isValid = true;
				}
			} else {
				isValid = validator.isValid( validatedValue, constraintValidatorContext );
			}
		}
		catch (RuntimeException e) {
			if ( e instanceof ConstraintDeclarationException ) {
				throw e;
			}
			throw LOG.getExceptionDuringIsValidCallException( e );
		}
		if ( !isValid ) {
			//We do not add these violations yet, since we don't know how they are
			//going to influence the final boolean evaluation
			return Optional.of( constraintValidatorContext );
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintTree" );
		sb.append( "{ descriptor=" ).append( descriptor );
		sb.append( '}' );
		return sb.toString();
	}

}
