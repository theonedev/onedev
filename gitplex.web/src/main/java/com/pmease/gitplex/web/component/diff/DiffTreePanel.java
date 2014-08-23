package com.pmease.gitplex.web.component.diff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jersey.repackaged.com.google.common.base.Splitter;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.Change;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class DiffTreePanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<List<Change>> changesModel;
	
	private final String fromRev;
	
	private final String toRev;
	
	private NestedTree<DiffTreeNode> tree;
	
	public DiffTreePanel(String id, IModel<Repository> repoModel, IModel<List<Change>> changesModel, 
			String fromRev, String toRev) {
		super(id);
		
		this.repoModel = repoModel;
		this.changesModel = changesModel;
		this.fromRev = fromRev;
		this.toRev = toRev;
	}
	
	public void reveal(Change change) {
		String path = change.getPath();
		StringBuilder expandPath = new StringBuilder();
		for (String segment: Splitter.on('/').split(path)) {
			if (expandPath.length() != 0)
				expandPath.append('/');
			expandPath.append(segment);
			Change expandChange = new Change(Change.Status.UNCHANGED, expandPath.toString(), expandPath.toString(), 0, 0);
			tree.expand(new DiffTreeNode(expandChange));
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(tree = new NestedTree<DiffTreeNode>("tree", new ITreeProvider<DiffTreeNode>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends DiffTreeNode> getRoots() {
				List<DiffTreeNode> roots = new ArrayList<>();
				for (Change change: repoModel.getObject().git().listTree(fromRev, toRev, null, changesModel.getObject())) {
					roots.add(new DiffTreeNode(change));
				}
				return roots.iterator();
			}

			@Override
			public boolean hasChildren(DiffTreeNode node) {
				return node.getChange().isFolder();
			}

			@Override
			public Iterator<? extends DiffTreeNode> getChildren(DiffTreeNode node) {
				List<DiffTreeNode> children = new ArrayList<>();
				for (Change change: repoModel.getObject().git().listTree(
						fromRev, toRev, node.getChange().getPath(), changesModel.getObject())) {
					children.add(new DiffTreeNode(change));
				}
				return children.iterator();
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

				WebMarkupContainer link;
				if (node.getChange().isFolder()) {
					link = new AjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (getState(node) == State.EXPANDED)
								collapse(node);
							else
								expand(node);
						}
						
					};
					fragment.add(AttributeAppender.append("class", " folder "));
				} else {
					link = newBlobLink("link", node.getChange());
					fragment.add(AttributeAppender.append("class", " file "));
				}
				link.add(AttributeAppender.append("title", node.getChange().getHint()));
				
				fragment.add(link);
				fragment.add(AttributeAppender.append("class", node.getChange().getStatus().name().toLowerCase()));

				link.add(new Label("label", node.getChange().getName()));
				
				return fragment;
			}
			
		});
	}
	
	protected abstract WebMarkupContainer newBlobLink(String id, Change change);

	@Override
	protected void onDetach() {
		repoModel.detach();
		changesModel.detach();
		
		super.onDetach();
	}

}
