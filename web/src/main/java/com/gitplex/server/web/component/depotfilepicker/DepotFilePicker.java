package com.gitplex.server.web.component.depotfilepicker;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.BlobIdentFilter;
import com.gitplex.server.model.Depot;
import com.gitplex.server.web.component.BlobIcon;
import com.gitplex.server.web.component.link.ViewStateAwareAjaxLink;

@SuppressWarnings("serial")
public abstract class DepotFilePicker extends GenericPanel<Depot> {

	private final String revision;
	
	public DepotFilePicker(String id, IModel<Depot> depotModel, String revision) {
		super(id, depotModel);
		this.revision = revision;
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
				return getChildren(new BlobIdent(revision, null, FileMode.TYPE_TREE));
			}

			@Override
			public boolean hasChildren(BlobIdent node) {
				return node.isTree();
			}

			@Override
			public Iterator<? extends BlobIdent> getChildren(BlobIdent node) {
				return getModelObject().getChildren(node, getBlobIdentFilter()).iterator();
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
			public void expand(BlobIdent blobIdent) {
				super.expand(blobIdent);
				
				List<BlobIdent> children = DepotFilePicker.this.getModelObject().getChildren(blobIdent, getBlobIdentFilter());
				if (children.size() == 1 && children.get(0).isTree()) 
					expand(children.get(0));
			}
			
			@Override
			protected Component newContentComponent(String id, IModel<BlobIdent> node) {
				BlobIdent blobIdent = node.getObject();
				if (blobIdent.isTree()) {
					Fragment fragment = new Fragment(id, "folderFrag", DepotFilePicker.this);
					fragment.add(new BlobIcon("icon", node));
					fragment.add(new Label("label", blobIdent.getName()));
					return fragment;
				} else {
					Fragment fragment = new Fragment(id, "fileFrag", DepotFilePicker.this);
					AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onSelect(target, blobIdent);
						}
						
					};
					link.add(new BlobIcon("icon", node));
					link.add(new Label("label", blobIdent.getName()));
					fragment.add(link);
					return fragment;
				}
			}
			
		});			

	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DepotFilePickerResourceReference()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, BlobIdent blobIdent);
	
	protected BlobIdentFilter getBlobIdentFilter() {
		return BlobIdentFilter.ALL;
	}

}
