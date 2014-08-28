package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.git.Commit;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentThread;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.page.repository.info.pullrequest.CommentThreadPanel;

@SuppressWarnings("serial")
public class OpenActivityPanel extends Panel {

	private IModel<PullRequest> requestModel;
	
	private String description;
	
	public OpenActivityPanel(String id, IModel<PullRequest> requestModel) {
		super(id);
		this.requestModel = requestModel;
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("description", "viewFrag", this);

		description = requestModel.getObject().getDescription();
		if (StringUtils.isNotBlank(description))
			fragment.add(new MultiLineLabel("content", description));
		else
			fragment.add(new Label("content", "<i>No description</i>").setEscapeModelStrings(false));
		
		fragment.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				description = requestModel.getObject().getDescription();
				
				Fragment fragment = new Fragment("description", "editFrag", OpenActivityPanel.this);
				
				final TextArea<String> descriptionArea = new TextArea<String>("content", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return description;
					}

					@Override
					public void setObject(String object) {
						description = object;
					}

				});
				
				descriptionArea.add(new AjaxFormComponentUpdatingBehavior("blur") {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						descriptionArea.processInput();
					}
					
				});
				
				fragment.add(descriptionArea);
				
				fragment.add(new AjaxLink<Void>("save") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = requestModel.getObject();
						request.setDescription(description);
						GitPlex.getInstance(Dao.class).persist(request);

						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(fragment);
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(fragment);
					}
					
				});
				
				OpenActivityPanel.this.replace(fragment);
				
				target.add(fragment);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitPlex.getInstance(AuthorizationManager.class)
						.canModify(requestModel.getObject()));
			}

		});
		
		fragment.setOutputMarkupId(true);
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(renderForView());

		PullRequest request = requestModel.getObject();
		Commit baseCommit = request.getCommit(request.getBaseCommit());
		
		add(new CommentThreadPanel("baseThreads", requestModel, baseCommit, new LoadableDetachableModel<List<CommentThread>>() {

			@Override
			protected List<CommentThread> load() {
				PullRequest request = requestModel.getObject();
				List<CommitComment> comments = request.getCommitComments().get(request.getBaseCommit());
				if (comments == null)
					return new ArrayList<>();
				else
					return CommentThread.asThreads(comments);
			}
			
		}, new LoadableDetachableModel<Map<CommentPosition, Date>>() {

			@Override
			protected Map<CommentPosition, Date> load() {
				PullRequest request = requestModel.getObject();
				Map<CommentPosition, Date> visits = request.getCommentVisits().get(request.getBaseCommit());
				if (visits == null)
					return new HashMap<>();
				else
					return visits;
			}
			
		}));
		
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}
	
}
