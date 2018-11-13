package io.onedev.server.web.component.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestReviewManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.JsoupUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.behavior.dropdown.DropdownHoverBehavior;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.ident.UserIdentPanel.Mode;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;
import io.onedev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public class ReviewListPanel extends GenericPanel<PullRequest> {

	private final IModel<List<PullRequestReview>> reviewsModel;
	
	public ReviewListPanel(String id, IModel<PullRequest> model) {
		super(id, model);
		
		reviewsModel = new LoadableDetachableModel<List<PullRequestReview>>() {

			@Override
			protected List<PullRequestReview> load() {
				PullRequest request = getPullRequest();
				List<PullRequestReview> reviews = new ArrayList<>();
				for (PullRequestReview review: request.getReviews()) {
					if (review.getExcludeDate() == null)
						reviews.add(review);
				}
				
				Collections.sort(reviews, new Comparator<PullRequestReview>() {

					@Override
					public int compare(PullRequestReview o1, PullRequestReview o2) {
						if (o1.getId() != null && o2.getId() != null)
							return o1.getId().compareTo(o1.getId());
						else
							return 0;
					}
					
				});
				
				return reviews;
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
		reviewsModel.detach();
		super.onDetach();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		PullRequest request = getPullRequest();
		setVisible(!reviewsModel.getObject().isEmpty() || SecurityUtils.canModify(request) && !request.isMerged());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequestReview>("reviews", reviewsModel) {

			@Override
			protected void populateItem(ListItem<PullRequestReview> item) {
				PullRequestReview review = item.getModelObject();
				item.add(new UserIdentPanel("user", UserIdent.of(UserFacade.of(review.getUser())), Mode.AVATAR_AND_NAME));
				
				PullRequest request = getPullRequest();
				
				WebMarkupContainer result = new WebMarkupContainer("result");
				if (review.getResult() != null) {
					if (review.getResult().isApproved())
						result.add(AttributeAppender.append("class", "approved fa fa-check"));
					else 
						result.add(AttributeAppender.append("class", "request-for-changes fa fa-warning"));
				} else {
					result.add(AttributeAppender.append("class", "awaiting fa fa-clock-o"));
				}
				result.add(new DropdownHoverBehavior() {
					
					@Override
					protected Component newContent(String id) {
						ReviewResult result = item.getModelObject().getResult();
						
						String title;
						if (result != null) {
							if (result.isApproved())
								title = "Approved";
							else
								title = "Request for changes";
						} else {
							title = "Waiting for review";
						}
						StringBuilder builder = new StringBuilder();
						builder.append("<div class='title'>").append(title).append("</div>");
						
						if (result != null && result.getComment() != null) {
							MarkdownManager markdownManager = OneDev.getInstance(MarkdownManager.class);
							String rendered = markdownManager.render(result.getComment());
							builder.append("<div class='comment'>").append(JsoupUtils.clean(rendered).body().html()).append("</div>");
						}
						Label label = new Label(id, builder.toString());
						label.add(AttributeAppender.append("class", "review-action"));
						label.setEscapeModelStrings(false);
						return label;
					}
					
				});
				result.setVisible(!request.isNew());
				
				item.add(result);
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						if (!getPullRequest().isNew()) {
							attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to remove '" 
										+ item.getModelObject().getUser().getDisplayName() + "' from reviewer list?"));
						}
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = getPullRequest();
						PullRequestReview review = item.getModelObject();
						boolean removed;
						review.setExcludeDate(new Date());
						if (request.isNew()) {
							OneDev.getInstance(PullRequestManager.class).checkQuality(request);
							removed = review.getExcludeDate() != null;								
						} else {
							removed = OneDev.getInstance(PullRequestReviewManager.class).excludeReviewer(review);
						}
						if (!removed) {
							getSession().warn("Reviewer '" + review.getUser().getDisplayName() 
									+ "' is required and can not be removed");
						}
						reviewsModel.detach();
						
						send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(SecurityUtils.canModify(getPullRequest()) && !request.isMerged());
					}
					
				});
			}
			
		});
		
		add(new ReviewerChoice("addReviewer", getModel()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(!getPullRequest().isMerged() && SecurityUtils.canModify(getPullRequest()));
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
		response.render(CssHeaderItem.forReference(new ReviewListCssResourceReference()));
	}

}
