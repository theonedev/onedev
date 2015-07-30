package com.pmease.gitplex.web.component.diff;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.Change;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class RevisionDiffPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String oldRev;
	
	private final String newRev;
	
	private final InlineCommentSupport commentSupport;
	
	private final List<Change> changes;
	
	public RevisionDiffPanel(String id, IModel<Repository> repoModel, String oldRev, String newRev, 
			InlineCommentSupport commentSupport) {
		super(id);
		
		this.repoModel = repoModel;
		this.oldRev = oldRev;
		this.newRev = newRev;
		this.commentSupport = commentSupport;
		
		changes = new ArrayList<>();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
