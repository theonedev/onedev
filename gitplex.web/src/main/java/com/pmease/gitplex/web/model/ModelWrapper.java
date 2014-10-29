package com.pmease.gitplex.web.model;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

public interface ModelWrapper<T> extends Serializable {
	IModel<? extends T> asModel(T object);
}
