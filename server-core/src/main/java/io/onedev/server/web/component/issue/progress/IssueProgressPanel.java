package io.onedev.server.web.component.issue.progress;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.TimeTrackingSetting;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.LoadableDetachableModel;

import javax.annotation.Nullable;
import java.util.Collection;

public abstract class IssueProgressPanel extends DropdownLink {
	
	public IssueProgressPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(AttributeAppender.append("style", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				var issue = getIssue();
				var timeTrackingSetting = getTimeTrackingSetting();
				Integer estimationTime = (Integer) issue.getFieldValue(timeTrackingSetting.getEstimatedTimeField());
				Integer spentTime = (Integer) issue.getFieldValue(timeTrackingSetting.getSpentTimeField());
				var builder = new StringBuilder("width:20px;height:20px;border-radius:50%;");
				if (estimationTime == null || estimationTime <= 0) {
					builder.append("margin-bottom:6px;");
				} else {
					if (spentTime == null || spentTime <= 0)
						spentTime = 0;
					if (spentTime > estimationTime) {
						builder.append("border:1px solid #F64E60;background-image:conic-gradient(#F64E60 100%,transparent 0);");
					} else {
						int ratio = spentTime * 100 / estimationTime;
						if (ratio < 50) 
							builder.append(String.format("border:1px solid #FFA800;background-image:conic-gradient(#FFA800 %s,transparent 0);", ratio + "%"));
						else
							builder.append(String.format("border:1px solid #1BC5BD;background-image:conic-gradient(#1BC5BD %s,transparent 0);", ratio + "%"));							
					}
				}
				return builder.toString();
			}
			
		}));
		
		add(AttributeAppender.append("title", "Estimated/Spent time. Click for details"));
		
		add(new ChangeObserver() {
			@Override
			protected Collection<String> findObservables() {
				return Sets.newHashSet(Issue.getDetailChangeObservable(getIssue().getId()));
			}
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		var timeTrackingSetting = getTimeTrackingSetting();
		setVisible(timeTrackingSetting != null && timeTrackingSetting.isProjectApplicable(getIssue().getProject()));
	}

	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		var issue = getIssue();
		var timeTrackingSetting = getTimeTrackingSetting();
		Integer estimationTime = (Integer) issue.getFieldValue(timeTrackingSetting.getEstimatedTimeField());
		if (estimationTime == null ||  estimationTime <= 0)
			replaceComponentTagBody(markupStream, openTag, "?");
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new IssueTimePanel(id) {
			@Override
			protected Issue getIssue() {
				return IssueProgressPanel.this.getIssue();
			}

			@Override
			protected void closeDropdown() {
				dropdown.close();
			}

		};
	}

	@Nullable
	private TimeTrackingSetting getTimeTrackingSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getTimeTrackingSetting();
	}
	
	protected abstract Issue getIssue();
	
}
