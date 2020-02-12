package io.onedev.server.web.component.blob.folderpicker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;

@SuppressWarnings("serial")
public abstract class BlobFolderPicker extends Panel {

	private final ObjectId commitId;
	
	public BlobFolderPicker(String id, @Nullable ObjectId commitId) {
		super(id);
		
		this.commitId = commitId;
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
				if (commitId != null)
					return getChildren(new BlobIdent(commitId.name(), null, FileMode.TYPE_TREE));
				else
					return new ArrayList<BlobIdent>().iterator();
			}

			@Override
			public boolean hasChildren(BlobIdent node) {
				return node.isTree();
			}

			@Override
			public Iterator<? extends BlobIdent> getChildren(BlobIdent node) {
				return getProject().getChildren(node, BlobIdentFilter.TREE, commitId).iterator();
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
				
				List<BlobIdent> children = getProject().getChildren(blobIdent, BlobIdentFilter.TREE, commitId);
				if (children.size() == 1 && children.get(0).isTree()) 
					expand(children.get(0));
				
				onStateChange();
			}
			
			@Override
			protected Component newContentComponent(String id, IModel<BlobIdent> node) {
				BlobIdent blobIdent = node.getObject();
				Fragment fragment = new Fragment(id, "contentFrag", BlobFolderPicker.this);
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, blobIdent);
					}
					
				};
				link.add(new Label("label", blobIdent.getName()));
				fragment.add(link);
				return fragment;
			}
			
		});			

	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BlobFolderPickerResourceReference()));
	}
	
	protected void onStateChange() {
		
	}
	
	protected abstract Project getProject();
	
	protected abstract Set<BlobIdent> getState();

	protected abstract void onSelect(AjaxRequestTarget target, BlobIdent blobIdent);
	
}
