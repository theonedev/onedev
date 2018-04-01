package io.onedev.server.web.component.requestreviewer;

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

import io.onedev.server.OneDev;
import io.onedev.server.manager.MarkdownManager;
import io.onedev.server.manager.ReviewInvitationManager;
import io.onedev.server.manager.ReviewManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Review;
import io.onedev.server.model.ReviewInvitation;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.JsoupUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.behavior.dropdown.DropdownHover;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;
import io.onedev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public class ReviewerListPanel extends GenericPanel<PullRequest> {

	private final IModel<List<User>> reviewersModel;
	
	public ReviewerListPanel(String id, IModel<PullRequest> model) {
		super(id, model);
		
		reviewersModel = new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				PullRequest request = getPullRequest();
				List<User> reviewers = new ArrayList<>();
				
				List<Review> reviews = new ArrayList<>(request.getQualityCheckStatus().getEffectiveReviews().values());
				Collections.sort(reviews);
				
				for (Review review: reviews)
					reviewers.add(review.getUser());
				
				for (User awaitingReviewer: request.getQualityCheckStatus().getAwaitingReviewers()) {
					if (!request.isMerged())
						reviewers.add(awaitingReviewer);
				}
				
				return reviewers;
			}
			
		};		
	}

	private PullRequest getPullRequest() {
		return getModelObject();
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (isVisibleInHierarchy() && event.getPayload() instanceof PageDataChanged) {
			PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
			pageDataChanged.getHandler().add(this);
		}
	}
	
	@Override
	protected void onDetach() {
		reviewersModel.detach();
		super.onDetach();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		PullRequest request = getPullRequest();
		setVisible(!reviewersModel.getObject().isEmpty() || SecurityUtils.canModify(request) && !request.isMerged());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<User>("reviewers", reviewersModel) {

			@Override
			protected void populateItem(ListItem<User> item) {
				item.add(new AvatarLink("avatar", item.getModelObject()));
				item.add(new UserLink("name", item.getModelObject()));
				
				PullRequest request = getPullRequest();
				Review review = request.getQualityCheckStatus().getEffectiveReviews().get(item.getModelObject());
				
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
								MarkdownManager markdownManager = OneDev.getInstance(MarkdownManager.class);
								String rendered = markdownManager.render(reviewNote);
								Label label = new Label(id, JsoupUtils.clean(rendered).body().html());
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
							request.clearQualityStatus();
							invitation.setDate(new Date());
							boolean removed;
							if (request.isNew()) {
								invitation.setType(ReviewInvitation.Type.EXCLUDE);
								removed = !request.getQualityCheckStatus().getAwaitingReviewers().contains(reviewer);								
							} else {
								removed = OneDev.getInstance(ReviewInvitationManager.class).exclude(invitation);
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
						Review review = request.getQualityCheckStatus().getEffectiveReviews().get(reviewer);
						setVisible(review != null 
								&& (reviewer.equals(SecurityUtils.getUser()) 
										|| SecurityUtils.canManage(request.getTargetProject())));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = getPullRequest();
						User reviewer = item.getModelObject();
						request.clearQualityStatus();
						OneDev.getInstance(ReviewManager.class).delete(reviewer, request);
						reviewersModel.detach();
						send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
					}
						
				});
			}
			
		});
		
		add(new ReviewerChoice("addReviewer", getModel()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				PullRequest request = getPullRequest();
				setVisible(SecurityUtils.canModify(request) && !request.isMerged());
			}
		                                                                                                                              
			@Override
			protected void onSelect(AjaxRequestTarget target, UserFacade user) {
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
