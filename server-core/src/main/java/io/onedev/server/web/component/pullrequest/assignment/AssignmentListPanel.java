package io.onedev.server.web.component.pullrequest.assignment;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestAssignmentManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.base.BasePage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.*;

@SuppressWarnings("serial")
public abstract class AssignmentListPanel extends Panel {

	private final IModel<List<PullRequestAssignment>> assignmentsModel;
	
	public AssignmentListPanel(String id) {
		super(id);
		
		assignmentsModel = new LoadableDetachableModel<List<PullRequestAssignment>>() {

			@Override
			protected List<PullRequestAssignment> load() {
				PullRequest request = getPullRequest();
				List<PullRequestAssignment> assignments = new ArrayList<>(request.getAssignments());
				
				Collections.sort(assignments, (o1, o2) -> {
					if (o1.getId() != null && o2.getId() != null)
						return o1.getId().compareTo(o1.getId());
					else
						return 0;
				});
				
				return assignments;
			}
			
		};		
	}

	protected abstract PullRequest getPullRequest();
	
	@Override
	protected void onDetach() {
		assignmentsModel.detach();
		super.onDetach();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		PullRequest request = getPullRequest();
		setVisible(!assignmentsModel.getObject().isEmpty() || SecurityUtils.canModify(request) && !request.isMerged());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<>("assignments", assignmentsModel) {

			@Override
			protected void populateItem(ListItem<PullRequestAssignment> item) {
				PullRequestAssignment assignment = item.getModelObject();
				item.add(new UserIdentPanel("user", assignment.getUser(), Mode.AVATAR_AND_NAME));

				PullRequest request = getPullRequest();

				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						if (!getPullRequest().isNew()) {
							attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to "
									+ "remove assignee '" + item.getModelObject().getUser().getDisplayName() + "'?"));
						}
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = getPullRequest();
						PullRequestAssignment assignment = item.getModelObject();
						request.getAssignments().remove(assignment);
						if (request.isNew()) {
							target.add(AssignmentListPanel.this);
						} else {
							OneDev.getInstance(PullRequestAssignmentManager.class).delete(assignment);
							((BasePage) getPage()).notifyObservableChange(target,
									PullRequest.getChangeObservable(getPullRequest().getId()));
						}
						assignmentsModel.detach();
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						setVisible(SecurityUtils.canModify(getPullRequest()) && !request.isMerged());
					}

				});
			}

		});
		
		add(new AssigneeChoice("addAssignee") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(!getPullRequest().isMerged() && SecurityUtils.canModify(getPullRequest()));
			}

			@Override
			protected void onSelect(AjaxRequestTarget target, User user) {
				super.onSelect(target, user);
				if (getPullRequest().isNew())
					target.add(AssignmentListPanel.this);
			}

			@Override
			protected PullRequest getPullRequest() {
				return AssignmentListPanel.this.getPullRequest();
			}
		                                                                                                                              
		});
		
		add(new ChangeObserver() {
			
			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(PullRequest.getChangeObservable(getPullRequest().getId()));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AssignmentListCssResourceReference()));
	}

}
