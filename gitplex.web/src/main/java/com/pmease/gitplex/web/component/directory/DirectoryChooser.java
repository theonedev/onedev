package com.pmease.gitplex.web.component.directory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
import com.pmease.gitplex.core.model.Branch;

@SuppressWarnings("serial")
public abstract class DirectoryChooser extends Panel {

	public DirectoryChooser(String id, IModel<Branch> branchModel) {
		super(id, branchModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DefaultNestedTree<TreeNode>("tree", new ITreeProvider<TreeNode>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends TreeNode> getRoots() {
				if (getBranch() != null) {
					Git git = getBranch().getRepository().git();
					List<TreeNode> roots = git.listTree(getBranch().getName(), null, false);
					if (roots != null) {
						for (Iterator<TreeNode> it = roots.iterator(); it.hasNext();) {
							if (it.next().getMode() != FileMode.TREE)
								it.remove();
						}
						return roots.iterator();
					} else {
						return new ArrayList<TreeNode>().iterator();
					}
				} else {
					return new ArrayList<TreeNode>().iterator();
				}
			}

			@Override
			public boolean hasChildren(TreeNode node) {
				return true;
			}

			@Override
			public Iterator<? extends TreeNode> getChildren(TreeNode node) {
				List<TreeNode> children = node.listChildren();
				if (children != null) {
					for (Iterator<TreeNode> it = children.iterator(); it.hasNext();) {
						if (it.next().getMode() != FileMode.TREE)
							it.remove();
					}
					return children.iterator();
				} else {
					return new ArrayList<TreeNode>().iterator();
				}
			}

			@Override
			public IModel<TreeNode> model(TreeNode object) {
				return Model.of(object);
			}
			
		}) {

			@Override
			protected Component newContentComponent(String id, IModel<TreeNode> node) {
				return new Folder<TreeNode>(id, this, node) {

					@Override
					protected IModel<?> newLabelModel(IModel<TreeNode> model) {
						return Model.of(model.getObject().getName());
					}

					@Override
					protected MarkupContainer newLinkComponent(String id, IModel<TreeNode> model) {
						return DirectoryChooser.this.newLinkComponent(id, model);
					}
					
				};
			}
			
		});
	}

	public Branch getBranch() {
		return (Branch) getDefaultModelObject();
	}

	protected abstract MarkupContainer newLinkComponent(String id, IModel<TreeNode> node);
}
