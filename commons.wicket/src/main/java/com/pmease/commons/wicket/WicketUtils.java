package com.pmease.commons.wicket;

import java.lang.reflect.Method;

import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.util.ExceptionUtils;
import com.pmease.commons.util.ReflectionUtils;

public class WicketUtils {
	
	private static Method formValidateMethod = ReflectionUtils.getMethod(Form.class, "validate");
	private static Method formModelUpdateMethod = ReflectionUtils.getMethod(Form.class, "updateFormComponentModels");
	
	static {
		formValidateMethod.setAccessible(true);
		formModelUpdateMethod.setAccessible(true);
	}
	
	public static void updateFormModels(Form<?> form) {
		if (form.isEnabledInHierarchy() && form.isVisibleInHierarchy()) {
			try {
				formValidateMethod.invoke(form);
				formModelUpdateMethod.invoke(form);
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
	}

}
