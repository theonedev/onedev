package com.pmease.gitop.web.page;

import java.io.Serializable;
import java.util.Random;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Password;
import com.pmease.commons.validation.ClassValidating;
import com.pmease.commons.validation.Validatable;
import com.pmease.commons.wicket.editor.BeanEditContext;
import com.pmease.commons.wicket.editor.BeanEditor;
import com.pmease.gitop.model.permission.operation.GeneralOperation;

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
		
		add(BeanEditContext.view("viewer", new LoadableDetachableModel<Serializable>() {

			@Override
			protected Serializable load() {
				Bean bean = new Bean();
				bean.setAge(new Random().nextInt());
				return bean;
			}
			
		}));
	}
	
	@Editable
	@ClassValidating
	public static class Bean implements Serializable, Validatable {
		
		private int age;
		
		private Long money;
		
		private Bean child;
		
		private String password;
		
		private String name;
		
		private Boolean married;
		
		private GeneralOperation operation;

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

		@Editable(order=201)
		@Password(confirmative=true)
		@NotEmpty
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		@Editable(order=202)
		@NotEmpty
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Editable(order=203)
		public Boolean isMarried() {
			return married;
		}

		public void setMarried(Boolean married) {
			this.married = married;
		}

		@Editable(order=204)
		@NotNull
		public GeneralOperation getOperation() {
			return operation;
		}

		public void setOperation(GeneralOperation operation) {
			this.operation = operation;
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
			return ReflectionToStringBuilder.toString(this);
		}

		@Override
		public boolean isValid(ConstraintValidatorContext context) {
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
