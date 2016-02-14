package com.pmease.gitplex.web.component.addtag;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class AddTagPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	private String tagName;
	
	private String tagMessage;
	
	public AddTagPanel(String id, IModel<Repository> repoModel, String revision) {
		super(id);
		this.repoModel = repoModel;
		this.revision = revision;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		form.add(new NotificationPanel("feedback", form));
		form.add(new TextField<String>("name", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return tagName;
			}

			@Override
			public void setObject(String object) {
				tagName = object;
			}
			
		}).setOutputMarkupId(true));
		
		form.add(new TextArea<String>("message", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return tagMessage;
			}

			@Override
			public void setObject(String object) {
				tagMessage = object;
			}
			
		}));
		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (tagName == null) {
					form.error("Tag name is required.");
					target.focusComponent(form.get("name"));
					target.add(form);
				} else {
					String tagRef = GitUtils.tag2ref(tagName);
					Repository repo = repoModel.getObject();
					if (repo.getObjectId(tagRef, false) != null) {
						form.error("Tag '" + tagName + "' already exists, please choose a different name.");
						target.add(form);
					} else {
						try (FileRepository jgitRepo = repo.openAsJGitRepo();) {
							Git git = Git.wrap(jgitRepo);
							TagCommand tag = git.tag();
							tag.setName(tagName);
							if (tagMessage != null)
								tag.setMessage(tagMessage);
							User user = GitPlex.getInstance(UserManager.class).getCurrent();
							tag.setTagger(user.asPerson());
							tag.setObjectId(repo.getRevCommit(revision));
							tag.call();
						} catch (GitAPIException e) {
							throw new RuntimeException(e);
						}
						onCreate(target);
					}
				}
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		add(form);
	}
	
	protected abstract void onCreate(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
