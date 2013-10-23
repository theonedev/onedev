package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.JavassistUtils;

@SuppressWarnings("serial")
public abstract class AbstractEditContext implements EditContext {

	protected List<String> validationErrors = new ArrayList<String>();
	
	private final Serializable bean;
	
	private final Class<?> beanClass;
	
	public AbstractEditContext(Serializable bean) {
		this.bean = bean;
		this.beanClass = JavassistUtils.unproxy(bean.getClass());
	}
	
	@Override
	public Serializable getBean() {
		return bean;
	}
	
	@Override
	public Class<?> getBeanClass() {
	    return beanClass;
	}
	
	@Override
	public List<String> getValidationErrors() {
		return validationErrors;
	}
	
	public boolean hasValidationError() {
		if (!validationErrors.isEmpty())
			return true;

		for (EditContext each: getChildContexts().values()) {
			if (each.hasValidationError())
				return true;
		}
		
		return false;
	}
	
	@Override
	public void validate() {
		clearValidationErrors();
		
		updateBean();
		
		for (ConstraintViolation<Serializable> violation: AppLoader.getInstance(Validator.class).validate(bean)) {
			List<Serializable> contextPath = new ArrayList<Serializable>();
			for (Path.Node node: violation.getPropertyPath()) {
				if (node.getIndex() != null)
					contextPath.add(node.getIndex());
				if (node.getName() != null)
					contextPath.add(node.getName());
			}
			findChildContext(contextPath, false).addValidationError(violation.getMessage());
		}
		
	}
	
	@Override
	public void clearValidationErrors() {
		validationErrors.clear();
		for (EditContext each: getChildContexts().values())
			each.clearValidationErrors();
	}
	
	@Override
	public void updateBean() {
		for (EditContext each: getChildContexts().values())
			each.updateBean();
	}
	
	@Override
	public void addValidationError(String errorMessage) {
		validationErrors.add(errorMessage);
	}

	@Override
	public EditContext findChildContext(List<Serializable> contextPath, boolean exactMatch) {
		if (contextPath.size() == 0)
			return this;
		
		contextPath = new ArrayList<>(contextPath);
		Serializable pathElement = contextPath.remove(0);
		EditContext childContext = getChildContexts().get(pathElement);
		if (childContext != null)
			return childContext.findChildContext(contextPath, exactMatch);
		else if (!exactMatch)
			return this;
		else
			return null;
	}

	@Override
	public Map<Serializable, EditContext> getChildContexts() {
		return new HashMap<>();
	}
}
