package io.onedev.server.web.component.pullrequest.review;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;

@SuppressWarnings("serial")
public abstract class ReviewListPanel extends Panel {

	private static final MetaDataKey<ArrayList<User>> UNPREFERABLE_REVIEWERS = new MetaDataKey<ArrayList<User>>() {};
	
	private final IModel<List<PullRequestReview>> reviewsModel;
	
	public ReviewListPanel(String id) {
		super(id);
		
		reviewsModel = new LoadableDetachableModel<List<PullRequestReview>>() {

			@Override
			protected List<PullRequestReview> load() {
				return getPullRequest().getSortedReviews();
			}
			
		};		
	}

	protected abstract PullRequest getPullRequest();
	
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
				
				item.add(new ReviewStatusIcon("status") {

					@Override
					protected ReviewResult getResult() {
						return item.getModelObject().getResult();
					}
					
				}.setVisible(!request.isNew()));
				
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
		
		add(new ReviewerChoice("addReviewer") {

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

			@Override
			protected PullRequest getPullRequest() {
				return ReviewListPanel.this.getPullRequest();
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
		response.render(CssHeaderItem.forReference(new ReviewCssResourceReference()));
	}

}
