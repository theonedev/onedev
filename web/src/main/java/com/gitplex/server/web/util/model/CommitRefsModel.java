package com.gitplex.server.web.util.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitplex.server.entity.Depot;
import com.gitplex.server.git.RefInfo;

public class CommitRefsModel extends LoadableDetachableModel<Map<String, List<String>>> {

	private static final long serialVersionUID = 1L;

	private final IModel<Depot> depotModel;
	
	public CommitRefsModel(IModel<Depot> depotModel) {
		this.depotModel = depotModel;
	}
	
	@Override
	protected Map<String, List<String>> load() {
		Depot depot = depotModel.getObject();
		Map<String, List<String>> labels = new HashMap<>();
		List<RefInfo> refInfos = depot.getBranches();
		refInfos.addAll(depot.getTags());
		for (RefInfo refInfo: refInfos) {
			if (refInfo.getPeeledObj() instanceof RevCommit) {
				RevCommit commit = (RevCommit) refInfo.getPeeledObj();
				List<String> commitLabels = labels.get(commit.name());
				if (commitLabels == null) {
					commitLabels = new ArrayList<>();
					labels.put(commit.name(), commitLabels);
				}
				commitLabels.add(Repository.shortenRefName(refInfo.getRef().getName()));
			}
		}
		return labels;
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}

}
