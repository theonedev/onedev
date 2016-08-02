package com.pmease.gitplex.web.page.depot.file;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.search.IndexListener;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage.CommitIndexed;

public class IndexChangeBroadcaster implements IndexListener {

	@Override
	public void commitIndexed(Depot depot, ObjectId commit) {
		CommitIndexed trait = new CommitIndexed();
		trait.depotId = depot.getId();
		trait.commitId = commit;
		WebSocketRenderBehavior.requestToRender(trait);
	}

}