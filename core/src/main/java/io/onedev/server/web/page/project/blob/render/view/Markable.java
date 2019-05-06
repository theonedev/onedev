package io.onedev.server.web.page.project.blob.render.view;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.commons.utils.PlanarRange;

public interface Markable {

	void mark(AjaxRequestTarget target, @Nullable PlanarRange mark);
	
}
