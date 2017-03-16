package com.gitplex.server.web.component.markdown;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.manager.MarkdownManager;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class MarkdownViewer extends GenericPanel<String> {

	private final ResponsiveTaskBehavior responsiveTaskBehavior;
	
	public MarkdownViewer(String id, IModel<String> model, @Nullable ResponsiveTaskBehavior responsiveTaskBehavior) {
		super(id, model);
		
		this.responsiveTaskBehavior = responsiveTaskBehavior;
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
				String markdown = MarkdownViewer.this.getModelObject();
				if (markdown != null) {
					return AppLoader.getInstance(MarkdownManager.class).render(markdown, true);
				} else {
					return null;
				}
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				if (responsiveTaskBehavior != null)
					add(responsiveTaskBehavior);
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				String script = String.format("gitplex.server.markdown.initRendered($('#%s'));", getMarkupId(true));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		}.setEscapeModelStrings(false));
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
	}

}
