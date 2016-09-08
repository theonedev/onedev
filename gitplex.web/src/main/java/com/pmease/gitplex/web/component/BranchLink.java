package com.pmease.gitplex.web.component;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.support.DepotAndBranch;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;

@SuppressWarnings("serial")
public class BranchLink extends BookmarkablePageLink<Void> {

	private final DepotAndBranch depotAndBranch;
	
	public BranchLink(String id, DepotAndBranch depotAndBranch) {
		this(id, depotAndBranch, null);
	}
	
	public BranchLink(String id, DepotAndBranch depotAndBranch, @Nullable PullRequest request) {
		super(id, DepotFilePage.class, getPageParams(depotAndBranch, request));
		this.depotAndBranch = depotAndBranch;
	}
	
	private static PageParameters getPageParams(DepotAndBranch depotAndBranch, @Nullable PullRequest request) {
		DepotFilePage.State state = new DepotFilePage.State();
		state.blobIdent.revision = depotAndBranch.getBranch();
		state.requestId = PullRequest.idOf(request);
		return DepotFilePage.paramsOf(depotAndBranch.getDepot(), state);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(depotAndBranch.getObjectName(false) != null);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		configure();
		if (!isEnabled()) {
			tag.setName("span");
		}
	}

	@Override
	public IModel<?> getBody() {
		String label;
		if (getPage() instanceof DepotPage) {
			DepotPage page = (DepotPage) getPage();
			if (page.getDepot().equals(depotAndBranch.getDepot())) 
				label = depotAndBranch.getBranch();
			else 
				label = depotAndBranch.getFQN();
		} else {
			label = depotAndBranch.getFQN();
		}
		return Model.of(label);
	}

}
