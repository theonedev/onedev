package com.gitplex.server.web.component.comment;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.model.CodeComment;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.util.editable.annotation.AccountChoice;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.FileChoice;
import com.gitplex.server.util.match.WildcardUtils;

@Editable
public class CodeCommentFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_USER = "user";
	
	private static final String PARAM_UNRESOLVED = "unresolved";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_BEFORE = "before";
	
	private static final String PARAM_AFTER = "after";
	
	private String userName;
	
	private boolean unresolved;
	
	private String path;
	
	private Date before;
	
	private Date after;
	
	@Editable(order=100, name="Created by", description="Choose the user who created the comment")
	@AccountChoice
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=200, name="Show unresolved only", description="Check this if you only want to show unresolved comments")
	public boolean isUnresolved() {
		return unresolved;
	}

	public void setUnresolved(boolean unresolved) {
		this.unresolved = unresolved;
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
		userName = params.get(PARAM_USER).toString();
		unresolved = "yes".equals(params.get(PARAM_UNRESOLVED).toString());
		path = params.get(PARAM_PATH).toString();
		
		String value = params.get(PARAM_BEFORE).toString();
		if (value != null)
			before = new Date(Long.valueOf(value));
		
		value = params.get(PARAM_AFTER).toString();
		if (value != null)
			after = new Date(Long.valueOf(value));
	}

	public void fill(EntityCriteria<CodeComment> criteria) {
		if (userName != null)
			criteria.createCriteria("user").add(Restrictions.eq("name", userName));
		if (unresolved)
			criteria.add(Restrictions.eq("resolved", false));
		if (path != null) {
			String pathQuery = path.replace('*', '%');
			if (pathQuery.endsWith("/"))
				pathQuery += "%";
			criteria.add(Restrictions.ilike("commentPos.path", pathQuery));
		}
			
		if (before != null)
			criteria.add(Restrictions.le("date", before));
		if (after != null)
			criteria.add(Restrictions.ge("date", after));
	}
	
	public void filter(Collection<CodeComment> comments) {
		for (Iterator<CodeComment> it = comments.iterator(); it.hasNext();) {
			CodeComment comment = it.next();
			if (userName != null) {
				if (comment.getUser() == null || !comment.getUser().getName().equals(userName)) {
					it.remove();
					continue;
				}
			}
			if (unresolved && comment.isResolved()) {
				it.remove();
				continue;
			}
			if (path != null) {
				if (comment.getCommentPos().getPath() == null) {
					it.remove();
					continue;
				} else {
					String matchWith = path;
					if (matchWith.endsWith("/"))
						matchWith += "*";
					if (!WildcardUtils.matchString(matchWith, comment.getCommentPos().getPath())) {
						it.remove();
						continue;
					}
				}
			}
			if (before != null && comment.getDate().getTime()>before.getTime()) {
				it.remove();
				continue;
			}
			if (after != null && comment.getDate().getTime()<after.getTime()) {
				it.remove();
				continue;
			}
		}
	}
	
	public void fillPageParams(PageParameters params) {
		if (userName != null)
			params.add(PARAM_USER, userName);
		if (unresolved)
			params.add(PARAM_UNRESOLVED, "yes");
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
		return ((CodeCommentAware)handler.getPage()).getCommentedFiles();
	}
	
}
