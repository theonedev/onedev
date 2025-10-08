package io.onedev.server.web.page.project.blob.render.view;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface Positionable {

	void position(AjaxRequestTarget target, @Nullable String position);
	
}
