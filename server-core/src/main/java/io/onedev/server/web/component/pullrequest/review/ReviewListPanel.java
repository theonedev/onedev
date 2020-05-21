package io.onedev.server.web.component.pullrequest.review;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.markdown.MarkdownManager;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.behavior.dropdown.DropdownHoverBehavior;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;

@SuppressWarnings("serial")
public class ReviewListPanel extends GenericPanel<PullRequest> {

	private static final MetaDataKey<ArrayList<User>> UNPREFERABLE_REVIEWERS = new MetaDataKey<ArrayList<User>>() {};
	
	private final IModel<List<PullRequestReview>> reviewsModel;
	
	public ReviewListPanel(String id, IModel<PullRequest> model) {
		super(id, model);
		
		reviewsModel = new LoadableDetachableModel<List<PullRequestReview>>() {

			@Override
			protected List<PullRequestReview> load() {
				PullRequest request = getPullRequest();
				List<PullRequestReview> reviews = new ArrayList<>(request.getReviews());
				
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
				item.add(new UserIdentPanel("user", review.getUser(), Mode.AVATAR_AND_NAME));
				
				PullRequest request = getPullRequest();
				
				WebMarkupContainer result = new WebMarkupContainer("result");
				if (review.getResult() != null) {
					if (review.getResult().isApproved())
						result.add(AttributeAppender.append("class", "approved fa fa-check-circle"));
					else 
						result.add(AttributeAppender.append("class", "request-for-changes fa fa-hand-stop-o"));
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
							builder.append("<div class='comment'>").append(HtmlUtils.clean(rendered).body().html()).append("</div>");
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
							attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to "
									+ "remove reviewer '" + item.getModelObject().getUser().getDisplayName() + "'?"));
						}
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = getPullRequest();
						PullRequestReview review = item.getModelObject();
						
						ArrayList<User> unpreferableReviewers = getPage().getMetaData(UNPREFERABLE_REVIEWERS);
						if (unpreferableReviewers == null)
							unpreferableReviewers = new ArrayList<>();
						unpreferableReviewers.remove(review.getUser());
						unpreferableReviewers.add(review.getUser());
						getPage().setMetaData(UNPREFERABLE_REVIEWERS, unpreferableReviewers);

						PullRequestReviewManager manager = OneDev.getInstance(PullRequestReviewManager.class);
						if (!manager.removeReviewer(review, unpreferableReviewers)) {
							getSession().warn("Reviewer '" + review.getUser().getDisplayName() 
									+ "' is required and can not be removed");
						} else if (request.isNew()) {
							target.add(ReviewListPanel.this);
						}
						reviewsModel.detach();
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
			protected void onSelect(AjaxRequestTarget target, User user) {
				super.onSelect(target, user);
				if (getPullRequest().isNew())
					target.add(ReviewListPanel.this);
			}
		                                                                                                                              
		});
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				if (isVisibleInHierarchy()) 
					handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(PullRequest.getWebSocketObservable(getPullRequest().getId()));
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
