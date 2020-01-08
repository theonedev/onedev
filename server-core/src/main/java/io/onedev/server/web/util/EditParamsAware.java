package io.onedev.server.web.util;

import javax.annotation.Nullable;

import org.apache.wicket.Page;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public interface EditParamsAware {

	PageParameters getParamsBeforeEdit();
	
	PageParameters getParamsAfterEdit();
	
	@Nullable
	static String getUrlBeforeEdit(Page page) {
		if (page instanceof EditParamsAware) {
			EditParamsAware editParamsAware = (EditParamsAware) page; 
			return RequestCycle.get()
					.urlFor(page.getClass(), editParamsAware.getParamsBeforeEdit())
					.toString();
		} else {
			return null;
		}
	}
	
	@Nullable
	static String getUrlAfterEdit(Page page) {
		if (page instanceof EditParamsAware) {
			EditParamsAware editParamsAware = (EditParamsAware) page; 
			return RequestCycle.get()
					.urlFor(page.getClass(), editParamsAware.getParamsAfterEdit())
					.toString();
		} else {
			return null;
		}
	}
	
}
