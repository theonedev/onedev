package com.pmease.gitop.web.page;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.wicket.markup.html.form.Form;
import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitop.model.helper.BranchMatcher;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private Bean bean = new Bean();
	
	public TestPage() {
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				System.out.println("submitted");
			}
			
		};
		form.add(new FeedbackPanel("feedback", form));
		form.add(BeanContext.editBean("editor", bean));
		add(form);
		
		add(BeanContext.viewBean("viewer", bean));
	}

	@Editable
	@Horizontal
	public static class Bean implements Serializable {
		private String name;
		
		private boolean married;
		
		private Bean child;
		
		private List<BranchMatcher> matchers;
		
		private List<Pet> pets;
		
		private BranchMatcher matcher;

		@Editable(order=200, description="will you do")
		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Editable(order=300)
		public boolean isMarried() {
			return married;
		}

		public void setMarried(boolean married) {
			this.married = married;
		}

		@Editable(description="raise some pets")
		@Valid
		public List<Pet> getPets() {
			return pets;
		}

		public void setPets(List<Pet> pets) {
			this.pets = pets;
		}

		@Editable(order=100)
		@Valid
		public Bean getChild() {
			return child;
		}

		public void setChild(Bean child) {
			this.child = child;
		}

		@Editable
		@Valid
		@Horizontal
		public List<BranchMatcher> getMatchers() {
			return matchers;
		}

		public void setMatchers(List<BranchMatcher> matchers) {
			this.matchers = matchers;
		}

		@Editable(description="hello world")
		@Valid
		@NotNull
		@Horizontal
		public BranchMatcher getMatcher() {
			return matcher;
		}

		public void setMatcher(BranchMatcher matcher) {
			this.matcher = matcher;
		}
		
	}

	@Editable
	public static class Pet implements Serializable {
		
		private String name;
		
		private Boolean cute;

		@Editable
		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Editable
		public Boolean getCute() {
			return cute;
		}

		public void setCute(Boolean cute) {
			this.cute = cute;
		}
		
	}
}
