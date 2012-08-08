package com.pmease.commons.product.web;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.PropertyModel;

import com.pmease.commons.wicket.modal.ModalBehavior;
import com.pmease.commons.wicket.modal.ModalPanel;
import com.pmease.commons.wicket.page.CommonPage;

@SuppressWarnings("serial")
public class HomePage extends CommonPage  {
	
	private String name;
	
	private String email;
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		add(newSection());
	}
	
	private WebMarkupContainer newSection() {
		WebMarkupContainer section = new WebMarkupContainer("section");
		section.setOutputMarkupId(true);
		
		section.add(new WebMarkupContainer("test").add(new ModalBehavior() {

			@Override
			protected ModalPanel newModal(String id) {
				return new ModalPanel(id, "Hello world") {

					@Override
					protected Component newContent(String id) {
						Fragment content = new Fragment(id, "dialogContent", HomePage.this);
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
						form.add(new AjaxButton("save") {

							@Override
							protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
								super.onAfterSubmit(target, form);
								close(target);
								WebMarkupContainer section = newSection();
								HomePage.this.replace(section);
								target.add(section);
							}
							
						});
						form.add(new AjaxLink<Void>("cancel") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								close(target);
							}
							
						});
						form.add(new WebMarkupContainer("test").add(new ModalBehavior() {

							@Override
							protected ModalPanel newModal(String id) {
								return new ModalPanel(id, "Really?") {

									@Override
									protected Component newContent(String id) {
										return new Fragment(id, "testContent", HomePage.this);
									}
									
								};
							}
							
						}));
						return content;
					}
					
				}.width("50%");
			}
		}));
		
		return section;
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