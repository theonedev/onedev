package com.pmease.gitplex.web.component.branch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.Constants;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class BranchChoiceProvider extends ChoiceProvider<RepoAndBranch> {

	private IModel<Repository> repoModel;

	public BranchChoiceProvider(IModel<Repository> repoModel) {
		this.repoModel = repoModel;
	}

	@Override
	public void query(String term, int page, Response<RepoAndBranch> response) {
		term = term.toLowerCase();
		List<RepoAndBranch> repoAndBranches = new ArrayList<>();
		Repository repository = repoModel.getObject();
		for (String branch: repository.getBranches()) {
			if (branch.toLowerCase().startsWith(term))
				repoAndBranches.add(new RepoAndBranch(repository, branch));
		}
		
		Collections.sort(repoAndBranches, new Comparator<RepoAndBranch>() {

			@Override
			public int compare(RepoAndBranch o1, RepoAndBranch o2) {
				return o1.getBranch().compareTo(o2.getBranch());
			}
			
		});

		int first = page * Constants.DEFAULT_SELECT2_PAGE_SIZE;
		int last = first + Constants.DEFAULT_SELECT2_PAGE_SIZE;
		response.setHasMore(last<repoAndBranches.size());
		if (last > repoAndBranches.size())
			last = repoAndBranches.size();
		response.addAll(repoAndBranches.subList(first, last));
	}

	@Override
	public void toJson(RepoAndBranch choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(StringEscapeUtils.escapeHtml4(choice.getBranch()));
	}

	@Override
	public Collection<RepoAndBranch> toChoices(Collection<String> ids) {
		List<RepoAndBranch> repoAndBranches = new ArrayList<>();
		for (String each : ids)
			repoAndBranches.add(new RepoAndBranch(each));

		return repoAndBranches;
	}

	@Override
	public void detach() {
		repoModel.detach();
		
		super.detach();
	}
}