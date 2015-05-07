package com.pmease.gitplex.web.component.treelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.PathInfo;

@SuppressWarnings("serial")
public abstract class TreeList extends GenericPanel<String> {

	public TreeList(String id, IModel<String> pathModel) {
		super(id, pathModel);
	}

	private String getNormalizedPath() {
		return GitUtils.normalizePath(getModelObject());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer parent = new WebMarkupContainer("parent") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getNormalizedPath() != null);
			}
			
		};
		parent.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				String path = getNormalizedPath();
				if (path.indexOf('/') != -1)
					path = StringUtils.substringBeforeLast(path, "/");
				else
					path = "";
				selectPath(target, path);
			}
			
		});
		add(parent);
		
		add(new ListView<PathInfo>("children", new LoadableDetachableModel<List<PathInfo>>() {

			@Override
			protected List<PathInfo> load() {
				String path = getNormalizedPath();
				org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
				try {
					RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
					TreeWalk treeWalk;
					if (path != null) {
						treeWalk = TreeWalk.forPath(jgitRepo, path, revTree);
						if (treeWalk != null) {
							treeWalk.enterSubtree();
						} else {
							path = null;
							treeWalk = new TreeWalk(jgitRepo);
							treeWalk.addTree(revTree);
						}
					} else {
						treeWalk = new TreeWalk(jgitRepo);
						treeWalk.addTree(revTree);
					}
					List<PathInfo> children = new ArrayList<>();
					while (treeWalk.next())
						children.add(new PathInfo(treeWalk.getPathString(), treeWalk.getRawMode(0)));
					return children;
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					jgitRepo.close();
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<PathInfo> item) {
				final PathInfo pathInfo = item.getModelObject();
				
				WebMarkupContainer pathIcon = new WebMarkupContainer("pathIcon");
				String iconClass;
				if (FileMode.TREE.equals(pathInfo.getMode()))
					iconClass = "fa fa-folder-o";
				else if (FileMode.GITLINK.equals(pathInfo.getMode())) 
					iconClass = "fa fa-ext fa-submodule-o";
				else if (FileMode.SYMLINK.equals(pathInfo.getMode())) 
					iconClass = "fa fa-ext fa-symbol-link";
				else  
					iconClass = "fa fa-file-text-o";
				pathIcon.add(AttributeModifier.append("class", iconClass));
				
				item.add(pathIcon);
				
				AjaxLink<Void> pathLink = new AjaxLink<Void>("pathLink") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						selectPath(target, pathInfo.getPath());
					}
					
				}; 
				if (pathInfo.getPath().indexOf('/') != -1)
					pathLink.add(new Label("label", StringUtils.substringAfterLast(pathInfo.getPath(), "/")));
				else
					pathLink.add(new Label("label", pathInfo.getPath()));
				item.add(pathLink);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private void selectPath(AjaxRequestTarget target, String path) {
		if (path.length() == 0)
			path = null;
		setModelObject(path);
		target.add(this);
	}
	
	private ObjectId getCommitId() {
		return Preconditions.checkNotNull(getRepository().resolveRevision(getRevision()));
	}
	
	protected abstract Repository getRepository();
	
	protected abstract String getRevision();
	
}
