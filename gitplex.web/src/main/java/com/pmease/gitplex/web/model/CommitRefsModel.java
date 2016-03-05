package com.pmease.gitplex.web.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import com.pmease.gitplex.core.entity.Depot;

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
		List<Ref> refs = depot.getBranchRefs();
		refs.addAll(depot.getTagRefs());
		for (Ref ref: refs) {
			RevCommit commit = depot.getRevCommit(ref.getObjectId());
			List<String> commitLabels = labels.get(commit.name());
			if (commitLabels == null) {
				commitLabels = new ArrayList<>();
				labels.put(commit.name(), commitLabels);
			}
			commitLabels.add(FileRepository.shortenRefName(ref.getName()));
		}
		return labels;
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}

}
