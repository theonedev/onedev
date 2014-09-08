package com.pmease.gitplex.web.page.repository.info.pullrequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.git.Commit;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Verification;

@SuppressWarnings("serial")
public class CommitStatusPanel extends Panel {

	private final IModel<PullRequestUpdate> updateModel;
	
	private final String commitHash;
	
	private final IModel<Set<String>> mergedCommitsModel = new LoadableDetachableModel<Set<String>>() {

		@Override
		protected Set<String> load() {
			Set<String> hashes = new HashSet<>();

			for (Commit commit: updateModel.getObject().getMergedCommits())
				hashes.add(commit.getHash());
			return hashes;
		}
		
	};

	public CommitStatusPanel(String id, IModel<PullRequestUpdate> updateModel, String commitHash) {
		super(id);
		
		this.updateModel = updateModel;
		this.commitHash = commitHash;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final List<Verification> verifications = new ArrayList<>();
		for (Verification verification: updateModel.getObject().getRequest().getVerifications()) {
			if (verification.getCommit().equals(commitHash))
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

		DropdownPanel verificationDropdown = new DropdownPanel("verificationDetails", true) {

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
		add(verificationDropdown);
		if (overallStatus == Verification.Status.PASSED) {
			add(new Label("verification", "build passed <span class='fa fa-caret-down'/>")
				.setEscapeModelStrings(false)
				.add(AttributeAppender.append("class", "label label-success"))
				.add(new DropdownBehavior(verificationDropdown)));
		} else if (overallStatus == Verification.Status.ONGOING) {
			add(new Label("verification", "build ongoing <span class='fa fa-caret-down'/>")
				.setEscapeModelStrings(false)
				.add(AttributeAppender.append("class", "label label-warning"))
				.add(new DropdownBehavior(verificationDropdown)));
		} else if (overallStatus == Verification.Status.NOT_PASSED) {
			add(new Label("verification", "build failed <span class='fa fa-caret-down'/>")
				.setEscapeModelStrings(false)
				.add(AttributeAppender.append("class", "label label-danger"))
				.add(new DropdownBehavior(verificationDropdown)));
		} else {
			add(new WebMarkupContainer("verification"));
		}

		if (mergedCommitsModel.getObject().contains(commitHash)) {
			add(new Label("integration", "merged").add(AttributeAppender.append("class", "label label-success")));
		} else if (updateModel.getObject().getRequest().getPendingCommits().contains(commitHash)) {
			add(new WebMarkupContainer("integration"));
		} else {
			add(new Label("integration", "rebased").add(AttributeAppender.append("class", "label label-danger")));
		}
	}

	@Override
	protected void onDetach() {
		updateModel.detach();
		mergedCommitsModel.detach();
		super.onDetach();
	}

}
