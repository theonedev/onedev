package com.pmease.gitop.web.page.project.source.tree;

import java.util.List;
import java.util.Set;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.ImmutableSet;
import com.pmease.commons.git.TreeNode;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.component.wiki.WikiTextPanel;
import com.pmease.gitop.web.component.wiki.WikiType;
import com.pmease.gitop.web.page.project.source.blob.language.Language;
import com.pmease.gitop.web.page.project.source.component.AbstractSourcePagePanel;
import com.pmease.gitop.web.service.FileBlob;

@SuppressWarnings("serial")
public class ReadmePanel extends AbstractSourcePagePanel {

	private final IModel<List<TreeNode>> nodesModel;
	private final IModel<FileBlob> blobModel;
	
	public ReadmePanel(String id,
						IModel<Repository> project,
						IModel<String> revisionModel,
						IModel<List<String>> pathsModel,
						IModel<List<TreeNode>> nodesModel) {
		
		super(id, project, revisionModel, pathsModel);
		this.nodesModel = nodesModel;
		
		this.blobModel = new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				TreeNode node = getReadmeNode();
				if (node == null) {
					return null;
				}
				
				return FileBlob.of(getRepo(), getRevision(), node.getPath());
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
			}, Model.of(WikiType.MARKDOWN)));
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
		if (nodesModel != null) {
			nodesModel.detach();
		}
		
		if (blobModel != null) {
			blobModel.detach();
		}
		
		super.onDetach();
	}
}
