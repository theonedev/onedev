package com.pmease.gitop.web.page;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.validation.ClassValidating;
import com.pmease.commons.validation.Validatable;
import com.pmease.commons.wicket.editor.BeanEditContext;
import com.pmease.commons.wicket.editor.BeanEditor;

@SuppressWarnings("serial")
public class TestPage extends WebPage {

	private Bean bean = new Bean();
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				System.out.println(bean);
			}
			
		};
		add(form);

		BeanEditor<Serializable> editor = BeanEditContext.edit("editor", bean); 
		form.add(editor);
		form.add(new FencedFeedbackPanel("feedback", editor));
	}
	
	@Editable
	@ClassValidating
	public static class Bean implements Serializable, Validatable {
		
		private int age;
		
		private Long money;
		
		private Bean child;

		@Editable(order=100)
		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		@Editable(order=200)
		@NotNull
		public Long getMoney() {
			return money;
		}

		public void setMoney(Long money) {
			this.money = money;
		}

		@Editable(order=300)
		@Valid
		public Bean getChild() {
			return child;
		}

		public void setChild(Bean child) {
			this.child = child;
		}

		@Override
		public String toString() {
			return "age: " + age + ", " + "money: " + money + ", child: " + child;
		}

		@Override
		public boolean isValid(ConstraintValidatorContext context) {
			System.out.println(this);
			
			if (money == null)
				return true;
			
			if (age < 20 && money > 10000) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("child should not get more than 10000 money").addPropertyNode("age").addConstraintViolation();
				context.buildConstraintViolationWithTemplate("child should not get more than 10000 money").addPropertyNode("money").addConstraintViolation();
				context.buildConstraintViolationWithTemplate("you idiot").addConstraintViolation();
				return false;
			} else {
				return true;
			}
		}
	}
}
