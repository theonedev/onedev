package com.pmease.commons.editable;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class BeanEditContext<T> extends AbstractEditContext<T> {

	public BeanEditContext(Serializable bean) {
		super(bean);
	}

}
