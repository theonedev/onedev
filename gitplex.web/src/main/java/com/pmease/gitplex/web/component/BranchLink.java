package com.pmease.gitplex.web.component;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.entity.component.DepotAndBranch;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage.HistoryState;

@SuppressWarnings("serial")
public class BranchLink extends BookmarkablePageLink<Void> {

	private final DepotAndBranch depotAndBranch;
	
	public BranchLink(String id, DepotAndBranch depotAndBranch) {
		super(id, DepotFilePage.class, getPageParams(depotAndBranch));
		this.depotAndBranch = depotAndBranch;
	}
	
	private static PageParameters getPageParams(DepotAndBranch depotAndBranch) {
		HistoryState state = new HistoryState();
		state.blobIdent.revision = depotAndBranch.getBranch();
		return DepotFilePage.paramsOf(depotAndBranch.getDepot(), state);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(SecurityUtils.canRead(depotAndBranch.getDepot()) 
				&& depotAndBranch.getObjectName(false) != null);
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
