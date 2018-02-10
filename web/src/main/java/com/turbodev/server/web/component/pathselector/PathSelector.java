package com.turbodev.server.web.component.pathselector;

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
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.turbodev.utils.StringUtils;
import com.turbodev.server.git.BlobIdent;
import com.turbodev.server.model.Project;
import com.turbodev.server.web.component.BlobIcon;
import com.turbodev.server.web.component.link.PreventDefaultAjaxLink;

@SuppressWarnings("serial")
public abstract class PathSelector extends Panel {

	private final IModel<Project> projectModel;
	
	private final String revision;
	
	private final Set<Integer> pathTypes;
	
	public PathSelector(String id, IModel<Project> projectModel, String revision, int... pathTypes) {
		super(id);
		
		this.projectModel = projectModel;
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
				Repository repository = projectModel.getObject().getRepository();				
				try (	RevWalk revWalk = new RevWalk(repository);
						TreeWalk treeWalk = new TreeWalk(repository)) {
					RevCommit commit = revWalk.parseCommit(projectModel.getObject().getObjectId(revision));
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
				Repository repository = projectModel.getObject().getRepository();				
				try (RevWalk revWalk = new RevWalk(repository)) {
					RevCommit commit = revWalk.parseCommit(projectModel.getObject().getObjectId(revision));
					TreeWalk treeWalk = TreeWalk.forPath(repository, node.path, commit.getTree());
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
					link = new PreventDefaultAjaxLink<Void>("link") {
	
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							PathSelector.this.updateAjaxAttributes(attributes);
						}

						@Override
						public void onClick(AjaxRequestTarget target) {
							onSelect(target, model.getObject());
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
		projectModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new PathSelectorResourceReference()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, BlobIdent blobIdent);
	
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
	}
	
}
