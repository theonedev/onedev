package com.pmease.gitplex.web.component.pathnavigator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class PathNavigator extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<String> revModel;
	
	private final List<String> pathSegments = new ArrayList<>();
	
	public PathNavigator(String id, IModel<Repository> repoModel, IModel<String> revModel, @Nullable String initialPath) {
		super(id);
		
		this.repoModel = repoModel;
		this.revModel = revModel;
		
		if (StringUtils.isNotBlank(initialPath))
			pathSegments.addAll(Splitter.on('/').omitEmptyStrings().splitToList(initialPath));
		pathSegments.add(0, repoModel.getObject().getName());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ListView<String>("pathSegments", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				String path = Joiner.on('/').join(pathSegments.subList(1, pathSegments.size()));
				if (path.length() != 0) {
					org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
					try {
						RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
						if (TreeWalk.forPath(jgitRepo, path, revTree) == null) {
							pathSegments.clear();
							pathSegments.add(repoModel.getObject().getName());
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						jgitRepo.close();
					}
				}
				
				return pathSegments;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<String> item) {
				final String currentPath = Joiner.on('/').join(pathSegments.subList(1, item.getIndex()+1));
				
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						selectPath(target, currentPath);
					}
					
				};
				link.setEnabled(item.getIndex() != pathSegments.size()-1);
				link.add(new Label("label", item.getModelObject()));
				item.add(link);
				
				WebMarkupContainer subtreeDropdownTrigger = new WebMarkupContainer("subtreeDropdownTrigger");
				
				if (currentPath.length() != 0 && item.getIndex() == pathSegments.size()-1) {
					org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
					try {
						RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, currentPath, revTree);
						if (!treeWalk.isSubtree())
							subtreeDropdownTrigger.setVisible(false);
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						jgitRepo.close();
					}
				}
				
				DropdownPanel subtreeDropdown = new DropdownPanel("subtreeDropdown", true) {

					@Override
					protected Component newContent(String id) {
						return new NestedTree<PathInfo>(id, new ITreeProvider<PathInfo>() {

							@Override
							public void detach() {
							}

							@Override
							public Iterator<? extends PathInfo> getRoots() {
								org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
								try {
									RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
									TreeWalk treeWalk;
									if (currentPath.length() != 0) {
										treeWalk = TreeWalk.forPath(jgitRepo, currentPath, revTree);
										treeWalk.enterSubtree();
									} else {
										treeWalk = new TreeWalk(jgitRepo);
										treeWalk.addTree(revTree);
									}
									treeWalk.setRecursive(false);
									
									List<PathInfo> roots = new ArrayList<>();
									while (treeWalk.next()) 
										roots.add(new PathInfo(treeWalk.getPathString(), treeWalk.getRawMode(0)));
									return roots.iterator();
								} catch (IOException e) {
									throw new RuntimeException(e);
								} finally {
									jgitRepo.close();
								}
							}

							@Override
							public boolean hasChildren(PathInfo pathInfo) {
								return FileMode.TREE.equals(pathInfo.getMode());
							}

							@Override
							public Iterator<? extends PathInfo> getChildren(PathInfo pathInfo) {
								return PathNavigator.this.getChildren(pathInfo).iterator();
							}

							@Override
							public IModel<PathInfo> model(PathInfo pathInfo) {
								return Model.of(pathInfo);
							}
							
						}) {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(new HumanTheme());				
							}

							@Override
							public void expand(PathInfo pathInfo) {
								super.expand(pathInfo);
								
								List<PathInfo> children = getChildren(pathInfo);
								if (children.size() == 1 && FileMode.TREE.equals(children.get(0).getMode())) 
									expand(children.get(0));
							}

							@Override
							protected Component newContentComponent(String id, final IModel<PathInfo> model) {
								PathInfo pathInfo = model.getObject();
								Fragment fragment = new Fragment(id, "treeNodeFrag", PathNavigator.this);
								
								WebMarkupContainer icon = new WebMarkupContainer("icon");
								String iconClass;
								if (FileMode.TREE.equals(pathInfo.getMode()))
									iconClass = "fa fa-folder-o";
								else if (FileMode.GITLINK.equals(pathInfo.getMode())) 
									iconClass = "fa fa-ext fa-submodule-o";
								else if (FileMode.SYMLINK.equals(pathInfo.getMode())) 
									iconClass = "fa fa-ext fa-symbol-link";
								else  
									iconClass = "fa fa-file-text-o";
								
								icon.add(AttributeModifier.append("class", iconClass));
								fragment.add(icon);
								
								AjaxLink<Void> link = new AjaxLink<Void>("link") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										selectPath(target, model.getObject().getPath());
										hide(target);
									}
									
								};
								if (pathInfo.getPath().indexOf('/') != -1)
									link.add(new Label("label", StringUtils.substringAfterLast(pathInfo.getPath(), "/")));
								else
									link.add(new Label("label", pathInfo.getPath()));
								fragment.add(link);
								
								return fragment;
							}
							
						};		
					}
					
				};
				item.add(subtreeDropdown);
				subtreeDropdownTrigger.add(new DropdownBehavior(subtreeDropdown)/*.mode(new DropdownMode.Hover())*/);
				item.add(subtreeDropdownTrigger);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(PathNavigator.class, "path-navigator.css")));
	}

	private ObjectId getCommitId() {
		return Preconditions.checkNotNull(repoModel.getObject().resolveRevision(revModel.getObject()));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		revModel.detach();
		
		super.onDetach();
	}
	
	private void selectPath(AjaxRequestTarget target, String path) {
		pathSegments.clear();
		pathSegments.addAll(Splitter.on('/').omitEmptyStrings().splitToList(path));
		pathSegments.add(0, repoModel.getObject().getName());
		target.add(PathNavigator.this);
		
		onSelect(target, path);
	}
	
	private List<PathInfo> getChildren(PathInfo pathInfo) {
		org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
		try {
			RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
			TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, pathInfo.getPath(), revTree);
			treeWalk.setRecursive(false);
			treeWalk.enterSubtree();
			
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
	
	protected abstract void onSelect(AjaxRequestTarget target, String path);

	private static class PathInfo implements Serializable {
		
		private final String path;
		
		private final int mode;
		
		public PathInfo(String path, int mode) {
			this.path = path;
			this.mode = mode;
		}

		public String getPath() {
			return path;
		}

		public int getMode() {
			return mode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PathInfo) 
				return path.equals(((PathInfo)obj).getPath());
			else 
				return false;
		}

		@Override
		public int hashCode() {
			return path.hashCode();
		}
		
	}
}
