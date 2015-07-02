package com.pmease.gitplex.web.component.savechange;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.commons.git.BlobIdent;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class SaveChangePanel extends Panel {

	private IModel<Repository> repoModel;
	
	private ObjectId parentCommitId;
	
	private String summaryCommitMessage;
	
	private String detailCommitMessage;
	
	private String defaultCommitMessage;
	
	public SaveChangePanel(String id, IModel<Repository> repoModel, BlobIdent blobIdent, 
			ObjectId parentCommitId, @Nullable byte[] content) {
		super(id);
	
		this.repoModel = repoModel;
		this.parentCommitId = parentCommitId;
		
		if (content != null) {
			try (	FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo();
					RevWalk revWalk = new RevWalk(jgitRepo)) {
				RevTree revTree = revWalk.parseCommit(parentCommitId).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, blobIdent.path, revTree);
				if (treeWalk != null)
					defaultCommitMessage = "Change " + blobIdent.getName();
				else
					defaultCommitMessage = "Add " + blobIdent.getName();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			defaultCommitMessage = "Delete " + blobIdent.getName();
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				try (	FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo();
						ObjectInserter inserter = jgitRepo.newObjectInserter();) {

				}
			}
			
		};
		add(form);
		
		form.add(new TextField<String>("summaryCommitMessage", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return summaryCommitMessage;
			}

			@Override
			public void setObject(String object) {
				summaryCommitMessage = object;
			}
			
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("placeholder", defaultCommitMessage);
			}
			
		});
		
		form.add(new TextArea<String>("detailCommitMessage", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return detailCommitMessage;
			}

			@Override
			public void setObject(String object) {
				detailCommitMessage = object;
			}
			
		}));
		
		form.add(new AjaxSubmitLink("save") {
		});

		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(SaveChangePanel.class, "save-change.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(SaveChangePanel.class, "save-change.css")));
		response.render(OnDomReadyHeaderItem.forScript(String.format("gitplex.saveChange.init('%s');", getMarkupId())));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
