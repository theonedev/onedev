package io.onedev.server.web.page.project.blob.render.view;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.support.TextRange;

public interface Markable {
	void mark(AjaxRequestTarget target, @Nullable TextRange mark);
}
