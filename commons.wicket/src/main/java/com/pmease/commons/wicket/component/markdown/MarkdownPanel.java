package com.pmease.commons.wicket.component.markdown;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.StaleStateException;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.AbstractPostAjaxBehavior;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class MarkdownPanel extends GenericPanel<String> {

	private final MarkdownEditSupport editSupport;
	
	private long lastVersion;
	
	public MarkdownPanel(String id, IModel<String> model, @Nullable MarkdownEditSupport editSupport) {
		super(id, model);
		
		this.editSupport = editSupport;
		if (editSupport != null)
			lastVersion = editSupport.getVersion();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		NotificationPanel feedback = new NotificationPanel("feedback", this);
		feedback.setOutputMarkupPlaceholderTag(true);
		add(feedback);
		
		add(new Label("content", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String markdown = MarkdownPanel.this.getModelObject();
				if (markdown != null) {
					return AppLoader.getInstance(MarkdownManager.class).parseAndProcess(markdown);
				} else {
					return null;
				}
			}
		}).setEscapeModelStrings(false));

		add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				int taskStartIndex = params.getParameterValue("taskStartIndex").toInt();
				boolean taskChecked = params.getParameterValue("taskChecked").toBoolean();
				String markdown = getModelObject();
				String beforeTask = markdown.substring(0, taskStartIndex);
				String beforeBracket = StringUtils.substringBeforeLast(beforeTask, "[");
				String afterBracket = StringUtils.substringAfterLast(beforeTask, "]");
				String taskStatus = taskChecked?"x":" ";
				String newMarkdown = beforeBracket + "[" + taskStatus + "]" 
						+ afterBracket + markdown.substring(taskStartIndex);
				
				try {
					if (editSupport.getVersion() != lastVersion)
						throw new StaleStateException("");
					editSupport.setContent(newMarkdown);
					target.add(feedback); // clear the feedback
				} catch (StaleStateException e) {
					warn("Some one changed the content you are editing. The content has now been "
							+ "reloaded, please try again.");
					target.add(MarkdownPanel.this);
				}
				lastVersion = editSupport.getVersion();
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
				
				CharSequence callbackFunc = getCallbackFunction(
						explicit("taskStartIndex"), explicit("taskChecked"));
				String script = String.format("pmease.commons.initMarkdownPanel($('#%s>.markdown-panel>.md-preview'), %s);", 
						getMarkupId(true), editSupport!=null?callbackFunc:"undefined");
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		setOutputMarkupId(true);
	}

}
