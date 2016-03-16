package com.pmease.gitplex.web.component.branchchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Ref;
import org.json.JSONException;
import org.json.JSONWriter;

import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.Constants;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class BranchChoiceProvider extends ChoiceProvider<String> {

	private IModel<Depot> depotModel;

	public BranchChoiceProvider(IModel<Depot> depotModel) {
		this.depotModel = depotModel;
	}

	@Override
	public void query(String term, int page, Response<String> response) {
		term = term.toLowerCase();
		List<String> branches = new ArrayList<>();
		Depot depot = depotModel.getObject();
		for (Ref ref: depot.getBranchRefs()) {
			String branch = GitUtils.ref2branch(ref.getName());
			if (branch.toLowerCase().startsWith(term))
				branches.add(branch);
		}
		
		Collections.sort(branches);

		int first = page * Constants.DEFAULT_PAGE_SIZE;
		int last = first + Constants.DEFAULT_PAGE_SIZE;
		response.setHasMore(last<branches.size());
		if (last > branches.size())
			last = branches.size();
		response.addAll(branches.subList(first, last));
	}

	@Override
	public void toJson(String choice, JSONWriter writer) throws JSONException {
		String escaped = StringEscapeUtils.escapeHtml4(choice);
		writer.key("id").value(choice).key("name").value(escaped);
	}

	@Override
	public Collection<String> toChoices(Collection<String> ids) {
		List<String> branches = new ArrayList<>();
		for (String each : ids)
			branches.add(each);

		return branches;
	}

	@Override
	public void detach() {
		depotModel.detach();
		
		super.detach();
	}
}