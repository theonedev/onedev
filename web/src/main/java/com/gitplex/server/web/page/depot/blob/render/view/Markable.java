package com.gitplex.server.web.page.depot.blob.render.view;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.model.support.TextRange;

public interface Markable {
	void mark(AjaxRequestTarget target, @Nullable TextRange mark);
}
