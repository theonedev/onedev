package com.pmease.gitplex.web.component.sourceview;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class SourceViewPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<Source> sourceModel;
	
	private Component codeComp;
	
	public SourceViewPanel(String id, IModel<Repository> repoModel, IModel<Source> sourceModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.sourceModel = sourceModel;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(codeComp = new WebMarkupContainer("code"));
		codeComp.setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

//		response.render(JavaScriptHeaderItem.forReference(AceModeListResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(SourceViewPanel.class, "source-view.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(SourceViewPanel.class, "source-view.css")));
		
		String script = String.format("gitplex.sourceview.init('%s', '%s', '%s');", 
				codeComp.getMarkupId(), 
				StringEscapeUtils.escapeEcmaScript(sourceModel.getObject().getContent()),
				sourceModel.getObject().getPath());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		sourceModel.detach();
		
		super.onDetach();
	}

}
