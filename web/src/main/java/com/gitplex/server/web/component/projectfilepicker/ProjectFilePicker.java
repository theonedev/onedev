package com.gitplex.server.web.component.projectfilepicker;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.BlobIdentFilter;
import com.gitplex.server.model.Project;
import com.gitplex.server.web.component.BlobIcon;
import com.gitplex.server.web.component.link.ViewStateAwareAjaxLink;

@SuppressWarnings("serial")
public abstract class ProjectFilePicker extends GenericPanel<Project> {

	private final String revision;
	
	private final ObjectId commitId;
	
	private final String initialDirectoryToOpen;
	
	public ProjectFilePicker(String id, IModel<Project> projectModel, String revision) {
		this(id, projectModel, revision, projectModel.getObject().getObjectId(revision), null);
	}

	public ProjectFilePicker(String id, IModel<Project> projectModel, String revision, ObjectId commitId, 
			@Nullable String initialDirectoryToOpen) {
		super(id, projectModel);
		this.revision = revision;
		this.commitId = commitId;
		this.initialDirectoryToOpen = initialDirectoryToOpen;
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
				return getModelObject().getChildren(node, getBlobIdentFilter(), commitId).iterator();
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
				
				if (initialDirectoryToOpen != null) {
					reveal(initialDirectoryToOpen);
				}
			}
			
			private void reveal(String directory) {
				expand(new BlobIdent(revision, directory, FileMode.TREE.getBits()));
				if (directory.contains("/")) {
					reveal(StringUtils.substringBeforeLast(directory, "/"));
				}
			}
			
			@Override
			public void expand(BlobIdent blobIdent) {
				super.expand(blobIdent);
				
				List<BlobIdent> children = ProjectFilePicker.this.getModelObject().getChildren(blobIdent, 
						getBlobIdentFilter(), commitId);
				if (children.size() == 1 && children.get(0).isTree()) 
					expand(children.get(0));
			}
			
			@Override
			protected Component newContentComponent(String id, IModel<BlobIdent> node) {
				BlobIdent blobIdent = node.getObject();
				if (blobIdent.isTree()) {
					Fragment fragment = new Fragment(id, "folderFrag", ProjectFilePicker.this);
					fragment.add(new BlobIcon("icon", node));
					fragment.add(new Label("label", blobIdent.getName()));
					return fragment;
				} else {
					Fragment fragment = new Fragment(id, "fileFrag", ProjectFilePicker.this);
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
		response.render(CssHeaderItem.forReference(new ProjectFilePickerResourceReference()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, BlobIdent blobIdent);
	
	protected BlobIdentFilter getBlobIdentFilter() {
		return BlobIdentFilter.ALL;
	}

}
