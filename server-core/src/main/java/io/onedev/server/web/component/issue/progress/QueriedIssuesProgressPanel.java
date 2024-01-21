package io.onedev.server.web.component.issue.progress;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.IssueTimes;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.behavior.CompletionRateBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import javax.annotation.Nullable;

import static io.onedev.server.util.DateUtils.formatWorkingPeriod;

public abstract class QueriedIssuesProgressPanel extends Panel {
	
	private final IModel<IssueTimes> timesModel = new LoadableDetachableModel<IssueTimes>() {
		@Override
		protected IssueTimes load() {
			return OneDev.getInstance(IssueManager.class).queryTimes(getProjectScope(), getQuery().getCriteria());
		}
	};
	
	public QueriedIssuesProgressPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getQuery() != null) {
			var fragment = new Fragment("content", "hasQueryFrag", this);
			fragment.add(new WebMarkupContainer("completion").add(new CompletionRateBehavior() {
				@Override
				protected long getTotal() {
					return timesModel.getObject().getEstimatedTime();
				}

				@Override
				protected long getCompleted() {
					return timesModel.getObject().getSpentTime();
				}
			}));
			
			fragment.add(new Label("estimatedTime", formatWorkingPeriod(timesModel.getObject().getEstimatedTime())));
			fragment.add(new Label("spentTime", formatWorkingPeriod(timesModel.getObject().getSpentTime())));
			add(fragment);
		} else {
			add(new Fragment("content", "noQueryFrag", this));
		}
	}

	@Override
	protected void onDetach() {
		timesModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProgressResourceReference()));
	}

	protected abstract ProjectScope getProjectScope();
	
	@Nullable
	protected abstract IssueQuery getQuery();
}
