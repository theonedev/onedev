package io.onedev.server.web.component.issue;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.util.ColorUtils;
import io.onedev.server.web.behavior.ChangeObserver;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Collection;

@SuppressWarnings("serial")
public class IssueStateBadge extends Label {
	
	private final Long issueId;
	
	private final IModel<Issue> issueModel = new LoadableDetachableModel<Issue>() {
		@Override
		protected Issue load() {
			return OneDev.getInstance(IssueManager.class).load(issueId);
		}
		
	};
	
	public IssueStateBadge(String id, Long issueId) {
		super(id);
		this.issueId = issueId;
		
		setDefaultModel(new LoadableDetachableModel<>() {
			@Override
			protected Object load() {
				return issueModel.getObject().getState();
			}
		});
	}

	@Override
	protected void onDetach() {
		issueModel.detach();
		super.onDetach();
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		Issue issue = issueModel.getObject();
		StateSpec stateSpec = OneDev.getInstance(SettingManager.class).getIssueSetting().getStateSpec(issue.getState());
		if (stateSpec != null) {
			String fontColor = ColorUtils.isLight(stateSpec.getColor())?"#333":"#f9f9f9";
			String style = String.format("background-color: %s; color: %s;", stateSpec.getColor(), fontColor);
			tag.put("style", style);
			tag.put("title", "State");
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ChangeObserver() {

			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(Issue.getDetailChangeObservable(issueModel.getObject().getId()));
			}

			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(component);
			}

		});
		
		add(AttributeAppender.append("class", "issue-state badge"));
		
		setOutputMarkupId(true);
	}

}
