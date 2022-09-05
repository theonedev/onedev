package io.onedev.server.event.issue;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.persistence.dao.Dao;

public class IssueChanged extends IssueEvent {

	private final IssueChange change;
	
	private final String comment;
	
	public IssueChanged(IssueChange change, @Nullable String comment) {
		super(change.getUser(), change.getDate(), change.getIssue());
		this.change = change;
		this.comment = comment;
	}

	public IssueChange getChange() {
		return change;
	}

	@Override
	public String getMarkdown() {
		return comment;
	}

	@Nullable
	public String getComment() {
		return comment;
	}

	@Override
	public boolean affectsListing() {
		return change.affectsBoards();
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		return change.getData().getNewUsers();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return change.getData().getNewGroups();
	}

	@Override
	public String getActivity() {
		return getChange().getData().getActivity();
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return getChange().getData().getActivityDetail();
	}

	@Override
	public IssueEvent cloneIn(Dao dao) {
		return new IssueChanged(dao.load(IssueChange.class, change.getId()), comment);		
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(change);
	}
	
}
