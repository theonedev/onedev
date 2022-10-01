package io.onedev.server.web.page.admin.usermanagement;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserInvitationManager;
import io.onedev.server.model.UserInvitation;
import io.onedev.server.model.support.administration.mailsetting.MailSetting;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;

@SuppressWarnings("serial")
public class NewInvitationPage extends AdministrationPage {

	public NewInvitationPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		MailSetting mailSetting = OneDev.getInstance(SettingManager.class).getMailSetting();
		if (mailSetting != null) {
			Fragment fragment = new Fragment("content", "inviteFrag", this);
			NewInvitationBean bean = new NewInvitationBean();
			Form<?> form = new Form<Void>("form");
			form.add(BeanContext.edit("editor", bean));
			form.add(new FencedFeedbackPanel("feedback", form));
			form.add(new TaskButton("invite") {

				@Override
				protected String runTask(TaskLogger logger) throws InterruptedException {
					for (String emailAddress: bean.getListOfEmailAddresses()) {
						logger.log("Sending invitation to '" + emailAddress + "'...");
						UserInvitation invitation = new UserInvitation();
						invitation.setEmailAddress(emailAddress);
						UserInvitationManager userInvitationManager = OneDev.getInstance(UserInvitationManager.class);
						userInvitationManager.save(invitation);
						userInvitationManager.sendInvitationEmail(invitation);
						if (Thread.interrupted())
							throw new InterruptedException();
					}
					return "Invitations sent";
				}

				@Override
				protected void onCompleted(AjaxRequestTarget target, boolean successful) {
					if (successful)
						setResponsePage(InvitationListPage.class);
				}
				
			});
			fragment.add(form);
			add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "noMailSettingFrag", this);
			fragment.add(new BookmarkablePageLink<Void>("mailSetting", MailSettingPage.class));
			add(fragment);
		}
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Invite Users");
	}

}
