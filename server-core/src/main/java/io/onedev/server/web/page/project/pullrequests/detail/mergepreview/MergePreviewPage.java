package io.onedev.server.web.page.project.pullrequests.detail.mergepreview;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.search.code.CommitIndexed;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.commit.status.CommitStatusPanel;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.websocket.PageDataChanged;
import io.onedev.server.web.websocket.WebSocketManager;

@SuppressWarnings("serial")
public class MergePreviewPage extends PullRequestDetailPage {

	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	private State state = new State();
	
	public MergePreviewPage(PageParameters params) {
		super(params);

		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.whitespaceOption = WhitespaceOption.ofNullableName(params.get(PARAM_WHITESPACE_OPTION).toString());
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		newContent(null);
	}

	public static PageParameters paramsOf(PullRequest request, @Nullable QueryPosition position, State state) {
		PageParameters params = PullRequestDetailPage.paramsOf(request, position);

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
		MergePreview preview = getPullRequest().getMergePreview();
		if (getPullRequest().isOpen() && preview != null && preview.getMerged() != null) {
			fragment = new Fragment("content", "availableFrag", this);

			CommitDetailPage.State commitState = new CommitDetailPage.State();
			commitState.revision = preview.getTargetHead();
			PageParameters params = CommitDetailPage.paramsOf(projectModel.getObject(), commitState);
			Link<Void> hashLink = new ViewStateAwarePageLink<Void>("targetHead", CommitDetailPage.class, params);
			fragment.add(hashLink);
			hashLink.add(new Label("hash", GitUtils.abbreviateSHA(preview.getTargetHead())));
			fragment.add(new WebMarkupContainer("copyTargetHead").add(new CopyClipboardBehavior(Model.of(preview.getTargetHead()))));
			
			commitState = new CommitDetailPage.State();
			commitState.revision = preview.getMerged();
			params = CommitDetailPage.paramsOf(projectModel.getObject(), commitState);
			hashLink = new ViewStateAwarePageLink<Void>("mergedCommit", CommitDetailPage.class, params);
			fragment.add(hashLink);
			hashLink.add(new Label("hash", GitUtils.abbreviateSHA(preview.getMerged())));
			fragment.add(new WebMarkupContainer("copyMergedCommit").add(new CopyClipboardBehavior(Model.of(preview.getMerged()))));
			fragment.add(new CommitStatusPanel("buildStatus", getProject(), ObjectId.fromString(preview.getMerged())) {

				@Override
				protected String getCssClasses() {
					return "btn btn-default";
				}
				
			});
			fragment.add(new WebMarkupContainer("outDated") {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new AjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							newContent(target);
						}
						
					});
				}
				
				@Override
				public void onEvent(IEvent<?> event) {
					super.onEvent(event);

					if (event.getPayload() instanceof PageDataChanged) {
						PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
						pageDataChanged.getHandler().add(this);
						OneDev.getInstance(WebSocketManager.class).notifyObserverChange(MergePreviewPage.this);
					}
				}
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					MergePreview latestPreview = getPullRequest().getMergePreview();
					setVisible(!getPullRequest().isOpen() 
							|| latestPreview == null 
							|| latestPreview.getMerged() == null 
							|| !latestPreview.getTargetHead().equals(preview.getTargetHead())
							|| !latestPreview.getMerged().equals(preview.getMerged()));
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
			
			Component revisionDiff = new RevisionDiffPanel("revisionDiff", projectModel,  
					requestModel, preview.getTargetHead(), preview.getMerged(), pathFilterModel, 
					whitespaceOptionModel, blameModel, null);
			revisionDiff.setOutputMarkupId(true);
			fragment.add(revisionDiff);
		} else {
			fragment = new Fragment("content", "notAvailableFrag", this) {

				@Override
				public void onEvent(IEvent<?> event) {
					super.onEvent(event);

					if (event.getPayload() instanceof PageDataChanged) {
						PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
						newContent(pageDataChanged.getHandler());
						OneDev.getInstance(WebSocketManager.class).notifyObserverChange(MergePreviewPage.this);
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
	
	public State getState() {
		return state;
	}
	
	@Override
	public Collection<String> getWebSocketObservables() {
		Collection<String> regions = super.getWebSocketObservables();
		MergePreview preview = getPullRequest().getMergePreview();
		if (getPullRequest().isOpen() && preview != null && preview.getMerged() != null) {
			regions.add(CommitIndexed.getWebSocketObservable(preview.getTargetHead()));
			regions.add(CommitIndexed.getWebSocketObservable(preview.getMerged()));
		}		
		return regions;
	}
	
	private void pushState(IPartialPageRequestHandler partialPageRequestHandler) {
		PageParameters params = paramsOf(getPullRequest(), getPosition(), state);
		CharSequence url = RequestCycle.get().urlFor(MergePreviewPage.class, params);
		pushState(partialPageRequestHandler, url.toString(), state);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MergePreviewResourceReference()));
	}

	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;

		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
		
		@Nullable
		public String pathFilter;
		
		@Nullable
		public String blameFile;
		
	}

}
