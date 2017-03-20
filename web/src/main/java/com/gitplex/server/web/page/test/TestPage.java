package com.gitplex.server.web.page.test;

import java.util.UUID;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.web.component.comment.CommentInput;
import com.gitplex.server.web.component.comment.DepotAttachmentSupport;
import com.gitplex.server.web.component.markdown.AttachmentSupport;
import com.gitplex.server.web.component.markdown.BlobReferenceSupport;
import com.gitplex.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private String comment;
	
	private static String ATTACHMENT_UUID = UUID.randomUUID().toString();
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.add(new CommentInput("comment", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return comment;
			}

			@Override
			public void setObject(String object) {
				comment = object;
			}
			
		}, false) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new DepotAttachmentSupport(GitPlex.getInstance(DepotManager.class).load(1L), ATTACHMENT_UUID);
			}

			@Override
			protected BlobReferenceSupport getBlobReferenceSupport() {
				return new BlobReferenceSupport() {
					
					@Override
					public Depot getDepot() {
						return GitPlex.getInstance(DepotManager.class).load(1L);
					}
					
					@Override
					public BlobIdent getBaseBlobIdent() {
						return new BlobIdent("master", "readme.md", FileMode.TYPE_FILE);
					}
				};
			}

			@Override
			protected Depot getDepot() {
				return GitPlex.getInstance(DepotManager.class).load(1L);
			}
			
		});
		add(form);
	}

}
