package io.onedev.server.web.component.branchchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;

@SuppressWarnings("serial")
public class BranchChoiceProvider extends ChoiceProvider<String> {

	private IModel<Project> projectModel;

	public BranchChoiceProvider(IModel<Project> projectModel) {
		this.projectModel = projectModel;
	}

	@Override
	public void query(String term, int page, Response<String> response) {
		term = term.toLowerCase();
		List<String> branches = new ArrayList<>();
		Project project = projectModel.getObject();
		for (RefInfo ref: project.getBranches()) {
			String branch = GitUtils.ref2branch(ref.getRef().getName());
			if (branch.toLowerCase().startsWith(term))
				branches.add(branch);
		}
		
		Collections.sort(branches);

		int first = page * WebConstants.PAGE_SIZE;
		int last = first + WebConstants.PAGE_SIZE;
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
		projectModel.detach();
		
		super.detach();
	}
}