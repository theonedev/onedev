package com.pmease.gitplex.web.component.blobview;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.assets.closestdescendant.ClosestDescendantResourceReference;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.editsave.CancelListener;
import com.pmease.gitplex.web.component.editsave.EditSavePanel;
import com.pmease.gitplex.web.component.fileedit.FileEditPanel;
import com.pmease.gitplex.web.page.repository.file.HistoryState;
import com.pmease.gitplex.web.resource.BlobResource;
import com.pmease.gitplex.web.resource.BlobResourceReference;

@SuppressWarnings("serial")
public abstract class BlobViewPanel extends Panel {

	protected final BlobViewContext context;
	
	private Component editPanel;
	
	public BlobViewPanel(String id, BlobViewContext context) {
		super(id);
		
		HistoryState state = context.getState();
		Preconditions.checkArgument(state.file.revision != null 
				&& state.file.path != null && state.file.mode != null);
		
		this.context = context;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("lines", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return context.getBlob().getText().getLines().size() + " lines";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getBlob().getText() != null);
			}
			
		});
		
		add(new Label("charset", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return context.getBlob().getText().getCharset().displayName();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getBlob().getText() != null);
			}
			
		});
		
		add(new Label("size", FileUtils.byteCountToDisplaySize(context.getBlob().getSize())));

		add(new ResourceLink<Void>("raw", new BlobResourceReference(), 
				BlobResource.paramsOf(context.getRepository(), context.getState().file)));
		
		add(new AjaxLink<Void>("blame") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (context.getState().blame)
							return " active";
						else
							return " ";
					}
					
				}));
				
				setOutputMarkupId(true);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				context.getState().blame = !context.getState().blame;
				context.onBlameChange(target);
				
				// this blob view panel might be replaced with another panel
				if (findPage() != null) {
					target.add(this);
					target.focusComponent(null);
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getBlob().getText() != null);
			}
			
		});
		add(new AjaxLink<Void>("history") {

			@Override
			public void onClick(AjaxRequestTarget target) {
			}
			
		});
		
		add(newCustomActions("customActions"));
		
		WebMarkupContainer changeActions = new WebMarkupContainer("changeActions") {
			
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (!context.getRepository().getRefs(Git.REFS_HEADS).containsKey(context.getState().file.revision))
					tag.put("title", "Must on a branch to change or propose change of this file");
			}
			
		};
		add(changeActions);
		
		changeActions.add(new AjaxLink<Void>("edit") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getBlob().getText() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (!context.getRepository().getRefs(Git.REFS_HEADS).containsKey(context.getState().file.revision))
					tag.put("disabled", "disabled");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				BlobViewPanel blobViewPanel = BlobViewPanel.this;
				String panelId = blobViewPanel.getId();
				IModel<Repository> repoModel = new AbstractReadOnlyModel<Repository>() {

					@Override
					public Repository getObject() {
						return context.getRepository();
					}
					
				};
				
				ObjectId commitId = context.getRepository().getObjectId(
						context.getState().file.revision, true);
				
				final BlobIdent file = context.getState().file;
				String refName = Git.REFS_HEADS + file.revision;
				
				final AtomicReference<String> newPathRef = new AtomicReference<>();
				
				editPanel = new FileEditPanel(panelId, repoModel, refName, 
						file.path, context.getBlob().getText().getContent(), commitId) {

					@Override
					protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
						Repository repo = context.getRepository();
						repo.cacheObjectId(file.revision, newCommitId);
						
						String newPath = newPathRef.get();
						if (file.path.equals(newPath)) {
							restoreViewPanel(target);
							context.onEditDone(target);
						} else { 
							context.onSelect(target, new BlobIdent(file.revision, newPath, file.mode), null);
						}
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						restoreViewPanel(target);
						context.onEditDone(target);
					}
					
				};
				final BlobNameChangeCallback callback = new BlobNameChangeCallback() {

					@Override
					public void onChange(AjaxRequestTarget target, String blobName) {
						String newPath;
						if (file.path.contains("/"))
							newPath = StringUtils.substringBeforeLast(file.path, "/") + "/" + blobName;
						else
							newPath = blobName;
						newPathRef.set(GitUtils.normalizePath(newPath));
						((FileEditPanel)editPanel).onNewPathChange(target, newPathRef.get());
					}
					
				};
				context.onEdit(target, callback);
				blobViewPanel.replaceWith(editPanel);
				target.add(editPanel);
			}
			
		});
		
		changeActions.add(new AjaxLink<Void>("delete") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (!context.getRepository().getRefs(Git.REFS_HEADS).containsKey(context.getState().file.revision))
					tag.put("disabled", "disabled");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				BlobViewPanel blobViewPanel = BlobViewPanel.this;
				String panelId = blobViewPanel.getId();
				IModel<Repository> repoModel = new AbstractReadOnlyModel<Repository>() {

					@Override
					public Repository getObject() {
						return context.getRepository();
					}
					
				};
				
				ObjectId commitId = context.getRepository().getObjectId(
						context.getState().file.revision, true);
				
				final BlobIdent file = context.getState().file;
				String refName = Git.REFS_HEADS + file.revision;

				CancelListener cancelListener = new CancelListener() {

					@Override
					public void onCancel(AjaxRequestTarget target) {
						restoreViewPanel(target);
					}
					
				};
				editPanel = new EditSavePanel(panelId, repoModel, refName, file.path, 
						null, commitId, cancelListener) {

					@Override
					protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
						Repository repo = context.getRepository();
						repo.cacheObjectId(file.revision, newCommitId);
						try (	FileRepository jgitRepo = repo.openAsJGitRepo();
								RevWalk revWalk = new RevWalk(jgitRepo)) {
							RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
							String parentPath = StringUtils.substringBeforeLast(file.path, "/");
							while (TreeWalk.forPath(jgitRepo, parentPath, revTree) == null) {
								if (parentPath.contains("/")) {
									parentPath = StringUtils.substringBeforeLast(parentPath, "/");
								} else {
									parentPath = null;
									break;
								}
							}
							BlobIdent parentBlobIdent = new BlobIdent(file.revision, parentPath, FileMode.TREE.getBits());
							context.onSelect(target, parentBlobIdent, null);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					
				};
				blobViewPanel.replaceWith(editPanel);
				target.add(editPanel);
			}

		});

		setOutputMarkupId(true);
	}
	
	private void restoreViewPanel(AjaxRequestTarget target) {
		if (editPanel != null) {
			BlobViewPanel blobViewPanel = context.render(getId());
			editPanel.replaceWith(blobViewPanel);
			target.add(blobViewPanel);
			target.appendJavaScript("$(window).resize();");
			editPanel = null;
		}
	}

	protected WebMarkupContainer newCustomActions(String id) {
		return new WebMarkupContainer(id);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				ClosestDescendantResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(BlobViewPanel.class, "blob-view.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(BlobViewPanel.class, "blob-view.css")));
		
		response.render(OnDomReadyHeaderItem.forScript(String.format("gitplex.blobView('%s');", getMarkupId())));
	}

	public BlobViewContext getContext() {
		return context;
	}
	
}
