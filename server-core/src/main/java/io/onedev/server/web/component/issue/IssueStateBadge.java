package io.onedev.server.web.component.issue;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.util.ColorUtils;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.svg.SpriteImage;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.unbescape.html.HtmlEscape;

import java.util.Collection;

public class IssueStateBadge extends Label {

	private final IModel<Issue> issueModel;
	
	public IssueStateBadge(String id, IModel<Issue> issueModel, boolean withArrow) {
		super(id);
		this.issueModel = issueModel;
		
		setDefaultModel(new LoadableDetachableModel<>() {
			@Override
			protected Object load() {
				var body = "<span>" + HtmlEscape.escapeHtml5(issueModel.getObject().getState()) + "</span>";
				if (withArrow)
					body += " <svg class='icon icon-sm'><use xlink:href='" + SpriteImage.getVersionedHref("arrow4") + "'/></svg>";
				return body;
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
		StateSpec stateSpec = OneDev.getInstance(SettingService.class).getIssueSetting().getStateSpec(issue.getState());
		if (stateSpec != null) {
			String fontColor = ColorUtils.isLight(stateSpec.getColor())?"#333":"#f9f9f9";
			String style = String.format("background-color: %s; color: %s;", stateSpec.getColor(), fontColor);
			tag.put("style", style);
			tag.put("data-tippy-content", "State");
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ChangeObserver() {

			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(Issue.getDetailChangeObservable(issueModel.getObject().getId()));
			}

		});
		add(AttributeAppender.append("class", "issue-state badge"));
		setEscapeModelStrings(false);
		setOutputMarkupId(true);
	}

}
