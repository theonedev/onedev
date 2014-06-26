package com.pmease.gitop.web.page;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.Form;
import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.editable.BeanContext;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private Bean bean = new Bean();
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.add(new FeedbackPanel("feedback", form));
		form.add(BeanContext.editBean("editor", bean));
		add(form);
		
		add(BeanContext.viewBean("viewer", bean));
	}
	
	@Editable
	public static class Bean implements Serializable {
		
		private String name;
		
		private String address;

		@Editable
		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@NotEmpty
		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
		
	}
}
