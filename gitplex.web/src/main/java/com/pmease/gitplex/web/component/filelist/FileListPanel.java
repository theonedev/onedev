package com.pmease.gitplex.web.component.filelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public abstract class FileListPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final BlobIdent directory;
	
	public FileListPanel(String id, IModel<Repository> repoModel, BlobIdent directory) {
		super(id);

		this.repoModel = repoModel;
		this.directory = directory;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer parent = new WebMarkupContainer("parent") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(directory.path != null);
			}
			
		};
		
		final BlobIdent parentIdent;
		if (directory.path == null) {
			parentIdent = null;
		} else if (directory.path.indexOf('/') != -1) {
			parentIdent = new BlobIdent(
					directory.revision, 
					StringUtils.substringBeforeLast(directory.path, "/"), 
					FileMode.TREE.getBits());
		} else {
			parentIdent = new BlobIdent(directory.revision, null, FileMode.TREE.getBits());
		}
		parent.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, parentIdent);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				PageParameters params = RepoFilePage.paramsOf(repoModel.getObject(), parentIdent); 
				tag.put("href", urlFor(RepoFilePage.class, params));
			}
			
		});
		add(parent);
		
		add(new ListView<BlobIdent>("children", new LoadableDetachableModel<List<BlobIdent>>() {

			@Override
			protected List<BlobIdent> load() {
				org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
				try {
					RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
					TreeWalk treeWalk;
					if (directory.path != null) {
						treeWalk = Preconditions.checkNotNull(TreeWalk.forPath(jgitRepo, directory.path, revTree));
						treeWalk.enterSubtree();
					} else {
						treeWalk = new TreeWalk(jgitRepo);
						treeWalk.addTree(revTree);
					}
					List<BlobIdent> children = new ArrayList<>();
					while (treeWalk.next())
						children.add(new BlobIdent(directory.revision, treeWalk.getPathString(), treeWalk.getRawMode(0)));
					for (int i=0; i<children.size(); i++) {
						BlobIdent child = children.get(i);
						while (child.isTree()) {
							treeWalk = TreeWalk.forPath(jgitRepo, child.path, revTree);
							Preconditions.checkNotNull(treeWalk);
							treeWalk.enterSubtree();
							if (treeWalk.next()) {
								BlobIdent grandChild = new BlobIdent(directory.revision, 
										treeWalk.getPathString(), treeWalk.getRawMode(0));
								if (treeWalk.next()) 
									break;
								else
									child = grandChild;
							} else {
								break;
							}
						}
						children.set(i, child);
					}
					
					Collections.sort(children);
					return children;
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					jgitRepo.close();
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<BlobIdent> item) {
				final BlobIdent blobIdent = item.getModelObject();
				
				WebMarkupContainer pathIcon = new WebMarkupContainer("pathIcon");
				String iconClass;
				if (blobIdent.isTree())
					iconClass = "fa fa-folder-o";
				else if (blobIdent.isGitLink()) 
					iconClass = "fa fa-ext fa-folder-submodule-o";
				else if (blobIdent.isSymbolLink()) 
					iconClass = "fa fa-ext fa-folder-symbol-link-o";
				else  
					iconClass = "fa fa-file-text-o";
				pathIcon.add(AttributeModifier.append("class", iconClass));
				
				item.add(pathIcon);
				
				AjaxLink<Void> pathLink = new AjaxLink<Void>("pathLink") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						PageParameters params = RepoFilePage.paramsOf(repoModel.getObject(), blobIdent); 
						tag.put("href", urlFor(RepoFilePage.class, params));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, blobIdent);
					}
					
				}; 
				
				if (directory.path != null)
					pathLink.add(new Label("label", blobIdent.path.substring(directory.path.length()+1)));
				else
					pathLink.add(new Label("label", blobIdent.path));
				item.add(pathLink);
				
				if (item.getIndex() == 0)
					item.add(new Label("lastCommitSummary", "Loading last commit info..."));
				else
					item.add(new Label("lastCommitSummary"));
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(FileListPanel.class, "file-list.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(FileListPanel.class, "file-list.css")));
		
		PageParameters params = LastCommitsResource.paramsOf(repoModel.getObject(), directory.revision, directory.path); 
		String lastCommitsUrl = urlFor(new LastCommitsResourceReference(), params).toString();
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("gitplex.filelist.init('%s', '%s')", getMarkupId(), lastCommitsUrl)));
	}

	protected abstract void onSelect(AjaxRequestTarget target, BlobIdent file);
	
	private ObjectId getCommitId() {
		return repoModel.getObject().getObjectId(directory.revision, true);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
