package io.onedev.server.validation.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.collect.Lists;

import io.onedev.server.annotation.Interpolative;

public class InterpolativeValidator implements ConstraintValidator<Interpolative, Object> {
	
	private String message;
	
	@Override
	public void initialize(Interpolative constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		List<List<String>> values = new ArrayList<>();
		if (value instanceof Collection) {
			for (Object element: (Collection<Object>)value) {
				if (element instanceof String) {
					values.add(Lists.newArrayList((String)element));
				} else if (element instanceof Collection) {
					values.add((List<String>) element);
				}
			}
		} else {
			values.add(Lists.newArrayList((String) value));
		}
		
		for (List<String> each: values) {
			for (String each2: each) {
				try {
					io.onedev.server.util.interpolative.Interpolative.parse(each2);					
				} catch (Exception e) {
					constraintContext.disableDefaultConstraintViolation();
					String message = this.message;
					if (message.length() == 0) {
						message = "Last appearance of @ is a surprise to me. Either use @...@ to reference a variable, "
								+ "or use @@ for literal @";
					}
					constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
					return false;
				}
			}
		}
		return true;
	}
	
}