package com.pmease.gitop.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitop.model.MergeInfo;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.page.repository.source.commit.diff.DiffViewPanel;

@SuppressWarnings("serial")
public class RequestChangesPanel extends Panel {

	private Panel diffView;
	
	private DropDownChoice<String> baseCommitSelector;
	
	private DropDownChoice<String> headCommitSelector;
	
	// base commit represents comparison base
	private String baseCommitName;
	
	// head commit represents comparison head
	private String headCommitName;
	
	// map commit name to comit hash
	private IModel<LinkedHashMap<String, String>> commitsModel = 
			new LoadableDetachableModel<LinkedHashMap<String, String>>() {

		@Override
		protected LinkedHashMap<String, String> load() {
			LinkedHashMap<String, String> choices = new LinkedHashMap<>();
			
			PullRequest request = getPullRequest();
			MergeInfo mergeInfo = request.getMergeInfo();
			if (mergeInfo.getMergeHead() != null && !mergeInfo.getMergeHead().equals(mergeInfo.getRequestHead())) 
				choices.put("Auto-Merge Preview", request.getMergeInfo().getMergeHead());
			
			int index = 0;
			int count = request.getSortedUpdates().size();
			for (PullRequestUpdate update: request.getSortedUpdates()) {
				if (index == 0)
					choices.put("Head of Update #" + (count-index) + " (latest)", update.getHeadCommit());
				else 
					choices.put("Head of Update #" + (count-index), update.getHeadCommit());
				index++;
			}
			
			if (request.getTarget().getHeadCommit().equals(request.getBaseCommit())) {
				choices.put("Target Branch Head", request.getTarget().getHeadCommit());
			} else {
				choices.put("Target Branch Head (current)", request.getTarget().getHeadCommit());
				choices.put("Target Branch Head (original)", request.getBaseCommit());
			}
			
			return choices;
		}
		
	};

	public RequestChangesPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		baseCommitSelector = new DropDownChoice<String>("baseCommitChoice", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				if (!commitsModel.getObject().containsKey(baseCommitName)) {
					List<String> commitNames = new ArrayList<>(commitsModel.getObject().keySet());
					PullRequest request = getPullRequest();
					if (request.getTarget().getHeadCommit().equals(request.getBaseCommit())) {
						baseCommitName = commitNames.get(commitNames.size()-1);
					} else {
						baseCommitName = commitNames.get(commitNames.size()-2);
					}
				}
				return baseCommitName;
			}

			@Override
			public void setObject(String object) {
				baseCommitName = object;
			}
			
		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return new ArrayList<String>(commitsModel.getObject().keySet());
			}
			
		});
		baseCommitSelector.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(diffView);
			}
		});
		add(baseCommitSelector);
		
		headCommitSelector = new DropDownChoice<String>("headCommitChoice", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				if (!commitsModel.getObject().containsKey(headCommitName)) {
					List<String> commitNames = new ArrayList<>(commitsModel.getObject().keySet());
					PullRequest request = getPullRequest();
					MergeInfo mergeInfo = request.getMergeInfo();
					if (mergeInfo.getMergeHead() != null && !mergeInfo.getMergeHead().equals(mergeInfo.getRequestHead())) 
						headCommitName = commitNames.get(1);
					else
						headCommitName = commitNames.get(0);
				}
				return headCommitName;
			}
			
			@Override
			public void setObject(String object) {
				headCommitName = object;
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return new ArrayList<String>(commitsModel.getObject().keySet());
			}
			
		});
		headCommitSelector.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(diffView);
			}
			
		});
		add(headCommitSelector);
		
		add(diffView = new DiffViewPanel("diffView", new AbstractReadOnlyModel<Repository>() {

			@Override
			public Repository getObject() {
				return getPullRequest().getTarget().getRepository();
			}
			
		}, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return commitsModel.getObject().get(baseCommitSelector.getDefaultModelObjectAsString());
			}
			
		}, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return commitsModel.getObject().get(headCommitSelector.getDefaultModelObjectAsString());
			}
			
		}));
		
		diffView.setOutputMarkupId(true);
	}
	
	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}

	@Override
	protected void onDetach() {
		commitsModel.detach();
		
		super.onDetach();
	}
	
}
