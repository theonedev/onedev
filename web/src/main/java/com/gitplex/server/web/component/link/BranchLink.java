package com.gitplex.server.web.component.link;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.DepotAndBranch;
import com.gitplex.server.web.page.depot.DepotPage;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;

@SuppressWarnings("serial")
public class BranchLink extends ViewStateAwarePageLink<Void> {

	private final DepotAndBranch depotAndBranch;
	
	public BranchLink(String id, DepotAndBranch depotAndBranch, @Nullable PullRequest request) {
		super(id, DepotBlobPage.class, paramsOf(depotAndBranch, request));
		this.depotAndBranch = depotAndBranch;
	}
	
	private static PageParameters paramsOf(DepotAndBranch depotAndBranch, PullRequest request) {
		BlobIdent blobIdent = new BlobIdent(depotAndBranch.getBranch(), null, FileMode.TREE.getBits());
		DepotBlobPage.State state = new DepotBlobPage.State(blobIdent);
		state.requestId = PullRequest.idOf(request);
		return DepotBlobPage.paramsOf(depotAndBranch.getDepot(), state);
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
