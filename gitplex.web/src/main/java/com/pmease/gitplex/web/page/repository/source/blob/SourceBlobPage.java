package com.pmease.gitplex.web.page.repository.source.blob;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.EnclosureContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.wicket.behavior.CollapseBehavior;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.page.repository.source.AbstractFilePage;
import com.pmease.gitplex.web.page.repository.source.component.SourceBreadcrumbPanel;
import com.pmease.gitplex.web.service.FileBlob;
import com.pmease.gitplex.web.util.UrlUtils;

@SuppressWarnings("serial")
public class SourceBlobPage extends AbstractFilePage {

	private final IModel<Commit> lastCommitModel;
	private final IModel<List<PersonIdent>> committersModel;
	
	public SourceBlobPage(PageParameters params) {
		super(params);
		
		lastCommitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				Git git = getRepository().git();
				List<String> paths = getPaths();
				List<Commit> commits = git.log(null, getRevision(), Joiner.on("/").join(paths), 1, 0);
				Commit commit = Iterables.getFirst(commits, null);
				if (commit == null) {
					throw new EntityNotFoundException(
							"Path: " + getFilePath() +
							", revision: " + getRevision() + " doesn't exist"); 
				}
				return commit;
			}
		};
		
		committersModel = new LoadableDetachableModel<List<PersonIdent>>() {

			@Override
			protected List<PersonIdent> load() {
				Git git = getRepository().git();
				List<Commit> commits = git.log(null, getRevision(), getFilePath(), 0, 0);
				Set<PersonIdent> users = Sets.newHashSet();
				for (Commit each : commits) {
					users.add(each.getAuthor());
				}
				
				return Lists.newArrayList(users);
			}
			
		};
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new SourceBreadcrumbPanel("breadcrumb", repositoryModel, revisionModel, pathsModel));
		
		add(new Label("shortMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getLastCommit().getSubject();
			}
		}));
		
		Label detailedMsg = new Label("detailedMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getLastCommit().getMessage();
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (Objects.equal(getLastCommit().getSubject(), getLastCommit().getMessage())) {
					this.setVisibilityAllowed(false);
				}
			}
		};

		EnclosureContainer detailedContainer = new EnclosureContainer("detailedMessageContainer", detailedMsg);
		add(detailedContainer);
		detailedContainer.add(detailedMsg);
		
		WebMarkupContainer detailedToggle = new WebMarkupContainer("detailed-toggle");
		detailedToggle.add(new CollapseBehavior(detailedMsg));
		detailedContainer.add(detailedToggle);
		
		add(new PersonLink("author", Model.of(getLastCommit().getAuthor()), AvatarMode.NAME_AND_AVATAR));
		
		add(new AgeLabel("author-date", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getLastCommit().getAuthor().getWhen();
			}
			
		}));
		
		add(new AjaxLazyLoadPanel("paticipants") {

			@Override
			public Component getLazyLoadComponent(String markupId) {
				return new ContributorsPanel(markupId, committersModel);
			}
		});
		
		add(new SourceBlobPanel("source", new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				return FileBlob.of(getRepository(), getRevision(), getFilePath());
			}
		}));
	}

	protected String getFilePath() {
		return UrlUtils.concatSegments(getPaths());
	}
	
	@Override
	protected String getPageTitle() {
		return getFilePath() + " at " + getRevision() + " " + getRepository().getFullName();
	}
	
	protected Commit getLastCommit() {
		return lastCommitModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (lastCommitModel != null) {
			lastCommitModel.detach();
		}
		
		if (committersModel != null) {
			committersModel.detach();
		}
		
		super.onDetach();
	}
}
