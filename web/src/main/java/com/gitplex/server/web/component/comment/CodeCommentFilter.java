package com.gitplex.server.web.component.comment;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentRelation;
import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.FileChoice;
import com.gitplex.server.util.editable.annotation.UserChoice;
import com.gitplex.server.web.page.project.ProjectPage;

@Editable
public class CodeCommentFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_CONTENT = "content";
	
	private static final String PARAM_USER = "user";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_BEFORE = "before";
	
	private static final String PARAM_AFTER = "after";
	
	private String content;
	
	private String userName;
	
	private String path;
	
	private Date before;
	
	private Date after;
	
	@Editable(order=50, name="Content Containing")
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	@Editable(order=100, name="Created by", description="Choose the user who created the comment")
	@UserChoice
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=500, name="Commented path", description="Show comments on specified path")
	@FileChoice("getCommentedFiles")
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Editable(order=600, name="After date", description="Show comments after specified date")
	public Date getAfter() {
		return after;
	}

	public void setAfter(Date after) {
		this.after = after;
	}

	@Editable(order=700, name="Before date", description="Show comments before specified date")
	public Date getBefore() {
		return before;
	}

	public void setBefore(Date before) {
		this.before = before;
	}

	public CodeCommentFilter() {
	}
	
	public CodeCommentFilter(PageParameters params) {
		content = params.get(PARAM_CONTENT).toString();
		userName = params.get(PARAM_USER).toString();
		path = params.get(PARAM_PATH).toString();
		
		String value = params.get(PARAM_BEFORE).toString();
		if (value != null)
			before = new Date(Long.valueOf(value));
		
		value = params.get(PARAM_AFTER).toString();
		if (value != null)
			after = new Date(Long.valueOf(value));
	}

	public void fillCriteria(EntityCriteria<CodeComment> criteria) {
		if (content != null)
			criteria.add(Restrictions.ilike("content", "%" + content + "%"));
		if (userName != null)
			criteria.createCriteria("user").add(Restrictions.eq("name", userName));
		if (path != null) {
			String pathQuery = path.replace('*', '%');
			if (pathQuery.endsWith("/"))
				pathQuery += "%";
			criteria.add(Restrictions.ilike("markPos.path", pathQuery));
		}
			
		if (before != null)
			criteria.add(Restrictions.le("date", before));
		if (after != null)
			criteria.add(Restrictions.ge("date", after));
	}
	
	public void fillRelationCriteria(EntityCriteria<CodeCommentRelation> criteria) {
		Criteria commentCriteria = criteria.createCriteria("comment");
		if (content != null)
			commentCriteria.add(Restrictions.ilike("content", "%" + content + "%"));
		if (userName != null)
			commentCriteria.createCriteria("user").add(Restrictions.eq("name", userName));
		if (path != null) {
			String pathQuery = path.replace('*', '%');
			if (pathQuery.endsWith("/"))
				pathQuery += "%";
			commentCriteria.add(Restrictions.ilike("markPos.path", pathQuery));
		}
			
		if (before != null)
			commentCriteria.add(Restrictions.le("date", before));
		if (after != null)
			commentCriteria.add(Restrictions.ge("date", after));
	}
	
	public void fillPageParams(PageParameters params) {
		if (content != null)
			params.add(PARAM_CONTENT, content);
		if (userName != null)
			params.add(PARAM_USER, userName);
		if (path != null)
			params.add(PARAM_PATH, path);
		if (before != null)
			params.add(PARAM_BEFORE, before.getTime());
		if (after != null)
			params.add(PARAM_AFTER, after.getTime());
	}

	@SuppressWarnings("unused")
	private static List<String> getCommentedFiles() {
		IPageRequestHandler handler = (IPageRequestHandler) RequestCycle.get().getActiveRequestHandler();
		Project project = ((ProjectPage) handler.getPage()).getProject();
		return GitPlex.getInstance(CommitInfoManager.class).getFiles(project);
	}
	
}
