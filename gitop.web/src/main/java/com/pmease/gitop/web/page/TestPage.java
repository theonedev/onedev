package com.pmease.gitop.web.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.PropertyContext;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private Bean bean = new Bean();
	
	public TestPage() {
		Child child = new Child();
		child.setName("robin");
		bean.getChildren().add(child);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.add(new FencedFeedbackPanel("feedback", form) {

			@Override
			protected void onBeforeRender() {
				super.onBeforeRender();
			}
			
		});
		form.add(PropertyContext.editBean("editor", bean, "children"));
		form.add(new AjaxButton("save") {

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(form);
			}
			
		});
		add(form);
	}
	
	@Editable
	public static class Bean implements Serializable {
		private List<Child> children = new ArrayList<>();

		@Valid
		@Editable
		@NotNull
		public List<Child> getChildren() {
			return children;
		}

		public void setChildren(List<Child> children) {
			this.children = children;
		}
		
	};
	
	@Editable
	public static class Child implements Serializable {
		private String name;

		@Editable
		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
}
