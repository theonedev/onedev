package io.onedev.server.web.util;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.AbstractEntity;

public interface QueryPositionSupport<T extends AbstractEntity> extends Serializable {
	
	QueryPosition getPosition();
	
	void navTo(AjaxRequestTarget target, T entity, QueryPosition position);
	
}
