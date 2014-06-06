package com.pmease.gitop.web.page;

import java.io.Serializable;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.annotation.Editable;
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
		
		add(BeanEditContext.view("viewer", bean));
	}
	
	@Editable
	public static class Bean implements Serializable {
		private String name;
		
		private Pet pet;

		@Editable
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Editable
		@Valid
		public Pet getPet() {
			return pet;
		}

		public void setPet(Pet pet) {
			this.pet = pet;
		}

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this);
		}
		
	}
	
	@Editable
	public static interface Pet extends Serializable {
	}

	@Editable
	public static class Cat implements Pet {
		private String name;
		
		private String mouse;

		@Editable
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Editable
		public String getMouse() {
			return mouse;
		}

		public void setMouse(String mouse) {
			this.mouse = mouse;
		}
		
	}
	
	@Editable
	public static class Dog implements Pet {
		private String name;
		
		private int age;

		@Editable
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Editable
		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
		
	}
}
