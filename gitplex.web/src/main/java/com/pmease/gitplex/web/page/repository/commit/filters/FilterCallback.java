package com.pmease.gitplex.web.page.repository.commit.filters;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface FilterCallback extends Serializable {
	void filter(AjaxRequestTarget target);
}
