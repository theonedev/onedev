package io.onedev.server.web.util.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;

public class CommitRefsModel extends LoadableDetachableModel<Map<String, List<String>>> {

	private static final long serialVersionUID = 1L;

	private final IModel<Project> projectModel;
	
	public CommitRefsModel(IModel<Project> projectModel) {
		this.projectModel = projectModel;
	}
	
	@Override
	protected Map<String, List<String>> load() {
		Project project = projectModel.getObject();
		Map<String, List<String>> labels = new HashMap<>();
		List<RefInfo> refInfos = project.getBranches();
		refInfos.addAll(project.getTags());
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
		projectModel.detach();
		super.onDetach();
	}

}
