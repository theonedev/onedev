package com.pmease.gitop.web.component.choice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class ComparableProjectChoiceProvider extends ChoiceProvider<Project> {

	private static final int PAGE_SIZE = 25;
	
	private IModel<Project> currentProjectModel;
	
	private IModel<List<Project>> comparableProjectsModel;

	public ComparableProjectChoiceProvider(IModel<Project> currentProjectModel) {
		this.currentProjectModel = currentProjectModel;
		
		comparableProjectsModel = new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				List<Project> comparableProjects = getCurrentProject().findComparables();
				comparableProjects.remove(getCurrentProject());
				if (getCurrentProject().getForkedFrom() != null)
					comparableProjects.remove(getCurrentProject().getForkedFrom());
				Collections.sort(comparableProjects, new Comparator<Project>() {

					@Override
					public int compare(Project project1, Project project2) {
						return project1.getUser().getName().compareTo(project2.getUser().getName());
					}
					
				});
				if (getCurrentProject().getForkedFrom() != null)
					comparableProjects.add(0, getCurrentProject().getForkedFrom());
				comparableProjects.add(0, getCurrentProject());
				return comparableProjects;
			}
			
		};
	}
	
	@Override
	public void query(String term, int page, Response<Project> response) {
		List<Project> projects = new ArrayList<>();
		for (Project project: getComparableProjects()) {
			if (project.getOwner().getName().contains(term))
				projects.add(project);
		}
		int first = page * PAGE_SIZE;
		if (first + PAGE_SIZE < projects.size()) {
			response.addAll(projects.subList(first, first + PAGE_SIZE));
			response.setHasMore(true);
		} else if (first + PAGE_SIZE == projects.size()) {
			response.addAll(projects.subList(first, first + PAGE_SIZE));
			response.setHasMore(false);
		} else {
			response.addAll(projects.subList(first, projects.size()));
			response.setHasMore(false);
		}
	}

	@Override
	public void toJson(Project choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		String value;
		if (getCurrentProject().equals(choice))
			value = "<i>Current</i>";
		else if (choice.equals(getCurrentProject().getForkedFrom()))
			value = "<i>UpStream</i>";
		else
			value = StringEscapeUtils.escapeHtml4(choice.getOwner().getName() + "/" + choice.getName());
		writer.value(value);
	}

	@Override
	public Collection<Project> toChoices(Collection<String> ids) {
		List<Project> projects = Lists.newArrayList();
		ProjectManager projectManager = Gitop.getInstance(ProjectManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			projects.add(projectManager.load(id));
		}

		return projects;
	}

	private Project getCurrentProject() {
		return currentProjectModel.getObject();
	}
	
	private List<Project> getComparableProjects() {
		return comparableProjectsModel.getObject();
	}

	@Override
	public void detach() {
		super.detach();
		currentProjectModel.detach();
		comparableProjectsModel.detach();
	}
	
}