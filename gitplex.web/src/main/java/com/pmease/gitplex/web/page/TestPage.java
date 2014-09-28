package com.pmease.gitplex.web.page;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Markdown;
import com.pmease.commons.editable.annotation.Multiline;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.editable.BeanContext;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private Bean bean = new Bean();
	
	public TestPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
			}
			
		};
		form.add(new FeedbackPanel("feedback", form));
		form.add(BeanContext.editBean("editor", bean));
		add(form);
	}

	@Editable
	public static class Bean implements Serializable {
		private String name;
		
		private String description;
		
		private String comment;

		@Editable
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Editable
		@Multiline
		@NotEmpty
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		@Editable
		@Markdown
		@NotEmpty
		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
		
	}
}
