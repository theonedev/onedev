package com.pmease.gitplex.web.page.repository.code.tree;

import java.util.List;
import java.util.Set;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.ImmutableSet;
import com.pmease.commons.git.TreeNode;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.wiki.WikiTextPanel;
import com.pmease.gitplex.web.page.repository.code.blob.language.Language;
import com.pmease.gitplex.web.service.FileBlob;

@SuppressWarnings("serial")
public class ReadmePanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<List<TreeNode>> nodesModel;
	
	private final IModel<FileBlob> blobModel;
	
	public ReadmePanel(String id, IModel<Repository> repoModel, final String revision, IModel<List<TreeNode>> nodesModel) {
		
		super(id);
	
		this.repoModel = repoModel;
		this.nodesModel = nodesModel;
		
		this.blobModel = new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				TreeNode node = getReadmeNode();
				if (node == null) {
					return null;
				}
				
				return FileBlob.of(ReadmePanel.this.repoModel.getObject(), revision, node.getPath());
			}
		};
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		FileBlob blob = blobModel.getObject();
		this.setVisibilityAllowed(
				blob != null 
				&& blob.isText() 
				&& !blob.isEmpty() 
				&& !blob.isLarge());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FileBlob blob = blobModel.getObject();
		if (blob == null) {
			return;
		}
		
		add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return blobModel.getObject().getName();
			}
		}));
		
		Language lang = blob.getLanguage();
		if (lang != null && lang.getName().equals("Markdown")) {
			add(new WikiTextPanel("readme", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					FileBlob blob = blobModel.getObject();
					if (blob == null) {
						return "";
					}
					
					return blob.getStringContent(); 
				}
			}));
		} else {
			Fragment frag = new Fragment("readme", "rawfrag", this);
			frag.add(new Label("text", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					return blobModel.getObject().getStringContent();
				}
				
			}));
			
			add(frag);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}
	
	static Set<String> README_FILES = ImmutableSet.of(
			"README",
			"README.md",
			"README.markdown",
			"README.txt"
			);
	
	private TreeNode getReadmeNode() {
		List<TreeNode> nodes = nodesModel.getObject();
		for (TreeNode each : nodes) {
			if (README_FILES.contains(each.getName())) {
				return each;
			}
		}
		
		return null;
	}
	
	@Override
	public void onDetach() {
		repoModel.detach();
		nodesModel.detach();
		blobModel.detach();
		
		super.onDetach();
	}
}
