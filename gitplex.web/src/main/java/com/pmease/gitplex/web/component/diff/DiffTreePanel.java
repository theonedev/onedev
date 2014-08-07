package com.pmease.gitplex.web.component.diff;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.DiffTreeNode;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class DiffTreePanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String fromRev;
	
	private final String toRev;
	
	public DiffTreePanel(String id, IModel<Repository> repoModel, String fromRev, String toRev) {
		super(id);
		
		this.repoModel = repoModel;
		this.fromRev = fromRev;
		this.toRev = toRev;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new NestedTree<DiffTreeNode>("tree", new ITreeProvider<DiffTreeNode>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends DiffTreeNode> getRoots() {
				return repoModel.getObject().git().listTreeWithDiff(fromRev, toRev, null).iterator();
			}

			@Override
			public boolean hasChildren(DiffTreeNode node) {
				return node.isFolder();
			}

			@Override
			public Iterator<? extends DiffTreeNode> getChildren(DiffTreeNode node) {
				return repoModel.getObject().git().listTreeWithDiff(fromRev, toRev, node.getPath() + "/").iterator();
			}

			@Override
			public IModel<DiffTreeNode> model(DiffTreeNode object) {
				return Model.of(object);
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new HumanTheme());				
			}

			@Override
			protected Component newContentComponent(String id, IModel<DiffTreeNode> nodeModel) {
				final DiffTreeNode node = nodeModel.getObject();
				Fragment fragment = new Fragment(id, "nodeFrag", DiffTreePanel.this);
				fragment.add(new WebMarkupContainer("addedDir").setVisible(
						node.isFolder() && node.getStatus() == DiffTreeNode.Status.ADD));
				fragment.add(new WebMarkupContainer("deletedDir").setVisible(
						node.isFolder() && node.getStatus() == DiffTreeNode.Status.DELETE));
				fragment.add(new WebMarkupContainer("modifiedDir").setVisible(
						node.isFolder() && node.getStatus() == DiffTreeNode.Status.MODIFY));
				fragment.add(new WebMarkupContainer("unchangedDir").setVisible(
						node.isFolder() && node.getStatus() == DiffTreeNode.Status.UNCHANGE));
				fragment.add(new WebMarkupContainer("addedFile").setVisible(
						!node.isFolder() && node.getStatus() == DiffTreeNode.Status.ADD));
				fragment.add(new WebMarkupContainer("deletedFile").setVisible(
						!node.isFolder() && node.getStatus() == DiffTreeNode.Status.DELETE));
				fragment.add(new WebMarkupContainer("modifiedFile").setVisible(
						!node.isFolder() && node.getStatus() == DiffTreeNode.Status.MODIFY));
				fragment.add(new WebMarkupContainer("unchangedFile").setVisible(
						!node.isFolder() && node.getStatus() == DiffTreeNode.Status.UNCHANGE));

				WebMarkupContainer link;
				if (node.isFolder()) {
					link = new AjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (getState(node) == State.EXPANDED)
								collapse(node);
							else
								expand(node);
						}
						
					};
				} else {
					link = newFileLink("link", node);
				}
				fragment.add(link);
				link.add(new Label("label", node.getName()));
				
				return fragment;
			}
			
		});
	}
	
	protected abstract Link<Void> newFileLink(String id, DiffTreeNode node);

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
