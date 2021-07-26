package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.CommentAware;
import io.onedev.server.web.component.propertychangepanel.PropertyChangePanel;

public class PullRequestTitleChangeData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldTitle;
	
	private final String newTitle;
	
	public PullRequestTitleChangeData(String oldTitle, String newTitle) {
		this.oldTitle = oldTitle;
		this.newTitle = newTitle;
	}
	
	@Override
	public String getActivity() {
		return "changed title";
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return new PropertyChangePanel(componentId, 
				CollectionUtils.newHashMap("Title", oldTitle), 
				CollectionUtils.newHashMap("Title", newTitle), 
				true);
	}
	
	@Override
	public CommentAware getCommentAware() {
		return null;
	}

}