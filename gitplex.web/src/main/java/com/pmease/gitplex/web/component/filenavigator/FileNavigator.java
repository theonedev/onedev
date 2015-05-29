package com.pmease.gitplex.web.component.filenavigator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.pmease.commons.git.GitPath;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownMode;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class FileNavigator extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<String> revModel;
	
	private final GitPath file;
	
	public FileNavigator(String id, IModel<Repository> repoModel, IModel<String> revModel, @Nullable GitPath file) {
		super(id);

		this.repoModel = repoModel;
		this.revModel = revModel;
		this.file = file;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ListView<GitPath>("paths", new LoadableDetachableModel<List<GitPath>>() {

			@Override
			protected List<GitPath> load() {
				List<GitPath> paths = new ArrayList<>();
				paths.add(null);
				
				if (file != null) {
					List<String> segments = Splitter.on('/').omitEmptyStrings().splitToList(file.getName());
					
					for (int i=0; i<segments.size(); i++) { 
						GitPath parent = paths.get(paths.size()-1);
						int mode = (i==segments.size()-1?file.getMode():FileMode.TREE.getBits());
						if (parent != null)
							paths.add(new GitPath(parent.getName() + "/" + segments.get(i), mode));
						else
							paths.add(new GitPath(segments.get(i), mode));
					}
				}
				
				return paths;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<GitPath> item) {
				final GitPath path = item.getModelObject();
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, path);
					}
					
				};
				link.setEnabled(item.getIndex() != getViewSize()-1);
				
				if (path != null) {
					if (path.getName().indexOf('/') != -1)
						link.add(new Label("label", StringUtils.substringAfterLast(path.getName(), "/")));
					else
						link.add(new Label("label", path.getName()));
				} else {
					link.add(new Label("label", repoModel.getObject().getName()));
				}
				
				item.add(link);
				
				WebMarkupContainer subtreeDropdownTrigger = new WebMarkupContainer("subtreeDropdownTrigger");
				
				if (path != null && item.getIndex() == size()-1) {
					org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
					try {
						RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, path.getName(), revTree);
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
						return new NestedTree<GitPath>(id, new ITreeProvider<GitPath>() {

							@Override
							public void detach() {
							}

							@Override
							public Iterator<? extends GitPath> getRoots() {
								org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
								try {
									RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
									TreeWalk treeWalk;
									if (path != null) {
										treeWalk = Preconditions.checkNotNull(TreeWalk.forPath(jgitRepo, path.getName(), revTree));
										treeWalk.enterSubtree();
									} else {
										treeWalk = new TreeWalk(jgitRepo);
										treeWalk.addTree(revTree);
									}
									treeWalk.setRecursive(false);
									
									List<GitPath> roots = new ArrayList<>();
									while (treeWalk.next()) 
										roots.add(new GitPath(treeWalk.getPathString(), treeWalk.getRawMode(0)));
									Collections.sort(roots);
									return roots.iterator();
								} catch (IOException e) {
									throw new RuntimeException(e);
								} finally {
									jgitRepo.close();
								}
							}

							@Override
							public boolean hasChildren(GitPath path) {
								return FileMode.TREE.equals(path.getMode());
							}

							@Override
							public Iterator<? extends GitPath> getChildren(GitPath path) {
								return FileNavigator.this.getChildren(path).iterator();
							}

							@Override
							public IModel<GitPath> model(GitPath path) {
								return Model.of(path);
							}
							
						}) {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(new HumanTheme());				
							}

							@Override
							public void expand(GitPath pathInfo) {
								super.expand(pathInfo);
								
								List<GitPath> children = getChildren(pathInfo);
								if (children.size() == 1 && FileMode.TREE.equals(children.get(0).getMode())) 
									expand(children.get(0));
							}

							@Override
							protected Component newContentComponent(String id, final IModel<GitPath> model) {
								GitPath pathInfo = model.getObject();
								Fragment fragment = new Fragment(id, "treeNodeFrag", FileNavigator.this);
								
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
										onSelect(target, model.getObject());
										hide(target);
									}
									
								};
								if (pathInfo.getName().indexOf('/') != -1)
									link.add(new Label("label", StringUtils.substringAfterLast(pathInfo.getName(), "/")));
								else
									link.add(new Label("label", pathInfo.getName()));
								fragment.add(link);
								
								return fragment;
							}
							
						};		
					}
					
				};
				item.add(subtreeDropdown);
				subtreeDropdownTrigger.add(new DropdownBehavior(subtreeDropdown).mode(new DropdownMode.Hover()));
				item.add(subtreeDropdownTrigger);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(FileNavigator.class, "file-navigator.css")));
	}

	private ObjectId getCommitId() {
		return Preconditions.checkNotNull(repoModel.getObject().resolveRevision(revModel.getObject()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, GitPath file);
	
	private List<GitPath> getChildren(GitPath pathInfo) {
		org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
		try {
			RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
			TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, pathInfo.getName(), revTree);
			treeWalk.setRecursive(false);
			treeWalk.enterSubtree();
			
			List<GitPath> children = new ArrayList<>();
			while (treeWalk.next()) 
				children.add(new GitPath(treeWalk.getPathString(), treeWalk.getRawMode(0)));
			Collections.sort(children);
			return children;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			jgitRepo.close();
		}
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		revModel.detach();
		
		super.onDetach();
	}
	
}
