package com.pmease.gitplex.web.page.repository.file;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.pmease.commons.git.BlobIdent;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.personlink.PersonLink;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
class LastCommitPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final BlobIdent blob;

	private final IModel<RevCommit> commitModel = new LoadableDetachableModel<RevCommit>() {

		@Override
		protected RevCommit load() {
			Git git = Git.wrap(repoModel.getObject().openAsJGitRepo());
			try {
				LogCommand log = git.log();
				log.setMaxCount(1);
				if (blob.path != null)
					log.addPath(blob.path);
				log.add(repoModel.getObject().getObjectId(blob.revision, true));
				return log.call().iterator().next();
			} catch (MissingObjectException | IncorrectObjectTypeException | GitAPIException e) {
				throw new RuntimeException(e);
			} finally {
				git.close();
			}
		}
		
	};
	
	public LastCommitPanel(String id, IModel<Repository> repoModel, BlobIdent blob) {
		super(id);
		
		this.repoModel = repoModel;
		this.blob = blob;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PersonLink("author", new AbstractReadOnlyModel<PersonIdent>() {

			@Override
			public PersonIdent getObject() {
				return commitModel.getObject().getAuthorIdent();
			}
			
		}));
		
		add(new Label("date", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return DateUtils.formatAge(commitModel.getObject().getAuthorIdent().getWhen());
			}
			
		}));
		
		add(new CommitMessagePanel("message", repoModel, commitModel));
		
		add(new TextField<String>("hash", Model.of(commitModel.getObject().name())));
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		commitModel.detach();
		
		super.onDetach();
	}

}
