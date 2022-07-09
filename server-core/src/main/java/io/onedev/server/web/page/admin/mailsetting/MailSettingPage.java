package io.onedev.server.web.page.admin.mailsetting;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.mail.MailCheckSetting;
import io.onedev.server.mail.MailManager;
import io.onedev.server.mail.MailPosition;
import io.onedev.server.mail.MessageListener;
import io.onedev.server.model.support.administration.mailsetting.MailSetting;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ParsedEmailAddress;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class MailSettingPage extends AdministrationPage {

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
		
		TaskButton testButton = new TaskButton("test") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				BeanEditor mailSettingEditor = editor.visitChildren(BeanEditor.class, new IVisitor<BeanEditor, BeanEditor>() {

					public void component(BeanEditor component, IVisit<BeanEditor> visit) {
						if (component.getModelObject() instanceof MailSetting)
							visit.stop(component); 
					}
					
				});
				setVisible(mailSettingEditor != null && mailSettingEditor.isVisibleInHierarchy());
			}

			@Override
			protected String runTask(TaskLogger logger) {
				return OneDev.getInstance(SessionManager.class).call(new Callable<String>() {

					@Override
					public String call() {
						MailManager mailManager = OneDev.getInstance(MailManager.class);
						MailSetting mailSetting = mailSettingHolder.getMailSetting();
						MailCheckSetting checkSetting = mailSetting.getCheckSetting();
						if (checkSetting != null) {
							String uuid = UUID.randomUUID().toString();
							AtomicReference<Future<?>> futureRef = new AtomicReference<>(null);
							MessageListener listener = new MessageListener() {
								
								@Override
								public void onReceived(Message message) throws MessagingException {
									if (message.getSubject().contains(uuid)) 
										futureRef.get().cancel(true);
								}
								
							};				

							checkSetting = new MailCheckSetting(checkSetting.getImapHost(), checkSetting.getImapPort(), 
									checkSetting.getImapUser(), checkSetting.getImapCredential(), checkSetting.getCheckAddress(), 
									checkSetting.isEnableSSL(), 5, checkSetting.getTimeout());
							futureRef.set(mailManager.monitorInbox(checkSetting, listener, new MailPosition()));
							
							ParsedEmailAddress checkAddress = ParsedEmailAddress.parse(checkSetting.getCheckAddress());
							String subAddressed = checkAddress.getSubAddressed(MailManager.TEST_SUB_ADDRESS);
							logger.log("Sending test mail to " + subAddressed + "...");
							mailManager.sendMail(mailSetting.getSendSetting(), 
									Sets.newHashSet(subAddressed), Lists.newArrayList(), Lists.newArrayList(), uuid, 
									"[Test] Test Email From OneDev", "This is a test email from OneDev", null, null);

							logger.log("Waiting for test mail to come back...");

							try {
								futureRef.get().get();
							} catch (CancellationException e) {
							} catch (InterruptedException e) {
								futureRef.get().cancel(true);
								throw new RuntimeException(e);
							} catch (ExecutionException e) {
								throw new RuntimeException(e);
							}
							
							logger.log("Received test mail");
							
							return "Great, your mail setting is working";
						} else {
							io.onedev.server.model.EmailAddress emailAddress = SecurityUtils.getUser().getPrimaryEmailAddress();
							if (emailAddress != null) {
								String body = "Great, your mail setting is working!";
								mailManager.sendMail(mailSettingHolder.getMailSetting().getSendSetting(), Sets.newHashSet(emailAddress.getValue()), 
										Lists.newArrayList(), Lists.newArrayList(), "[Test] Test Email From OneDev", 
										body, body, null, null);
								return "Test mail has been sent to " + emailAddress.getValue() + ", please check your mail box";
							} else {
								throw new ExplicitException("Primary email address of your account is not specified yet");
							}
						}	
					}
					
				});
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

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Mail Setting");
	}

}