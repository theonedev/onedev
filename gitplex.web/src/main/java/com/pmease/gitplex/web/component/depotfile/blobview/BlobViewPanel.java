package com.pmease.gitplex.web.component.depotfile.blobview;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;
import com.pmease.commons.wicket.component.ViewStateAwareAjaxLink;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.comment.CodeCommentToggled;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext.Mode;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.resource.BlobResource;
import com.pmease.gitplex.web.resource.BlobResourceReference;

@SuppressWarnings("serial")
public abstract class BlobViewPanel extends Panel {

	protected final BlobViewContext context;
	
	public BlobViewPanel(String id, BlobViewContext context) {
		super(id);
		
		BlobIdent blobIdent = context.getBlobIdent();
		Preconditions.checkArgument(blobIdent.revision != null 
				&& blobIdent.path != null && blobIdent.mode != null);
		
		this.context = context;
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof CodeCommentToggled) {
			CodeCommentToggled codeCommentToggled = (CodeCommentToggled) event.getPayload();
			newChangeActions(codeCommentToggled.getPartialPageRequestHandler());
		} 
	}
	
	protected WebMarkupContainer newAdditionalActions(String id) {
		return new WebMarkupContainer(id);
	}

	private void newChangeActions(@Nullable IPartialPageRequestHandler target) {
		WebMarkupContainer changeActions = new WebMarkupContainer("changeActions");
		CodeComment comment = context.getOpenComment();
		changeActions.setVisible(SecurityUtils.canModify(context.getDepot(), context.getBlobIdent().revision, context.getBlobIdent().path) 
				&& (context.isOnBranch() || context.isOnCommentBranch()));
		changeActions.setOutputMarkupId(true);
		
		if (context.getDepot().getBlob(context.getBlobIdent()).getText() != null) {
			if (context.isOnBranch()) {
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("edit") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					public void onClick(AjaxRequestTarget target, String viewState) {
						RequestCycle.get().setMetaData(DepotFilePage.VIEW_STATE_KEY, viewState);
						context.onEdit(target);
					}
				};
				link.add(AttributeAppender.append("title", "Edit on branch " + context.getBlobIdent().revision));
				changeActions.add(link);
			} else if (context.isOnCommentBranch()) {
				PageParameters params;
				DepotFilePage.State state = new DepotFilePage.State();
				state.blobIdent.revision = GitUtils.ref2branch(context.getOpenComment().getBranchRef());
				state.blobIdent.path = context.getBlobIdent().path;
				state.commentId = comment.getId();
				state.requestId = PullRequest.idOf(context.getPullRequest());
				state.mode = Mode.EDIT;
				params = DepotFilePage.paramsOf(context.getDepot(), state);
				Link<Void> link = new BookmarkablePageLink<Void>("edit", DepotFilePage.class, params);
				link.add(AttributeAppender.append("title", "Edit on branch " + state.blobIdent.revision));
				changeActions.add(link);
			} else {
				changeActions.add(new WebMarkupContainer("edit").setVisible(false));
			}
		} else {
			changeActions.add(new WebMarkupContainer("edit").setVisible(false));
		}
		
		if (context.isOnBranch()) {
			AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("delete") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					context.onDelete(target);
				}

			};
			link.add(AttributeAppender.append("title", "Delete from branch " + context.getBlobIdent().revision));
			changeActions.add(link);
		} else if (context.isOnCommentBranch()) {
			DepotFilePage.State state = new DepotFilePage.State();
			state.blobIdent.revision = GitUtils.ref2branch(context.getOpenComment().getBranchRef());
			state.blobIdent.path = context.getBlobIdent().path;
			state.commentId = comment.getId();
			state.requestId = PullRequest.idOf(context.getPullRequest());
			state.mode = Mode.DELETE;
			PageParameters params = DepotFilePage.paramsOf(context.getDepot(), state);
			Link<Void> link = new BookmarkablePageLink<Void>("delete", DepotFilePage.class, params);
			link.add(AttributeAppender.append("title", "Delete from branch " + state.blobIdent.revision));
			changeActions.add(link);
		} else {
			changeActions.add(new WebMarkupContainer("delete").setVisible(false));
		}
		
		if (target != null) {
			replace(changeActions);
			target.add(changeActions);
		} else {
			add(changeActions);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ResourceLink<Void>("raw", new BlobResourceReference(), 
				BlobResource.paramsOf(context.getDepot(), context.getBlobIdent())));
		add(newAdditionalActions("additionalActions"));
		newChangeActions(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new BlobViewResourceReference()));
		
		response.render(OnDomReadyHeaderItem.forScript(String.format("gitplex.blobView('%s');", getMarkupId())));
	}

	public BlobViewContext getContext() {
		return context;
	}
	
}
