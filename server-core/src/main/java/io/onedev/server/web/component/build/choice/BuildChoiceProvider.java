package io.onedev.server.web.component.build.choice;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.service.BuildService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScopedQuery;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public abstract class BuildChoiceProvider extends ChoiceProvider<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public void toJson(Build choice, JSONWriter writer) throws JSONException {
		writer
			.key("id").value(choice.getId())
			.key("reference").value(choice.getReference().toString(getProject()))
			.key("jobName").value(HtmlEscape.escapeHtml5(choice.getJobName()));
		if (choice.getVersion() != null)
			writer.key("version").value(HtmlEscape.escapeHtml5(choice.getVersion()));
	}

	@Override
	public Collection<Build> toChoices(Collection<String> ids) {
		List<Build> builds = Lists.newArrayList();
		BuildService buildService = OneDev.getInstance(BuildService.class); 
		for (String id: ids) {
			Build build = buildService.load(Long.valueOf(id));
			Hibernate.initialize(build);
			builds.add(build);
		}
		return builds;
	}

	@Override
	public void query(String term, int page, Response<Build> response) {
		int count = (page+1) * WebConstants.PAGE_SIZE + 1;
		var scopedQuery = ProjectScopedQuery.of(getProject(), term, '#', '-');
		if (scopedQuery != null) {
			List<Build> builds = OneDev.getInstance(BuildService.class).query(SecurityUtils.getSubject(), scopedQuery.getProject(), scopedQuery.getQuery(), count);
			new ResponseFiller<>(response).fill(builds, page, WebConstants.PAGE_SIZE);
		} else {
			response.setHasMore(false);
		}
	}
	
	@Nullable
	protected abstract Project getProject();
	
}