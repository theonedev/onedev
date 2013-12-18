package com.pmease.gitop.web.page.project.source.component;

import java.util.List;
import java.util.Set;

import org.apache.tika.mime.MimeType;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.ImmutableSet;
import com.pmease.commons.git.TreeNode;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.component.wiki.WikiTextPanel;
import com.pmease.gitop.web.component.wiki.WikiType;
import com.pmease.gitop.web.page.project.source.blob.FileBlob;
import com.pmease.gitop.web.page.project.source.blob.renderer.highlighter.HighlightJsResourceReference;

@SuppressWarnings("serial")
public class ReadmePanel extends AbstractSourcePagePanel {

	private final IModel<List<TreeNode>> nodesModel;
	private final IModel<FileBlob> blobModel;
	
	public ReadmePanel(String id,
						IModel<Project> project,
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
				
				return FileBlob.of(getProject(), getRevision(), node.getPath());
			}
		};
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		this.setVisibilityAllowed(getReadmeNode() != null);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getReadmeNode().getName();
			}
		}));
		
		add(new WikiTextPanel("readme", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				FileBlob blob = blobModel.getObject();
				if (blob == null) {
					return "";
				}
				
				return blob.getStringContent(); 
			}
		}, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				FileBlob blob = blobModel.getObject();
				MimeType mime = blob.getMimeType();
				if (mime.getType().toString().contains("markdown")) {
					return WikiType.MARKDOWN.getLanguage();
				} else {
					return null;
				}
			}
			
		}));
	}
	
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(HighlightJsResourceReference.getInstance()));
		response.render(OnDomReadyHeaderItem.forScript(
				("$('.readme .body pre code').each(function(i, e) { hljs.highlightBlock(e)});")));
	}
	
	static Set<String> README_FILES = ImmutableSet.of(
			"README",
			"README.md",
			"README.markdown"
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
