package com.pmease.gitplex.web.component.pathselector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.BlobIcon;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public abstract class PathSelector extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	private final Set<Integer> pathTypes;
	
	public PathSelector(String id, IModel<Repository> repoModel, String revision, int... pathTypes) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
		this.pathTypes = new HashSet<>();
		for (int i=0; i<pathTypes.length; i++)
			this.pathTypes.add(pathTypes[i]);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new NestedTree<BlobIdent>("tree", new ITreeProvider<BlobIdent>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends BlobIdent> getRoots() {
				try (	FileRepository repo = repoModel.getObject().openAsJGitRepo();
						RevWalk revWalk = new RevWalk(repo);
						TreeWalk treeWalk = new TreeWalk(repo)) {
					RevCommit commit = revWalk.parseCommit(repoModel.getObject().getObjectId(revision));
					treeWalk.addTree(commit.getTree());
					
					List<BlobIdent> roots = new ArrayList<>();
					while (treeWalk.next()) {
						int fileMode = treeWalk.getRawMode(0);
						int fileType = fileMode & FileMode.TYPE_MASK;
						if (fileType == FileMode.TYPE_TREE || pathTypes.contains(fileType))
							roots.add(new BlobIdent(revision, treeWalk.getPathString(), fileMode));
					}
					Collections.sort(roots);
					return roots.iterator();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean hasChildren(BlobIdent node) {
				return node.isTree();
			}

			@Override
			public Iterator<? extends BlobIdent> getChildren(BlobIdent node) {
				try (	FileRepository repo = repoModel.getObject().openAsJGitRepo();
						RevWalk revWalk = new RevWalk(repo);) {
					RevCommit commit = revWalk.parseCommit(repoModel.getObject().getObjectId(revision));
					TreeWalk treeWalk = TreeWalk.forPath(repo, node.path, commit.getTree());
					treeWalk.enterSubtree();
					List<BlobIdent> children = new ArrayList<>();
					while (treeWalk.next()) {
						int fileMode = treeWalk.getRawMode(0);
						int fileType = fileMode & FileMode.TYPE_MASK;
						if (fileType == FileMode.TYPE_TREE || pathTypes.contains(fileType))
							children.add(new BlobIdent(revision, treeWalk.getPathString(), fileMode));
					}
					Collections.sort(children);
					return children.iterator();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public IModel<BlobIdent> model(BlobIdent object) {
				return Model.of(object);
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new HumanTheme());				
			}

			@Override
			protected Component newContentComponent(String id, final IModel<BlobIdent> model) {
				BlobIdent blobIdent = model.getObject();
				Fragment fragment = new Fragment(id, "treeNodeFrag", PathSelector.this);
				fragment.add(new BlobIcon("icon", model));

				WebMarkupContainer link;
				
				if (pathTypes.contains(blobIdent.mode & FileMode.TYPE_MASK)) {
					link = new AjaxLink<Void>("link") {
	
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							PathSelector.this.updateAjaxAttributes(attributes);
						}

						@Override
						public void onClick(AjaxRequestTarget target) {
							onSelect(target, model.getObject());
						}
	
						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							
							PageParameters params = RepoFilePage.paramsOf(repoModel.getObject(), model.getObject());
							tag.put("href", urlFor(RepoFilePage.class, params));
						}
						
					};
				} else {
					link = new WebMarkupContainer("link");
					link.setRenderBodyOnly(true);
				}
				if (blobIdent.path.indexOf('/') != -1)
					link.add(new Label("label", StringUtils.substringAfterLast(blobIdent.path, "/")));
				else
					link.add(new Label("label", blobIdent.path));
				fragment.add(link);
				
				return fragment;
			}
			
		});
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(PathSelector.class, "path-selector.css")));
	}

	protected abstract void onSelect(AjaxRequestTarget target, BlobIdent blobIdent);
	
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
	}
	
}
