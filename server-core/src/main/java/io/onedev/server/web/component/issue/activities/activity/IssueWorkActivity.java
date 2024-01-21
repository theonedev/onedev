package io.onedev.server.web.component.issue.activities.activity;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.User;
import io.onedev.server.web.util.DeleteCallback;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Date;

@SuppressWarnings("serial")
public class IssueWorkActivity implements IssueActivity {

	private final Long workId;
	
	public IssueWorkActivity(IssueWork work) {
		workId = work.getId();
	}
	
	@Override
	public Panel render(String panelId, DeleteCallback deleteCallback) {
		return new IssueWorkPanel(panelId, new LoadableDetachableModel<IssueWork>() {

			@Override
			protected IssueWork load() {
				return getWork();
			}
			
		}, target -> {
			OneDev.getInstance(IssueWorkManager.class).delete(getWork());
			deleteCallback.onDelete(target);
		});
	}
	
	public Long getWorkId() {
		return workId;
	}
	
	public IssueWork getWork() {
		return OneDev.getInstance(IssueWorkManager.class).load(workId);
	}

	@Override
	public Date getDate() {
		return getWork().getDate();
	}

	@Override
	public String getAnchor() {
		return null;
	}

	@Override
	public User getUser() {
		return getWork().getUser();
	}
	
}
