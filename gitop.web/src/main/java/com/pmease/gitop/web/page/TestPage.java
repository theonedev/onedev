package com.pmease.gitop.web.page;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;

import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Vertical;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitop.model.helper.BranchMatcher;


@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Form<Void>("form").add(BeanContext.editBean("editor", new Bean())));
	}
	
	@Editable
	public static class Bean implements Serializable {
		
		private List<BranchMatcher> matchers;

		@Editable
		@Valid
		@Vertical
		public List<BranchMatcher> getMatchers() {
			return matchers;
		}

		public void setMatchers(List<BranchMatcher> matchers) {
			this.matchers = matchers;
		}
		
	};
}
