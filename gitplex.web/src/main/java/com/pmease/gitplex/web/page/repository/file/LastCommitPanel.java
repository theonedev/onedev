package com.pmease.gitplex.web.page.repository.file;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.personlink.PersonLink;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
class LastCommitPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final BlobIdent blob;

	private final IModel<Commit> commitModel = new LoadableDetachableModel<Commit>() {

		@Override
		protected Commit load() {
			// call git command line for performance reason
			return repoModel.getObject().git().log(null, blob.revision, blob.path, 1, 0).iterator().next();
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
				return commitModel.getObject().getAuthor();
			}
			
		}));
		
		add(new Label("date", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return DateUtils.formatAge(commitModel.getObject().getAuthor().getWhen());
			}
			
		}));
		
		add(new CommitMessagePanel("message", repoModel, commitModel));
		
		add(new TextField<String>("hash", Model.of(commitModel.getObject().getHash())));
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		commitModel.detach();
		
		super.onDetach();
	}

}
