package com.pmease.gitop.web.component.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.User;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class ProjectChoiceProvider extends ChoiceProvider<Project> {

	private IModel<User> userModel;
	
	public ProjectChoiceProvider(IModel<User> userModel) {
		this.userModel = userModel;
	}
	
	@Override
	public void query(String term, int page, Response<Project> response) {
		ProjectManager pm = Gitop.getInstance(ProjectManager.class);
		int first = page * 25;
		List<Project> projects = 
				pm.query(
						new Criterion[] {
								Restrictions.eq("owner", getUser()),
								Restrictions.like("name", term, MatchMode.START).ignoreCase()
						}, new Order[] {
								Order.asc("name")
						}, first, 25);
		
		response.addAll(projects);
	}

	@Override
	public void toJson(Project choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId())
			  .key("owner").value(choice.getOwner().getName())
			  .key("name").value(choice.getName());
	}

	@Override
	public Collection<Project> toChoices(Collection<String> ids) {
		List<Project> list = Lists.newArrayList();
		ProjectManager pm = Gitop.getInstance(ProjectManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			list.add(pm.load(id));
		}
		
		return list;
	}

	private User getUser() {
		return userModel.getObject();
	}
	
	@Override
	public void detach() {
		if (userModel != null) {
			userModel.detach();
		}
		
		super.detach();
	}
}
