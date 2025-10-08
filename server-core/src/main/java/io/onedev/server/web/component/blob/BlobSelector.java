package io.onedev.server.web.component.blob;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.model.Project;
import io.onedev.server.util.ChildrenAggregator;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.nested.BranchItem;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class BlobSelector extends Panel {

	private final IModel<Project> projectModel;
	
	private final ObjectId commitId;
	
	public BlobSelector(String id, IModel<Project> projectModel, @Nullable ObjectId commitId) {
		super(id);
		this.projectModel = projectModel;
		this.commitId = commitId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var childrenAggregator = new ChildrenAggregator<BlobIdent>() {

			@Override
			protected List<BlobIdent> getChildren(BlobIdent node) {
				return projectModel.getObject().getBlobChildren(node, getBlobIdentFilter(), commitId);
			}
		};
		add(new NestedTree<BlobIdent>("tree", new ITreeProvider<>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends BlobIdent> getRoots() {
				if (commitId != null)
					return childrenAggregator.getAggregatedChildren(new BlobIdent(commitId.name(), null, FileMode.TYPE_TREE)).iterator();
				else
					return new ArrayList<BlobIdent>().iterator();
			}

			@Override
			public boolean hasChildren(BlobIdent node) {
				return node.isTree();
			}

			@Override
			public Iterator<? extends BlobIdent> getChildren(BlobIdent node) {
				return childrenAggregator.getAggregatedChildren(node).iterator();
			}

			@Override
			public IModel<BlobIdent> model(BlobIdent object) {
				return Model.of(object);
			}

		}, new AbstractReadOnlyModel<Set<BlobIdent>>() {

			@Override
			public Set<BlobIdent> getObject() {
				return getState();
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new HumanTheme());
			}

			@Override
			public void collapse(BlobIdent blobIdent) {
				super.collapse(blobIdent);
				onStateChange();
			}

			@Override
			public void expand(BlobIdent blobIdent) {
				super.expand(blobIdent);

				List<BlobIdent> children = projectModel.getObject().getBlobChildren(
						blobIdent, getBlobIdentFilter(), commitId);
				if (children.size() == 1 && children.get(0).isTree())
					expand(children.get(0));

				onStateChange();
			}
			
			private IModel<String> newLabelModel(Fragment fragment, BlobIdent blobIdent) {
				return new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						var label = blobIdent.path;
						var branchItem = fragment.getParent().getParent().findParent(BranchItem.class);
						if (branchItem != null) {
							var parentBlobIdent = (BlobIdent) branchItem.getModelObject();
							label = label.substring(parentBlobIdent.path.length() + 1);
						}
						return label;
					}
				};
			}

			@Override
			protected Component newContentComponent(String id, IModel<BlobIdent> node) {
				BlobIdent blobIdent = node.getObject();
				
				Fragment fragment = new Fragment(id, "nodeFrag", BlobSelector.this);
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (blobIdent.isTree()) {
							if (getState(blobIdent) == State.EXPANDED)
								collapse(blobIdent);
							else
								expand(blobIdent);
						} else {
							onSelect(target, blobIdent.path);
						}
					}

				};
				link.add(new BlobIcon("icon", node));
				link.add(new Label("label", newLabelModel(fragment, blobIdent)));
				fragment.add(link);
				return fragment;
			}

		});			

	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BlobResourceReference()));
	}
	
	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	protected void onStateChange() {
		
	}
	
	protected abstract Set<BlobIdent> getState();

	protected abstract void onSelect(AjaxRequestTarget target, String blobPath);
	
	protected BlobIdentFilter getBlobIdentFilter() {
		return BlobIdentFilter.ALL;
	}

}
