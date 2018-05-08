package io.onedev.server.web.component.requestreviewer;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ReviewInvitationManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.ReviewInvitation;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.component.userchoice.UserChoiceResourceReference;

@SuppressWarnings("serial")
public class ReviewerChoice extends SelectToAddChoice<UserFacade> {

	private final IModel<PullRequest> requestModel;
	
	public ReviewerChoice(String id, IModel<PullRequest> requestModel) {
		super(id, new ReviewerProvider(requestModel));
		
		this.requestModel = requestModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder("Select user to add as reviewer...");
		getSettings().setFormatResult("onedev.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.userChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, UserFacade user) {
		PullRequest request = requestModel.getObject();
		ReviewInvitation invitation = null;
		for(ReviewInvitation each: request.getReviewInvitations()) {
			if (each.getUser().getFacade().equals(user)) {
				invitation = each;
				break;
			}
		}
		if (invitation == null) {
			invitation = new ReviewInvitation();
			invitation.setRequest(request);
			invitation.setUser(OneDev.getInstance(UserManager.class).load(user.getId()));
			request.getReviewInvitations().add(invitation);
		}
		invitation.setType(ReviewInvitation.Type.MANUAL);
		invitation.setDate(new Date());

		request.clearQualityStatus();
		
		if (!request.isNew())
			OneDev.getInstance(ReviewInvitationManager.class).invite(invitation);
	};
	
}
