package io.onedev.server.web.component.issuechoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.IModel;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class IssueChoiceProvider extends ChoiceProvider<Issue> {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Project> projectModel;
	
	public IssueChoiceProvider(IModel<Project> projectModel) {
		this.projectModel = projectModel;
	}
	
	@Override
	public void detach() {
		projectModel.detach();
		super.detach();
	}

	@Override
	public void toJson(Issue choice, JSONWriter writer) throws JSONException {
		writer
			.key("id").value(choice.getId())
			.key("number").value(choice.getNumber())
			.key("title").value(HtmlEscape.escapeHtml5(choice.getTitle()));
	}

	@Override
	public Collection<Issue> toChoices(Collection<String> ids) {
		List<Issue> issues = Lists.newArrayList();
		for (String id: ids)
			issues.add(OneDev.getInstance(IssueManager.class).load(Long.valueOf(id)));
		return issues;
	}

	@Override
	public void query(String term, int page, Response<Issue> response) {
		int count = (page+1) * WebConstants.PAGE_SIZE;
		
		Project project = projectModel.getObject();
		List<Issue> issues = new ArrayList<>();
		if (StringUtils.isNumeric(term)) {
			long number = Long.parseLong(term);
			Issue issue = OneDev.getInstance(IssueManager.class).find(project, number);
			if (issue != null)
				issues.add(issue);
			EntityCriteria<Issue> criteria = EntityCriteria.of(Issue.class);
			criteria.add(Restrictions.eq("project", project));
			criteria.add(Restrictions.and(
					Restrictions.or(Restrictions.ilike("title", "%" + term + "%"), Restrictions.ilike("numberStr", term + "%")), 
					Restrictions.ne("number", number)
				));
			criteria.addOrder(Order.desc("number"));
			issues.addAll(OneDev.getInstance(IssueManager.class).findRange(criteria, 0, count-issues.size()));
		} else {
			EntityCriteria<Issue> criteria = EntityCriteria.of(Issue.class);
			criteria.add(Restrictions.eq("project", project));
			if (StringUtils.isNotBlank(term)) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("title", "%" + term + "%"), 
						Restrictions.ilike("numberStr", term + "%")));
			}
			criteria.addOrder(Order.desc("number"));
			issues.addAll(OneDev.getInstance(IssueManager.class).findRange(criteria, 0, count));
		} 
		
		new ResponseFiller<>(response).fill(issues, page, WebConstants.PAGE_SIZE);
	}
	
}