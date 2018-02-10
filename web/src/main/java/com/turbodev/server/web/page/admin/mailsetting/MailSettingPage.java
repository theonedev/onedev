package com.turbodev.server.web.page.admin.mailsetting;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.manager.MailManager;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.User;
import com.turbodev.server.web.behavior.testform.TestFormBehavior;
import com.turbodev.server.web.behavior.testform.TestResult;
import com.turbodev.server.web.editable.BeanContext;
import com.turbodev.server.web.editable.BeanEditor;
import com.turbodev.server.web.editable.EditorChanged;
import com.turbodev.server.web.page.admin.AdministrationPage;
import com.turbodev.utils.ExceptionUtils;

@SuppressWarnings("serial")
public class MailSettingPage extends AdministrationPage {

	private static final Logger logger = LoggerFactory.getLogger(MailSettingPage.class);
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		MailSettingHolder mailSettingHolder = new MailSettingHolder();
		mailSettingHolder.setMailSetting(TurboDev.getInstance(ConfigManager.class).getMailSetting());
		
		BeanEditor<Serializable> editor = BeanContext.editBean("editor", mailSettingHolder);
		
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				TurboDev.getInstance(ConfigManager.class).saveMailSetting(mailSettingHolder.getMailSetting());
				getSession().success("Mail setting has been saved");
			}
			
		};
		AjaxButton testButton = new AjaxButton("test") {

			private TestFormBehavior testBehavior;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(testBehavior = new TestFormBehavior() {

					@Override
					protected TestResult test() {
						User currentUser = TurboDev.getInstance(UserManager.class).getCurrent();
						try {
							TurboDev.getInstance(MailManager.class).sendMail(mailSettingHolder.getMailSetting(), 
									Sets.newHashSet(currentUser.getEmail()), 
									"Test email from TurboDev", "Great, your mail setting is working!");
							return new TestResult.Successful("Test mail has been sent to " + 
									currentUser.getEmail() + ", please check your mail box.");
						} catch (Exception e) {
							logger.error("Error sending test email", e);
							String suggestedSolution = ExceptionUtils.suggestSolution(e);
							if (suggestedSolution != null)
								logger.warn("!!! " + suggestedSolution);
							return new TestResult.Failed("Error sending test email: " + e.getMessage() + ", check server log for details.");
						}
					}
					
				});
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				BeanEditor<Serializable> mailSettingEditor = editor.visitChildren(BeanEditor.class, new IVisitor<BeanEditor<Serializable>, BeanEditor<Serializable>>() {

					public void component(BeanEditor<Serializable> component, IVisit<BeanEditor<Serializable>> visit) {
						visit.stop(component);
					}
					
				});
				setVisible(mailSettingEditor != null && mailSettingEditor.isVisibleInHierarchy());
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				target.add(editor);
				target.focusComponent(null);
				testBehavior.requestTest(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(editor);
			}

		};
		
		Form<?> form = new Form<Void>("mailSetting") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof EditorChanged) {
					EditorChanged editorChanged = (EditorChanged) event.getPayload();
					editorChanged.getHandler().add(testButton);
				}
				
			}

		};
		
		form.add(editor);
		form.add(saveButton);
		form.add(testButton);
		
		add(form);
	}

}