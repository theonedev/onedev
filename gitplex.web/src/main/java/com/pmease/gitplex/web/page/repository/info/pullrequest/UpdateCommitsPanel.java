package com.pmease.gitplex.web.page.repository.info.pullrequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.Commit;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Verification;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.git.GitUtils;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.info.code.commit.RepoCommitPage;
import com.pmease.gitplex.web.util.DateUtils;

/**
 * This panel displays commits of a request update.
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class UpdateCommitsPanel extends Panel {

	private final boolean showDate;
	
	private IModel<Set<String>> mergedCommitHashesModel = new LoadableDetachableModel<Set<String>>() {

		@Override
		protected Set<String> load() {
			Set<String> hashes = new HashSet<>();

			for (Commit commit: getUpdate().getMergedCommits())
				hashes.add(commit.getHash());
			return hashes;
		}
		
	};
	
	public UpdateCommitsPanel(String id, IModel<PullRequestUpdate> model, boolean showDate) {
		super(id, model);
		this.showDate = showDate;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Commit>("commits", new AbstractReadOnlyModel<List<Commit>>() {

			@Override
			public List<Commit> getObject() {
				return getUpdate().getCommits();
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<Commit> item) {
				Commit commit = item.getModelObject();
				
				item.add(new PersonLink("author", Model.of(commit.getAuthor()), AvatarMode.NAME_AND_AVATAR));

				item.add(new Label("message", commit.getSubject()));
				
				if (showDate)
					item.add(new Label("date", DateUtils.formatAge(commit.getAuthor().getWhen())));
				else
					item.add(new WebMarkupContainer("date"));
				
				RepositoryPage page = (RepositoryPage) getPage();
				AbstractLink link = new BookmarkablePageLink<Void>("shaLink",
						RepoCommitPage.class,
						RepoCommitPage.paramsOf(page.getRepository(), commit.getHash(), null));
				link.add(new Label("sha", GitUtils.abbreviateSHA(commit.getHash())));
				
				item.add(link);
				
				final List<Verification> verifications = new ArrayList<>();
				for (Verification verification: getUpdate().getRequest().getVerifications()) {
					if (verification.getCommit().equals(commit.getHash()))
						verifications.add(verification);
				}

				Verification.Status overallStatus = null;
				for (Verification verification: verifications) {
					if (verification.getStatus() == Verification.Status.NOT_PASSED) {
						overallStatus = Verification.Status.NOT_PASSED;
						break;
					} else if (verification.getStatus() == Verification.Status.ONGOING) {
						overallStatus = Verification.Status.ONGOING;
					} else if (overallStatus == null) {
						overallStatus = Verification.Status.PASSED;
					}
				}

				DropdownPanel verificationDropdownPanel = new DropdownPanel("verificationDetails", true) {

					@Override
					protected Component newContent(String id) {
						return new VerificationDetailPanel(id, new AbstractReadOnlyModel<List<Verification>>() {

							@Override
							public List<Verification> getObject() {
								return verifications;
							}
							
						});
					}
					
				};
				item.add(verificationDropdownPanel);
				if (overallStatus == Verification.Status.PASSED) {
					item.add(new Label("verification", "build passed <span class='fa fa-caret-down'/>")
						.setEscapeModelStrings(false)
						.add(AttributeAppender.append("class", "label label-success"))
						.add(new DropdownBehavior(verificationDropdownPanel)));
				} else if (overallStatus == Verification.Status.ONGOING) {
					item.add(new Label("verification", "build ongoing <span class='fa fa-caret-down'/>")
						.setEscapeModelStrings(false)
						.add(AttributeAppender.append("class", "label label-warning"))
						.add(new DropdownBehavior(verificationDropdownPanel)));
				} else if (overallStatus == Verification.Status.NOT_PASSED) {
					item.add(new Label("verification", "build not passed <span class='fa fa-caret-down'/>")
						.setEscapeModelStrings(false)
						.add(AttributeAppender.append("class", "label label-danger"))
						.add(new DropdownBehavior(verificationDropdownPanel)));
				} else {
					item.add(new WebMarkupContainer("verification"));
				}
				
				if (mergedCommitHashesModel.getObject().contains(commit.getHash())) {
					item.add(new Label("integration", "merged").add(AttributeAppender.append("class", "label label-success")));
					item.add(AttributeAppender.append("class", " merged"));
				} else if (getUpdate().getRequest().getPendingCommits().contains(commit.getHash())) {
					item.add(new WebMarkupContainer("integration"));
				} else {
					item.add(new Label("integration", "rebased").add(AttributeAppender.append("class", "label label-danger")));
					item.add(AttributeAppender.append("class", " rebased"));
				}
			}
			
		});
	}

	private PullRequestUpdate getUpdate() {
		return (PullRequestUpdate) getDefaultModelObject();
	}
	
	@Override
	protected void onDetach() {
		mergedCommitHashesModel.detach();
		
		super.onDetach();
	}
	
}
