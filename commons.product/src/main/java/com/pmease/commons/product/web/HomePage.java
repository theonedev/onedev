package com.pmease.commons.product.web;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.PropertyModel;

import com.pmease.commons.wicket.behavior.confirm.ConfirmBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.modal.ModalBehavior;
import com.pmease.commons.wicket.behavior.modal.ModalPanel;
import com.pmease.commons.wicket.page.CommonPage;

@SuppressWarnings("serial")
public class HomePage extends CommonPage  {
	
	private String name;
	
	private String email;
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		ModalPanel modalPanel = new ModalPanel("modal") {

			@Override
			protected Component newContent(String id) {
				Fragment content = new Fragment(id, "modalFrag", HomePage.this);
				Form<?> form = new Form<Object>("form") {

					@Override
					protected void onSubmit() {
						super.onSubmit();
						System.out.println(name + ":" + email);
					}
					
				};
				content.add(form);
				form.add(new TextField<String>("name", new PropertyModel<String>(HomePage.this, "name")));
				form.add(new TextField<String>("email", new PropertyModel<String>(HomePage.this, "email")));
				form.add(new AjaxSubmitLink("save") {

					@Override
					protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onAfterSubmit(target, form);
						close(target);
					}

				}.add(new ConfirmBehavior("Do you really want to update this?")));
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						close(target);
					}
					
				});
				return content;
			}
			
		};
		add(modalPanel);
		
		add(new WebMarkupContainer("modalTrigger").add(new ModalBehavior(modalPanel)));
		
		DropdownPanel dropdownPanel = new DropdownPanel("dropdown") {

			@Override
			protected Component newContent(String id) {
				Fragment fragment = new Fragment(id, "dropdownFrag", HomePage.this);
				ModalPanel modalPanel = new ModalPanel("modal") {

					@Override
					protected Component newContent(String id) {
						Fragment content = new Fragment(id, "modalFrag", HomePage.this);
						Form<?> form = new Form<Object>("form") {

							@Override
							protected void onSubmit() {
								super.onSubmit();
								System.out.println(name + ":" + email);
							}
							
						};
						content.add(form);
						form.add(new TextField<String>("name", new PropertyModel<String>(HomePage.this, "name")));
						form.add(new TextField<String>("email", new PropertyModel<String>(HomePage.this, "email")));
						form.add(new AjaxSubmitLink("save") {

							@Override
							protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
								super.onAfterSubmit(target, form);
								close(target);
							}

						}.add(new ConfirmBehavior("Do you really want to update this?")));
						
						form.add(new AjaxLink<Void>("cancel") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								close(target);
							}
							
						});
						DropdownPanel dropdownPanel = new DropdownPanel("dropdown") {

							@Override
							protected Component newContent(String id) {
								return new Label(id, "Hello World");
							}
							
						};
						form.add(dropdownPanel);
						
						form.add(new WebMarkupContainer("dropdownTrigger").add(new DropdownBehavior(dropdownPanel).setHoverMode(false)));
						return content;
					}
					
				};
				fragment.add(modalPanel);
				
				fragment.add(new WebMarkupContainer("modalTrigger").add(new ModalBehavior(modalPanel)));
				return fragment;
			}
			
		};
		add(dropdownPanel);
		
		add(new WebMarkupContainer("dropdownTrigger").add(new DropdownBehavior(dropdownPanel).setHoverMode(true)));
		
		
		add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				System.out.println("This has been deleted.");
			}

		}.add(new ConfirmBehavior("Do you really want to delete this?")));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}