package io.onedev.server.rest.jersey;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class ValidQueryParamsValidator implements ConstraintValidator<ValidQueryParams, Object[]> {

	@Context
	private ResourceInfo resourceInfo;
	
	@Context
	private UriInfo uriInfo;
	
	@Override
	public void initialize(ValidQueryParams constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object[] value, ConstraintValidatorContext context) {
		Set<String> expectedParams = new HashSet<>();
		for (Annotation[] annotations: resourceInfo.getResourceMethod().getParameterAnnotations()) {
			for (Annotation annotation: annotations) {
				if (annotation instanceof QueryParam) {
					QueryParam param = (QueryParam) annotation;
					expectedParams.add(param.value());
				}
			}
		}
		
		Set<String> actualParams = new HashSet<>(uriInfo.getQueryParameters().keySet());
		actualParams.removeAll(expectedParams);
		if (actualParams.isEmpty()) {
			return true;
		} else {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Unexpected query params: " + actualParams).addConstraintViolation();
			return false;
		}
	}

}
