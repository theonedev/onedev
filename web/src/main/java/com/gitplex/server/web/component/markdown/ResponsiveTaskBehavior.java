package com.gitplex.server.web.component.markdown;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.StaleStateException;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.MarkdownManager;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;
import com.gitplex.server.web.behavior.markdown.MarkdownResourceReference;
import com.gitplex.server.web.behavior.markdown.SourcePositionTrackExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;

@SuppressWarnings("serial")
public abstract class ResponsiveTaskBehavior extends AbstractPostAjaxBehavior {

	private static final String TASK_CHECKED = "taskchecked";
	
	private long lastContentVersion;
	
	public ResponsiveTaskBehavior() {
		lastContentVersion = getContentVersion();
	}
	
	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
		int taskPosition = params.getParameterValue(SourcePositionTrackExtension.DATA_START_ATTRIBUTE).toInt();
		boolean taskChecked = params.getParameterValue(TASK_CHECKED).toBoolean();
		String markdown = getComponent().getDefaultModelObjectAsString();
		String beforeTask = markdown.substring(0, taskPosition);
		String fromTask = markdown.substring(taskPosition);
		String beforeBracket = StringUtils.substringBefore(fromTask, "[");
		String afterBracket = StringUtils.substringAfter(fromTask, "]");
		String taskStatus = taskChecked?"x":" ";
		markdown = beforeTask + beforeBracket + "[" + taskStatus + "]" + afterBracket;

		try {
			if (getContentVersion() != lastContentVersion)
				throw new StaleStateException("");
			getComponent().setDefaultModelObject(markdown);
		} catch (StaleStateException e) {
			getComponent().warn("Some one changed the content you are editing. The content has now been "
					+ "reloaded, please try again.");
		}
		target.add(getComponent());
		lastContentVersion = getContentVersion();
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
		
		CharSequence callback = getCallbackFunction(
				explicit(SourcePositionTrackExtension.DATA_START_ATTRIBUTE), 
				explicit(TASK_CHECKED));
		
		String taskItemClass = GitPlex.getInstance(MarkdownManager.class).getOptions().get(TaskListExtension.ITEM_CLASS);
		String script = String.format("gitplex.server.markdown.initResponsiveTask('%s', %s, '%s', '%s');", 
				getComponent().getMarkupId(), callback, taskItemClass, SourcePositionTrackExtension.DATA_START_ATTRIBUTE);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		getComponent().setOutputMarkupId(true);
	}

	protected abstract long getContentVersion();
	
}
