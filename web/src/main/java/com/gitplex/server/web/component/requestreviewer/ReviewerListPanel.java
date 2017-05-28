package com.gitplex.server.web.component.requestreviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.MarkdownManager;
import com.gitplex.server.manager.ReviewManager;
import com.gitplex.server.manager.ReviewInvitationManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.Review;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.ReviewStatus;
import com.gitplex.server.web.behavior.dropdown.DropdownHover;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.link.UserLink;
import com.gitplex.server.web.util.ajaxlistener.ConfirmListener;
import com.gitplex.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public class ReviewerListPanel extends GenericPanel<PullRequest> {

	public ReviewerListPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	private PullRequest getPullRequest() {
		return getModelObject();
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PageDataChanged) {
			PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
			pageDataChanged.getHandler().add(this);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<List<User>> reviewersModel = new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				PullRequest request = getPullRequest();
				List<User> reviewers = new ArrayList<>();
				
				List<Review> reviews = new ArrayList<>(request.getReviewStatus().getEffectiveReviews().values());
				Collections.sort(reviews);
				
				for (Review review: reviews)
					reviewers.add(review.getUser());
				
				for (User awaitingReviewer: request.getReviewStatus().getAwaitingReviewers())
					reviewers.add(awaitingReviewer);
				
				return reviewers;
			}
			
		};
		add(new ListView<User>("reviewers", reviewersModel) {

			@Override
			protected void populateItem(ListItem<User> item) {
				item.add(new AvatarLink("avatar", item.getModelObject()));
				item.add(new UserLink("name", item.getModelObject()));
				
				PullRequest request = getPullRequest();
				Review review = request.getReviewStatus().getEffectiveReviews().get(item.getModelObject());
				
				WebMarkupContainer result = new WebMarkupContainer("result");
				if (review != null) {
					if (review.isApproved()) {
						result.add(AttributeAppender.append("class", "approved fa fa-thumbs-up"));
						if (review.getNote() == null)
							result.add(AttributeAppender.append("title", "Approved"));
					} else {
						result.add(AttributeAppender.append("class", "disapproved fa fa-thumbs-down"));
						if (review.getNote() == null)
							result.add(AttributeAppender.append("title", "Disapproved"));
					}
					String reviewNote = review.getNote();
					if (reviewNote != null) {
						result.add(new DropdownHover() {
							
							@Override
							protected Component newContent(String id) {
								MarkdownManager markdownManager = GitPlex.getInstance(MarkdownManager.class);
								Label label = new Label(id, markdownManager.render(reviewNote, null, true));
								label.add(AttributeAppender.append("class", "review-note"));
								label.setEscapeModelStrings(false);
								return label;
							}
							
						});
					}
				} else {
					result.add(AttributeAppender.append("class", "awaiting fa fa-clock-o"));
					result.add(AttributeAppender.append("title", "Waiting for review"));
				}
				result.setVisible(!request.isNew());
				
				item.add(result);
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						if (!getPullRequest().isNew()) {
							attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to remove '" 
										+ item.getModelObject().getDisplayName() + "' from reviewer list?"));
						}
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = getPullRequest();
						
						User reviewer = item.getModelObject();
						ReviewInvitation invitation = null;
						for (ReviewInvitation each: request.getReviewInvitations()) {
							if (each.getUser().equals(reviewer)) {
								invitation = each;
								break;
							}
						}
						
						if (invitation != null) {
							request.clearReviewStatus();
							invitation.setDate(new Date());
							boolean removed;
							if (request.isNew()) {
								invitation.setType(ReviewInvitation.Type.EXCLUDE);
								removed = !request.getReviewStatus().getAwaitingReviewers().contains(reviewer);								
							} else {
								removed = GitPlex.getInstance(ReviewInvitationManager.class).exclude(invitation);
							}
							if (!removed) {
								getSession().warn("Reviewer '" + reviewer.getDisplayName() 
										+ "' is required and can not be removed");
							}
							reviewersModel.detach();
							
							send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
						}
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						PullRequest request = getPullRequest();
						User currentUser = SecurityUtils.getUser();
						setVisible(currentUser != null && currentUser.equals(request.getSubmitter())
										|| SecurityUtils.canManage(request.getTargetProject()));
					}
					
				});
				item.add(new AjaxLink<Void>("withdraw") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to withdraw "
								+ "reviews of '" + item.getModelObject().getDisplayName() + "'?"));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						PullRequest request = getPullRequest();
						User reviewer = item.getModelObject();
						Review review = request.getReviewStatus().getEffectiveReviews().get(reviewer);
						setVisible(review != null 
								&& (reviewer.equals(SecurityUtils.getUser()) 
										|| SecurityUtils.canManage(request.getTargetProject())));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = getPullRequest();
						User reviewer = item.getModelObject();
						request.clearReviewStatus();
						GitPlex.getInstance(ReviewManager.class).delete(reviewer, request);
						reviewersModel.detach();
						send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
					}
						
				});
			}
			
		});
		
		add(new WebMarkupContainer("noReviewers") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				ReviewStatus reviewCheckStatus = getPullRequest().getReviewStatus();
				setVisible(reviewCheckStatus.getAwaitingReviewers().isEmpty() 
						&& reviewCheckStatus.getEffectiveReviews().isEmpty());
			}
			
		});
		
		add(new ReviewerChoice("addReviewer", getModel()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				PullRequest request = getPullRequest();
				setVisible(SecurityUtils.canModify(request));
			}
		                                                                                                                              
			@Override
			protected void onSelect(AjaxRequestTarget target, User user) {
				super.onSelect(target, user);
				send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ReviewerListCssResourceReference()));
	}

}
