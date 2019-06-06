package io.onedev.server.web.page.admin.mailsetting;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.notification.MailManager;
import io.onedev.server.web.behavior.testform.TestFormBehavior;
import io.onedev.server.web.behavior.testform.TestResult;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class MailSettingPage extends AdministrationPage {

	private static final Logger logger = LoggerFactory.getLogger(MailSettingPage.class);
	
	public MailSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		MailSettingHolder mailSettingHolder = new MailSettingHolder();
		mailSettingHolder.setMailSetting(OneDev.getInstance(SettingManager.class).getMailSetting());
		
		BeanEditor editor = BeanContext.edit("editor", mailSettingHolder);
		
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				OneDev.getInstance(SettingManager.class).saveMailSetting(mailSettingHolder.getMailSetting());
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
						User currentUser = OneDev.getInstance(UserManager.class).getCurrent();
						try {
							OneDev.getInstance(MailManager.class).sendMail(mailSettingHolder.getMailSetting(), 
									Sets.newHashSet(currentUser.getEmail()), 
									"Test email from OneDev", "Great, your mail setting is working!");
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
				
				BeanEditor mailSettingEditor = editor.visitChildren(BeanEditor.class, new IVisitor<BeanEditor, BeanEditor>() {

					public void component(BeanEditor component, IVisit<BeanEditor> visit) {
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

				if (event.getPayload() instanceof BeanUpdating) {
					BeanUpdating beanUpdating = (BeanUpdating) event.getPayload();
					beanUpdating.getHandler().add(testButton);
				}
				
			}

		};
		
		form.add(editor);
		form.add(saveButton);
		form.add(testButton);
		
		add(form);
	}

}