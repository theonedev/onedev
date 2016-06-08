package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.integrationpreview;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.IntegrationPreview;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.websocket.PullRequestChanged;

@SuppressWarnings("serial")
public class IntegrationPreviewPage extends RequestDetailPage {

	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	private State state = new State();
	
	public IntegrationPreviewPage(PageParameters params) {
		super(params);

		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.whitespaceOption = WhitespaceOption.of(params.get(PARAM_WHITESPACE_OPTION).toString());
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		newContent(null);
	}

	public static PageParameters paramsOf(PullRequest request, State state) {
		PageParameters params = RequestDetailPage.paramsOf(request);

		if (state.whitespaceOption != WhitespaceOption.DEFAULT)
			params.set(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.set(PARAM_PATH_FILTER, state.pathFilter);
		if (state.blameFile != null)
			params.set(PARAM_BLAME_FILE, state.blameFile);
			
		return params;
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);

		state = (State) data;
		
		newContent(target);
	}
	
	private void newContent(IPartialPageRequestHandler target) {
		Fragment fragment;
		IntegrationPreview preview = getPullRequest().getIntegrationPreview();
		if (getPullRequest().isOpen() && preview != null && preview.getIntegrated() != null) {
			fragment = new Fragment("content", "availableFrag", this);
			fragment.add(new AjaxLink<Void>("refresh") {

				@Override
				public void onEvent(IEvent<?> event) {
					super.onEvent(event);

					if (event.getPayload() instanceof PullRequestChanged) {
						PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
						pullRequestChanged.getPartialPageRequestHandler().add(this);
					}
				}
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					IntegrationPreview latestPreview = getPullRequest().getIntegrationPreview();
					setVisible(!getPullRequest().isOpen() 
							|| latestPreview == null 
							|| latestPreview.getIntegrated() == null 
							|| !latestPreview.getTargetHead().equals(preview.getTargetHead())
							|| !latestPreview.getIntegrated().equals(preview.getIntegrated()));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					newContent(target);
				}
				
			}.setOutputMarkupPlaceholderTag(true));
			
			IModel<String> blameModel = new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return state.blameFile;
				}

				@Override
				public void setObject(String object) {
					state.blameFile = object;
					pushState(RequestCycle.get().find(AjaxRequestTarget.class));
				}
				
			};
			IModel<String> pathFilterModel = new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return state.pathFilter;
				}

				@Override
				public void setObject(String object) {
					state.pathFilter = object;
					pushState(RequestCycle.get().find(AjaxRequestTarget.class));
				}
				
			};
			IModel<WhitespaceOption> whitespaceOptionModel = new IModel<WhitespaceOption>() {

				@Override
				public void detach() {
				}

				@Override
				public WhitespaceOption getObject() {
					return state.whitespaceOption;
				}

				@Override
				public void setObject(WhitespaceOption object) {
					state.whitespaceOption = object;
					pushState(RequestCycle.get().find(AjaxRequestTarget.class));
				}

			};
			
			Component revisionDiff = new RevisionDiffPanel("revisionDiff", depotModel,  
					requestModel, preview.getTargetHead(), preview.getIntegrated(), pathFilterModel, 
					whitespaceOptionModel, blameModel, null);
			revisionDiff.setOutputMarkupId(true);
			fragment.add(revisionDiff);
		} else {
			fragment = new Fragment("content", "notAvailableFrag", this) {

				@Override
				public void onEvent(IEvent<?> event) {
					super.onEvent(event);

					if (event.getPayload() instanceof PullRequestChanged) {
						PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
						newContent(pullRequestChanged.getPartialPageRequestHandler());
					}
				}
				
			};
		}
		fragment.setOutputMarkupId(true);
		
		if (target != null) {
			replace(fragment);
			target.add(fragment);
		} else {
			add(fragment);
		}
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RequestListPage.class, paramsOf(depot));
	}

	private void pushState(IPartialPageRequestHandler partialPageRequestHandler) {
		PageParameters params = paramsOf(getPullRequest(), state);
		CharSequence url = RequestCycle.get().urlFor(IntegrationPreviewPage.class, params);
		pushState(partialPageRequestHandler, url.toString(), state);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(IntegrationPreviewPage.class, "integration-preview.css")));
	}

	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;

		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
		
		@Nullable
		public String pathFilter;
		
		@Nullable
		private String blameFile;
		
	}

}
