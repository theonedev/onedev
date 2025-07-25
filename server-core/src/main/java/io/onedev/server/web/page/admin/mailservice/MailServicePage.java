package io.onedev.server.web.page.admin.mailservice;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import javax.mail.MessagingException;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisitor;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.SubscriptionRequired;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ParsedEmailAddress;
import io.onedev.server.web.component.DisableAwareButton;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.component.taskbutton.TaskResult.PlainMessage;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.WicketUtils;

public class MailServicePage extends AdministrationPage {
	
	private String oldAuditContent;

	public MailServicePage(PageParameters params) {
		super(params);
	}

	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		MailServiceBean bean = new MailServiceBean();
		bean.setMailService(getSettingManager().getMailService());
		oldAuditContent = VersionedXmlDoc.fromBean(bean.getMailService()).toXML();
		
		PropertyEditor<Serializable> editor = 
				PropertyContext.edit("editor", bean, "mailService");
		Button saveButton = new DisableAwareButton("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				var newAuditContent = VersionedXmlDoc.fromBean(bean.getMailService()).toXML();
				getSettingManager().saveMailService(bean.getMailService());
				getAuditManager().audit(null, "changed mail service settings", oldAuditContent, newAuditContent);
				oldAuditContent = newAuditContent;
				getSession().success(_T("Mail service settings saved"));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				BeanEditor beanEditor = editor.visitChildren(
						BeanEditor.class,
						(IVisitor<BeanEditor, BeanEditor>) (component, visit) -> visit.stop(component));
				setEnabled(beanEditor == null 
						|| beanEditor.getDescriptor().getBeanClass().getAnnotation(SubscriptionRequired.class) == null 
						|| WicketUtils.isSubscriptionActive());
			}
		};
		
		var sendingMailText = _T("Sending test mail to {0}...");
		var waitingMailText = _T("Waiting for test mail to come back...");
		var receivedMailText = _T("Received test mail");
		var mailSentText = _T("Test mail has been sent to {0}, please check your mail box");
		var primaryMailNotSpecifiedText = _T("Primary email address of your account is not specified yet");
		var mailServiceWorkingText = _T("Great, your mail service configuration is working");

		TaskButton testButton = new TaskButton("test") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (!isEnabled())
					tag.put("disabled", "disabled");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				BeanEditor beanEditor = editor.visitChildren(
						BeanEditor.class,
						(IVisitor<BeanEditor, BeanEditor>) (component, visit) -> visit.stop(component));
				setVisible(beanEditor != null && beanEditor.isVisibleInHierarchy());
				setEnabled(beanEditor == null
						|| beanEditor.getDescriptor().getBeanClass().getAnnotation(SubscriptionRequired.class) == null
						|| WicketUtils.isSubscriptionActive());
			}

			@Override
			protected TaskResult runTask(TaskLogger logger) {
				return OneDev.getInstance(SessionManager.class).call(() -> {
					var mailService = bean.getMailService();
					var inboxMonitor = mailService.getInboxMonitor(true);
					if (inboxMonitor != null) {
						String uuid = UUID.randomUUID().toString();
						var futureRef = new AtomicReference<Future<?>>(null);
						futureRef.set(inboxMonitor.monitor(message -> {
							try {
								if (message.getSubject() != null && message.getSubject().contains(uuid)) {
									while (futureRef.get() == null) {
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											throw new RuntimeException(e);
										}
									}
									futureRef.get().cancel(true);
								}
							} catch (MessagingException e) {
								throw new RuntimeException(e);
							}
						}, true));

						var systemAddress = ParsedEmailAddress.parse(mailService.getSystemAddress());
						String subAddressed = systemAddress.getSubaddress(MailManager.TEST_SUB_ADDRESS);
						logger.log(MessageFormat.format(sendingMailText, subAddressed));
						mailService.sendMail(Sets.newHashSet(subAddressed), Sets.newHashSet(), 
								Sets.newHashSet(), uuid, "[Test] Test Email From OneDev", 
								"This is a test email from OneDev", null, 
								null, null, true);

						logger.log(waitingMailText);

						try {
							futureRef.get().get();
						} catch (CancellationException ignored) {
						} catch (InterruptedException e) {
							futureRef.get().cancel(true);
							throw new RuntimeException(e);
						} catch (ExecutionException e) {
							throw new RuntimeException(e);
						}

						logger.log(receivedMailText);

						return new TaskResult(true, new PlainMessage(mailServiceWorkingText));
					} else {
						var emailAddress = SecurityUtils.getAuthUser().getPrimaryEmailAddress();
						if (emailAddress != null) {
							String body = "Great, your mail service configuration is working!";
							mailService.sendMail(Sets.newHashSet(emailAddress.getValue()),
									Sets.newHashSet(), Sets.newHashSet(), "[Test] Test Email From OneDev",
									body, body, null, null, null, true);
							return new TaskResult(true, new PlainMessage(MessageFormat.format(mailSentText, emailAddress.getValue())));
						} else {
							return new TaskResult(false, new PlainMessage(primaryMailNotSpecifiedText));
						}
					}
				});
			}
		};
		
		Form<?> form = new Form<Void>("mailService") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PropertyUpdating) {
					PropertyUpdating propertyChanged = (PropertyUpdating) event.getPayload();
					propertyChanged.getHandler().add(testButton);
					propertyChanged.getHandler().add(saveButton);
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
		return new Label(componentId, _T("Mail Service"));
	}

}