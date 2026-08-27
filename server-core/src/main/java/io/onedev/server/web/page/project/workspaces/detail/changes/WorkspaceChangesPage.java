package io.onedev.server.web.page.project.workspaces.detail.changes;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.security.SecurityUtils;
import io.onedev.agent.workspace.FileData;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.DisableGlobalAjaxIndicatorListener;
import io.onedev.server.web.component.diff.text.PlainTextDiffPanel;
import io.onedev.server.web.component.fileview.FileViewPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.page.project.workspaces.detail.WorkspaceDetailPage;
import io.onedev.server.web.page.project.workspaces.detail.log.WorkspaceLogPage;
import io.onedev.agent.workspace.GitExecutionResult;

public class WorkspaceChangesPage extends WorkspaceDetailPage {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_FILE = "file";

	private static final String PARAM_REPOSITORY = "repository";

	/**
	 * Matches viewports on which the diff pane is hidden by css and diffs are shown in a
	 * dropdown instead. Keep in sync with workspace-changes.css.
	 */
	private static final String NARROW_MEDIA_QUERY = "(max-width: 991.98px)";

	private final Map<String, String> commitMessages = new HashMap<>();

	private String selectedRepository = "";

	private String selectedFile;

	private boolean selectedStaged;

	private boolean selectedConflicted;

	private WebMarkupContainer sidebar;

	private WebMarkupContainer diffContent;

	private WebMarkupContainer noSelection;

	private final IModel<List<RepositoryInfo>> repositoriesModel = new LoadableDetachableModel<>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<RepositoryInfo> load() {
			if (getWorkspace().getStatus() != Workspace.Status.ACTIVE) {
				return List.of(new RepositoryInfo("", StatusInfo.failure(
						_T("Please reprovision the workspace to show changes"))));
			}
			List<RepositoryInfo> repositories = new ArrayList<>();
			repositories.add(new RepositoryInfo("", loadStatus("")));
			for (String path : getSubmodulePaths())
				repositories.add(new RepositoryInfo(path, loadStatus(path)));
			return repositories;
		}

	};

	public WorkspaceChangesPage(PageParameters params) {
		super(params);
		if (getWorkspace().getStatus() == Workspace.Status.PENDING)
			throw new RestartResponseException(WorkspaceLogPage.class, params);

		selectedFile = params.get(PARAM_FILE).toString();
		String repository = params.get(PARAM_REPOSITORY).toString();
		if (repository != null)
			selectedRepository = repository;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		sidebar = new WebMarkupContainer("changesSidebar");
		sidebar.setOutputMarkupId(true);
		add(sidebar);

		addRepositorySections(sidebar);

		diffContent = new WebMarkupContainer("diffContent");
		diffContent.setOutputMarkupPlaceholderTag(true);
		diffContent.setVisible(false);
		add(diffContent);

		diffContent.add(new Label("diffFileName", ""));
		diffContent.add(new WebMarkupContainer("diffPanel"));

		noSelection = new WebMarkupContainer("noSelection");
		noSelection.setOutputMarkupPlaceholderTag(true);
		add(noSelection);

		if (selectedFile != null) {
			RepositoryInfo repository = findSelectedRepository();
			FileEntry found = findSelectedFileEntry(repository);
			if (found == null)
				throw new ExplicitException("Not a changed file: " + selectedFile);
			selectedStaged = found.staged;
			selectedConflicted = found.conflicted;
			showDiffContent(repository, found);
		}
	}

	private void addRepositorySections(WebMarkupContainer container) {
		List<RepositoryInfo> repositories = repositoriesModel.getObject();
		container.add(new ListView<>("repositorySections", repositories) {

			@Override
			protected void populateItem(ListItem<RepositoryInfo> item) {
				if (repositories.size() == 1)
					item.add(AttributeAppender.append("class", " flex-grow-1 min-height-0"));
				RepositoryInfo repository = item.getModelObject();
				Label title = new Label("repositoryTitle",
						repository.path.isEmpty()
								? _T("Main Repository")
								: _T("Submodule") + ": " + repository.path);
				title.setVisible(repositories.size() > 1);
				item.add(title);
				addSyncInfo(item, repository);
				addCommitForm(item, repository);
				addFileSections(item, repository);
			}

		});
	}

	private void addSyncInfo(WebMarkupContainer container, RepositoryInfo repository) {
		int aheadCount = getAhead(repository);

		container.add(new Label("aheadCount", String.valueOf(aheadCount)));

		AjaxLink<Void> pullLink = new AjaxLink<Void>("pull") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new AttachAjaxIndicatorListener(false));
				attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				GitExecutionResult result = executeGitIn(repository.path, "pull");
				if (result.getReturnCode() == 0) {
					Session.get().success(_T("Pull successful"));
				} else {
					Session.get().error(getErrorMessage(result));
				}
				refreshAll(target);
			}
			
		};

		if (getWorkspace().getStatus() != Workspace.Status.ACTIVE || !SecurityUtils.canModifyOrDelete(getWorkspace())) {
			pullLink.add(AttributeAppender.append("class", "disabled"));
			pullLink.setEnabled(false);
		}
		container.add(pullLink);

		AjaxLink<Void> syncLink = new AjaxLink<Void>("sync") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new AttachAjaxIndicatorListener(false));
				attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (!repository.path.isEmpty() || getWorkspace().getProject().getDefaultBranch() != null) {
					var result = executeGitIn(repository.path, "pull");
					if (result.getReturnCode() != 0) {
						Session.get().error(getErrorMessage(result));
						refreshAll(target);
						return;
					}
				}
				if (getAhead(repository) > 0) {
					var result = executeGitIn(repository.path, "push");
					if (result.getReturnCode() != 0) {
						Session.get().error(getErrorMessage(result));
						refreshAll(target);
						return;
					}
				}

				Session.get().success(_T("Sync successful"));
				refreshAll(target);
			}

		};
		if (getWorkspace().getStatus() != Workspace.Status.ACTIVE || !SecurityUtils.canModifyOrDelete(getWorkspace())) {
			syncLink.add(AttributeAppender.append("class", "disabled"));
			syncLink.setEnabled(false);
		}
		container.add(syncLink);

		AjaxLink<Void> pushLink = new AjaxLink<Void>("push") {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new AttachAjaxIndicatorListener(false));
				attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				GitExecutionResult result = executeGitIn(repository.path, "push");
				if (result.getReturnCode() == 0) {
					Session.get().success(_T("Push successful"));
				} else {
					Session.get().error(getErrorMessage(result));
					executeGitIn(repository.path, "fetch");
				}
				refreshAll(target);
			}

		};
		pushLink.add(new AjaxIndicatorAppender());
		if (aheadCount == 0 || getWorkspace().getStatus() != Workspace.Status.ACTIVE || !SecurityUtils.canModifyOrDelete(getWorkspace())) {
			pushLink.add(AttributeAppender.append("class", "disabled"));
			pushLink.setEnabled(false);
		}
		container.add(pushLink);
	}

	private void addCommitForm(WebMarkupContainer container, RepositoryInfo repository) {
		Form<?> form = new Form<>("commitForm");
		container.add(form);

		form.add(new TextArea<>("commitMessage", new IModel<String>() {

			@Override
			public String getObject() {
				return commitMessages.get(repository.path);
			}

			@Override
			public void setObject(String object) {
				commitMessages.put(repository.path, object);
			}

			@Override
			public void detach() {
			}

		}));

		AjaxButton commitButton = new AjaxButton("commitButton", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				doCommit(target, repository, false);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				refreshAll(target);
			}
		};
		form.add(commitButton);

		form.add(new WebMarkupContainer("dropdownToggle"));

		AjaxButton amendButton = new AjaxButton("amendButton", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				doCommit(target, repository, true);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				refreshAll(target);
			}
		};
		form.add(amendButton);

		boolean conflicts = hasConflicts(repository);
		boolean mergeInProgress = isMergeInProgress(repository);

		WebMarkupContainer mergeHint = new WebMarkupContainer("mergeHint");
		mergeHint.setVisible(mergeInProgress && !conflicts);
		mergeHint.add(new AjaxLink<Void>("abortMerge") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to abort the merge?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				executeGitIn(repository.path, "merge", "--abort");
				Session.get().success(_T("Merge aborted"));
				refreshAll(target);
			}
						
		});
		form.add(mergeHint);

		form.setVisible((hasStagedFiles(repository) || mergeInProgress)
				&& !conflicts 
				&& SecurityUtils.canModifyOrDelete(getWorkspace()));
	}

	private void doCommit(AjaxRequestTarget target, RepositoryInfo repository, boolean amend) {
		String commitMessage = commitMessages.get(repository.path);
		if (commitMessage == null || commitMessage.trim().isEmpty()) {
			Session.get().error(_T("Please enter a commit message"));
			refreshAll(target);
			return;
		}
		List<String> args = new ArrayList<>();
		args.add("commit");
		if (amend)
			args.add("--amend");
		args.add("-m");
		args.add(commitMessage.trim());
		GitExecutionResult result = executeGitIn(repository.path, args.toArray(new String[0]));
		if (result.getReturnCode() == 0) {
			Session.get().success(_T("Changes committed successfully"));
			commitMessages.remove(repository.path);
		} else {
			Session.get().error(getErrorMessage(result));
		}
		refreshAll(target);
	}

	private void addFileSections(WebMarkupContainer container, RepositoryInfo repository) {
		StatusInfo status = repository.status;

		Label statusError = new Label("statusError", status.errorMessage != null ? status.errorMessage : "");
		statusError.setVisible(status.errorMessage != null);
		container.add(statusError);

		List<FileEntry> allEntries = status.entries;

		container.add(new WebMarkupContainer("noChangedFiles")
				.setVisible(status.errorMessage == null && allEntries.isEmpty()));

		List<FileEntry> conflictEntries = new ArrayList<>();
		List<FileEntry> stagedEntries = new ArrayList<>();
		List<FileEntry> unstagedEntries = new ArrayList<>();
		for (FileEntry entry : allEntries) {
			if (isConflicted(entry)) {
				conflictEntries.add(new FileEntry(entry.path, entry.indexStatus, entry.workTreeStatus));
			} else {
				if (entry.indexStatus != ' ' && entry.indexStatus != '?')
					stagedEntries.add(new FileEntry(entry.indexStatus, entry.path, true));
				if (entry.workTreeStatus != ' ' || entry.indexStatus == '?')
					unstagedEntries.add(new FileEntry(
							entry.indexStatus == '?' ? 'U' : entry.workTreeStatus,
							entry.path, false, isSubmodule(repository, entry.path)));
			}
		}

		addConflictSection(container, repository, conflictEntries);

		WebMarkupContainer stagedSection = new WebMarkupContainer("stagedSection");
		stagedSection.setVisible(!stagedEntries.isEmpty());
		container.add(stagedSection);

		stagedSection.add(new Label("stagedCount", String.valueOf(stagedEntries.size())));
		stagedSection.add(new AjaxLink<Void>("unstageAll") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				executeGitIn(repository.path, "reset", "HEAD", "--", ".");
				refreshAll(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModifyOrDelete(getWorkspace()));
			}

		});

		stagedSection.add(new ListView<>("stagedFiles", stagedEntries) {
			@Override
			protected void populateItem(ListItem<FileEntry> item) {
				FileEntry entry = item.getModelObject();
				populateFileItem(item, repository, entry);
			}
		});

		WebMarkupContainer unstagedSection = new WebMarkupContainer("unstagedSection");
		unstagedSection.setVisible(!unstagedEntries.isEmpty());
		container.add(unstagedSection);

		unstagedSection.add(new Label("unstagedCount", String.valueOf(unstagedEntries.size())));
		unstagedSection.add(new AjaxLink<Void>("discardAll") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to discard all unstaged changes? This cannot be undone.")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				executeGitIn(repository.path, "checkout", "--", ".");
				executeGitIn(repository.path, "clean", "-fd");
				refreshAll(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(unstagedEntries.stream().anyMatch(it -> !it.submodule)
						&& SecurityUtils.canModifyOrDelete(getWorkspace()));
			}

		});
		unstagedSection.add(new AjaxLink<Void>("stageAll") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				executeGitIn(repository.path, "add", "-A");
				refreshAll(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModifyOrDelete(getWorkspace()));
			}

		});

		unstagedSection.add(new ListView<>("unstagedFiles", unstagedEntries) {
			@Override
			protected void populateItem(ListItem<FileEntry> item) {
				FileEntry entry = item.getModelObject();
				populateFileItem(item, repository, entry);
			}
		});
	}

	private void addConflictSection(WebMarkupContainer container, RepositoryInfo repository,
			List<FileEntry> conflictEntries) {
		WebMarkupContainer conflictSection = new WebMarkupContainer("conflictSection");
		conflictSection.setVisible(!conflictEntries.isEmpty());
		container.add(conflictSection);

		conflictSection.add(new Label("conflictCount", String.valueOf(conflictEntries.size())));
		conflictSection.add(new AjaxLink<Void>("abortMerge") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				executeGitIn(repository.path, "merge", "--abort");
				Session.get().success(_T("Merge aborted"));
				refreshAll(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModifyOrDelete(getWorkspace()));
			}

		});

		conflictSection.add(new ListView<>("conflictFiles", conflictEntries) {
			@Override
			protected void populateItem(ListItem<FileEntry> item) {
				populateConflictFileItem(item, repository, item.getModelObject());
			}
		});
	}

	private void populateConflictFileItem(ListItem<FileEntry> item, RepositoryInfo repository,
			FileEntry entry) {
		item.setOutputMarkupId(true);
		if (isSelected(repository, entry) && selectedConflicted) {
			item.add(new AttributeAppender("class", " active"));
		}

		DropdownLink fileLink = newFileLink(item, repository, entry);
		item.add(fileLink);

		String fileName = entry.path;
		String dirName = null;
		int lastSlash = fileName.lastIndexOf('/');
		if (lastSlash >= 0) {
			dirName = fileName.substring(0, lastSlash);
			fileName = fileName.substring(lastSlash + 1);
		}
		fileLink.add(new Label("fileName", fileName)
				.add(new AttributeModifier("title", entry.path)));
		Label fileDirLabel = new Label("fileDir", dirName != null ? dirName : "");
		fileDirLabel.setVisible(dirName != null);
		fileLink.add(fileDirLabel);

		Label conflictTypeLabel = new Label("conflictType",
				"" + entry.indexStatus + entry.workTreeStatus);
		conflictTypeLabel.add(new AttributeModifier("title",
				getConflictDescription(entry.indexStatus, entry.workTreeStatus)));
		fileLink.add(conflictTypeLabel);

		item.add(new AjaxLink<Void>("markResolved") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				GitExecutionResult result = executeGitIn(repository.path, "add", "-A", "--", entry.path);
				if (result.getReturnCode() != 0)
					executeGitIn(repository.path, "rm", "--cached", "--", entry.path);
				refreshSidebar(target);
				if (isSelected(repository, entry))
					refreshDiff(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModifyOrDelete(getWorkspace()));
			}

		});
	}

	private void populateFileItem(ListItem<FileEntry> item, RepositoryInfo repository, FileEntry entry) {
		item.setOutputMarkupId(true);
		if (isSelected(repository, entry)
				&& selectedStaged == entry.staged && !selectedConflicted) {
			item.add(new AttributeAppender("class", " active"));
		}

		DropdownLink fileLink = newFileLink(item, repository, entry);
		item.add(fileLink);

		Label statusLabel = new Label("status", String.valueOf(entry.displayStatus));
		statusLabel.add(new org.apache.wicket.behavior.AttributeAppender(
				"class", " status-" + entry.displayStatus));
		fileLink.add(statusLabel);

		String fileName = entry.path;
		String dirName = null;
		int lastSlash = fileName.lastIndexOf('/');
		if (lastSlash >= 0) {
			dirName = fileName.substring(0, lastSlash);
			fileName = fileName.substring(lastSlash + 1);
		}
		fileLink.add(new Label("fileName", fileName)
				.add(new org.apache.wicket.AttributeModifier("title", entry.path)));
		Label fileDirLabel = new Label("fileDir", dirName != null ? dirName : "");
		fileDirLabel.setVisible(dirName != null);
		fileLink.add(fileDirLabel);

		if (entry.staged) {
			item.add(new AjaxLink<Void>("unstage") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					executeGitIn(repository.path, "reset", "HEAD", "--", entry.path);
					refreshSidebar(target);
					if (isSelected(repository, entry))
						refreshDiff(target);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canModifyOrDelete(getWorkspace()));
				}
	
			});
		} else {
			item.add(new AjaxLink<Void>("discard") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to discard changes to this file? This cannot be undone.")));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					if (entry.displayStatus == 'U') {
						executeGitIn(repository.path, "clean", "-f", "--", entry.path);
					} else {
						executeGitIn(repository.path, "checkout", "--", entry.path);
					}
					refreshSidebar(target);
					if (isSelected(repository, entry))
						refreshDiff(target);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!entry.submodule
							&& SecurityUtils.canModifyOrDelete(getWorkspace()));
				}
	
			});
			item.add(new AjaxLink<Void>("stage") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					executeGitIn(repository.path, "add", "--", entry.path);
					refreshSidebar(target);
					if (isSelected(repository, entry))
						refreshDiff(target);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canModifyOrDelete(getWorkspace()));
				}
	
			});
		}
	}

	/**
	 * Clicking a file shows its diff in the side pane, except on narrow viewports where the
	 * pane is hidden by css and the diff is shown in a dropdown instead. Only the browser
	 * knows which of the two applies, hence the extra ajax parameter.
	 */
	private DropdownLink newFileLink(ListItem<FileEntry> item, RepositoryInfo repository, FileEntry entry) {
		return new DropdownLink("fileLink") {

			@Override
			protected void onInitialize(FloatingPanel dropdown) {
				super.onInitialize(dropdown);
				dropdown.add(AttributeAppender.append("class", "workspace-diff-dropdown"));
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getDynamicExtraParameters().add(
						"return {narrow: window.matchMedia(\"" + NARROW_MEDIA_QUERY + "\").matches};");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (isNarrow()) {
					super.onClick(target);
				} else {
					selectedRepository = repository.path;
					selectedFile = entry.path;
					selectedStaged = entry.staged;
					selectedConflicted = entry.conflicted;
					showDiffContent(repository, entry);
					target.add(diffContent);
					target.add(noSelection);
					pushState(target);
					target.appendJavaScript(
							"$('.file-list > li').removeClass('active');"
							+ "$('#" + item.getMarkupId() + "').addClass('active');");
				}
			}

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Fragment fragment = new Fragment(id, "diffDropdownFrag", WorkspaceChangesPage.this);
				fragment.add(new Label("diffFileName", getDiffTitle(entry)));
				fragment.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						dropdown.close();
					}

				});
				fragment.add(newDiffPanel("diffPanel", repository, entry));
				return fragment;
			}

		};
	}

	private boolean isNarrow() {
		return RequestCycle.get().getRequest().getRequestParameters()
				.getParameterValue("narrow").toBoolean(false);
	}

	private void refreshAll(AjaxRequestTarget target) {
		refreshSidebar(target);
		refreshDiff(target);
	}

	private void refreshSidebar(AjaxRequestTarget target) {
		WebMarkupContainer newSidebar = new WebMarkupContainer("changesSidebar");
		newSidebar.setOutputMarkupId(true);
		addRepositorySections(newSidebar);
		sidebar.replaceWith(newSidebar);
		sidebar = newSidebar;
		target.add(sidebar);
	}

	private void refreshDiff(AjaxRequestTarget target) {
		if (selectedFile != null) {
			RepositoryInfo repository = findSelectedRepository();
			FileEntry found = findSelectedFileEntry(repository);
			if (found != null) {
				selectedStaged = found.staged;
				selectedConflicted = found.conflicted;
				showDiffContent(repository, found);
			} else {
				selectedFile = null;
				selectedRepository = "";
				diffContent.setVisible(false);
				noSelection.setVisible(true);
				replaceState(target);
			}
		} else {
			diffContent.setVisible(false);
			noSelection.setVisible(true);
		}
		target.add(diffContent);
		target.add(noSelection);
	}

	private void showDiffContent(RepositoryInfo repository, FileEntry entry) {
		diffContent.setVisible(true);
		noSelection.setVisible(false);

		diffContent.replace(new Label("diffFileName", getDiffTitle(entry)));
		diffContent.replace(newDiffPanel("diffPanel", repository, entry));
	}

	private String getDiffTitle(FileEntry entry) {
		if (entry.conflicted) {
			return entry.path + " \u2014 "
					+ getConflictDescription(entry.indexStatus, entry.workTreeStatus);
		} else {
			return entry.path;
		}
	}

	private Component newDiffPanel(String id, RepositoryInfo repository, FileEntry entry) {
		if (entry.conflicted)
			return newConflictDiffPanel(id, repository, entry);
		else
			return newRegularDiffPanel(id, repository, entry);
	}

	private Component newMessagePanel(String id, String message) {
		return new Label(id, message).add(AttributeModifier.replace("class",
				"d-flex flex-grow-1 align-items-center justify-content-center text-muted text-center"));
	}

	private Component newRegularDiffPanel(String id, RepositoryInfo repository, FileEntry entry) {
		if (entry.displayStatus == 'U') {
			FileData fileData = workspaceService.readFileData(
					getWorkspace(), workspacePath(repository.path, entry.path));
			if (fileData != null)
				return new FileViewPanel(id, Model.of(fileData));
			else
				return newMessagePanel(id, _T("File not found"));
		}

		String diffOutput = getDiffOutput(repository, entry);
		if (isBinaryDiff(diffOutput)) {
			return newMessagePanel(id, _T("Binary file"));
		} else {
			List<List<String>> parsed = parseDiffToOldNewLines(diffOutput);
			return new PlainTextDiffPanel(id, parsed.get(0), parsed.get(1), true, entry.path);
		}
	}

	private Component newConflictDiffPanel(String id, RepositoryInfo repository, FileEntry entry) {
		if (entry.indexStatus == 'D' && entry.workTreeStatus == 'D') {
			return newMessagePanel(id, _T("Both sides deleted this file") + ". "
					+ _T("Mark as resolved to confirm the deletion, or restore the file in terminal."));
		}

		var fileData = workspaceService.readFileData(
				getWorkspace(), workspacePath(repository.path, entry.path));
		if (fileData != null) {
			return new FileViewPanel(id, Model.of(fileData));
		} else {
			return newMessagePanel(id, getConflictDescription(entry.indexStatus, entry.workTreeStatus)
					+ ". " + _T("Unable to read file from the working directory. Please resolve this conflict in the terminal and then mark as resolved."));
		}
	}

	private static String getConflictDescription(char indexStatus, char workTreeStatus) {
		if (indexStatus == 'D' && workTreeStatus == 'D')
			return _T("Both deleted");
		if (indexStatus == 'A' && workTreeStatus == 'A')
			return _T("Both added");
		if (indexStatus == 'U' && workTreeStatus == 'U')
			return _T("Both modified");
		if (indexStatus == 'D' && workTreeStatus == 'U')
			return _T("Deleted by us, modified by them");
		if (indexStatus == 'U' && workTreeStatus == 'D')
			return _T("Modified by us, deleted by them");
		if (indexStatus == 'A' && workTreeStatus == 'U')
			return _T("Added by us");
		if (indexStatus == 'U' && workTreeStatus == 'A')
			return _T("Added by them");
		return _T("Conflict");
	}

	private String getDiffOutput(RepositoryInfo repository, FileEntry entry) {
		GitExecutionResult result;
		if (entry.staged) {
			result = executeGitIn(repository.path, "diff", "--cached", "-U999999", "--", entry.path);
		} else {
			result = executeGitIn(repository.path, "diff", "-U999999", "--", entry.path);
		}
		return stdoutString(result);
	}

	private boolean isBinaryDiff(String diffOutput) {
		for (String line : diffOutput.split("\n")) {
			if (line.startsWith("Binary files "))
				return true;
		}
		return false;
	}

	// --- Git helper methods ---

	private GitExecutionResult executeGit(String... args) {
		return workspaceService.executeGitCommand(getWorkspace(), args);
	}

	private GitExecutionResult executeGitIn(String repository, String... args) {
		if (repository.isEmpty())
			return executeGit(args);
		String[] contextualArgs = new String[args.length + 2];
		contextualArgs[0] = "-C";
		contextualArgs[1] = repository;
		System.arraycopy(args, 0, contextualArgs, 2, args.length);
		return executeGit(contextualArgs);
	}

	private StatusInfo loadStatus(String repository) {
		GitExecutionResult result = executeGitIn(repository,
				"status", "-b", "--porcelain", "--untracked-files=all", "--ignore-submodules=dirty");
		if (result.getReturnCode() != 0)
			return StatusInfo.failure(getErrorMessage(result));
		boolean mergeInProgress = executeGitIn(repository,
				"rev-parse", "-q", "--verify", "MERGE_HEAD").getReturnCode() == 0;
		StatusInfo info = parseStatusOutput(stdoutString(result), mergeInProgress);
		if (info.ahead == null) {
			// Branch has no upstream tracking (e.g. workspace was provisioned
			// from an empty server repository). Fall back to counting local
			// commits reachable from HEAD so commits made inside the workspace
			// are reflected by the ahead count and the push button is enabled.
			info = StatusInfo.success(info.entries, countLocalCommits(repository), mergeInProgress);
		}
		return info;
	}

	private List<String> getSubmodulePaths() {
		GitExecutionResult result = executeGit("submodule", "status", "--recursive");
		if (result.getReturnCode() != 0)
			return List.of();
		return parseSubmodulePaths(stdoutString(result));
	}

	static List<String> parseSubmodulePaths(String output) {
		List<String> paths = new ArrayList<>();
		for (String line : output.split("\n")) {
			// A leading '-' denotes a submodule which is defined but not checked out.
			// The commit hash occupies the next 40 characters, followed by a space.
			if (line.length() > 42 && line.charAt(0) != '-') {
				String pathAndDescription = line.substring(42);
				int descriptionStart = pathAndDescription.lastIndexOf(" (");
				String path = descriptionStart != -1
						? pathAndDescription.substring(0, descriptionStart)
						: pathAndDescription;
				if (!path.isEmpty())
					paths.add(path);
			}
		}
		return paths;
	}

	private String workspacePath(String repository, String path) {
		return repository.isEmpty() ? path : repository + "/" + path;
	}

	private boolean isSubmodule(RepositoryInfo repository, String path) {
		String workspacePath = workspacePath(repository.path, path);
		for (RepositoryInfo each : repositoriesModel.getObject()) {
			if (each.path.equals(workspacePath))
				return true;
		}
		return false;
	}

	private String stdoutString(GitExecutionResult result) {
		return new String(result.getStdout(), StandardCharsets.UTF_8);
	}

	private boolean indicatesLocalChangesOverwritten(String text) {
		return text.contains("Your local changes to the following files would be overwritten by merge")
				|| text.contains("The following untracked working tree files would be overwritten by merge");
	}

	private int collectErrorLines(String output, List<String> keyLines) {
		int conflictCount = 0;
		for (String line : output.split("\n")) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			if (line.startsWith("CONFLICT ")) {
				conflictCount++;
			} else if (line.startsWith("fatal:") || line.startsWith("error:")
					|| line.startsWith("nothing to commit")
					|| line.startsWith("nothing added to commit")
					|| line.contains("! [rejected]")) {
				if (line.startsWith("fatal:"))
					line = line.substring("fatal:".length()).trim();
				else if (line.startsWith("error:"))
					line = line.substring("error:".length()).trim();
				if (!line.isEmpty() && !keyLines.contains(line))
					keyLines.add(line);
			}
		}
		return conflictCount;
	}

	private String buildErrorMessage(List<String> errorLines, int conflictCount) {
		if (conflictCount > 0)
			errorLines.add(0, _T("Conflicts found when trying to merge") + " (" + conflictCount + " " + (conflictCount == 1 ? _T("file") : _T("files")) + ")");
		return String.join("\n", errorLines);
	}

	private String getErrorMessage(GitExecutionResult result) {
		String stderr = new String(result.getStderr(), StandardCharsets.UTF_8).trim();
		String stdout = new String(result.getStdout(), StandardCharsets.UTF_8).trim();

		if (indicatesLocalChangesOverwritten(stderr))
			return _T("Some local changes would be overwritten by merge");

		List<String> errorLines = new ArrayList<>();
		int conflictCount = collectErrorLines(stderr, errorLines);
		if (!errorLines.isEmpty() || conflictCount > 0)
			return buildErrorMessage(errorLines, conflictCount);

		if (indicatesLocalChangesOverwritten(stdout))
			return _T("Some local changes would be overwritten by merge");

		conflictCount = collectErrorLines(stdout, errorLines);
		if (!errorLines.isEmpty() || conflictCount > 0)
			return buildErrorMessage(errorLines, conflictCount);

		if (!stderr.isEmpty())
			return stderr;
		return _T("Git command failed with exit code") + " " + result.getReturnCode();
	}


	private static boolean isConflicted(FileEntry entry) {
		char x = entry.indexStatus, y = entry.workTreeStatus;
		return x == 'U' || y == 'U'
				|| (x == 'A' && y == 'A')
				|| (x == 'D' && y == 'D');
	}

	private boolean hasConflicts(RepositoryInfo repository) {
		for (FileEntry entry : repository.status.entries) {
			if (isConflicted(entry))
				return true;
		}
		return false;
	}

	private boolean isMergeInProgress(RepositoryInfo repository) {
		return repository.status.mergeInProgress;
	}

	private boolean hasStagedFiles(RepositoryInfo repository) {
		for (FileEntry entry : repository.status.entries) {
			if (entry.indexStatus != ' ' && entry.indexStatus != '?' && !isConflicted(entry))
				return true;
		}
		return false;
	}

	private int getAhead(RepositoryInfo repository) {
		Integer cached = repository.status.ahead;
		return cached != null ? cached : 0;
	}

	private int countLocalCommits(String repository) {
		GitExecutionResult result = executeGitIn(repository,
				"rev-list", "--count", "HEAD", "--not", "--remotes");
		if (result.getReturnCode() != 0)
			return 0;
		try {
			return Integer.parseInt(stdoutString(result).trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private StatusInfo parseStatusOutput(String output, boolean mergeInProgress) {
		List<FileEntry> entries = new ArrayList<>();
		Integer ahead = null;
		for (String line : output.split("\n")) {
			if (line.startsWith("## ")) {
				ahead = parseAheadFromBranchHeader(line);
				continue;
			}
			if (line.length() < 4)
				continue;
			char indexStatus = line.charAt(0);
			char workTreeStatus = line.charAt(1);
			String path = line.substring(3);
			if (path.startsWith("\"") && path.endsWith("\""))
				path = unquoteGitPath(path);
			entries.add(new FileEntry(indexStatus, workTreeStatus, path));
		}
		return StatusInfo.success(entries, ahead, mergeInProgress);
	}

	/**
	 * Parses the ahead count from the {@code ## branch...upstream [ahead N, behind M]}
	 * header produced by {@code git status -b --porcelain}. Returns {@code null}
	 * when the branch has no upstream (or for unborn / detached HEAD), in which
	 * case the caller falls back to {@link #countLocalCommits()}.
	 */
	private Integer parseAheadFromBranchHeader(String line) {
		String header = line.substring(3);
		if (header.indexOf("...") < 0)
			return null;
		int aheadIdx = header.indexOf("[ahead ");
		if (aheadIdx < 0)
			return 0;
		int start = aheadIdx + "[ahead ".length();
		int end = start;
		while (end < header.length() && Character.isDigit(header.charAt(end)))
			end++;
		try {
			return Integer.parseInt(header.substring(start, end));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private String unquoteGitPath(String quoted) {
		String inner = quoted.substring(1, quoted.length() - 1);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inner.length(); i++) {
			char c = inner.charAt(i);
			if (c == '\\' && i + 1 < inner.length()) {
				char next = inner.charAt(i + 1);
				switch (next) {
					case 'n': sb.append('\n'); i++; break;
					case 't': sb.append('\t'); i++; break;
					case '\\': sb.append('\\'); i++; break;
					case '"': sb.append('"'); i++; break;
					default:
						if (next >= '0' && next <= '7') {
							StringBuilder octal = new StringBuilder();
							for (int j = i + 1; j < Math.min(i + 4, inner.length()); j++) {
								char oc = inner.charAt(j);
								if (oc >= '0' && oc <= '7')
									octal.append(oc);
								else
									break;
							}
							sb.append((char) Integer.parseInt(octal.toString(), 8));
							i += octal.length();
						} else {
							sb.append(c);
						}
						break;
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Parse unified diff output into [oldLines, newLines] by extracting context
	 * and change lines from hunks.
	 */
	private List<List<String>> parseDiffToOldNewLines(String diffOutput) {
		List<String> oldLines = new ArrayList<>();
		List<String> newLines = new ArrayList<>();
		boolean inHunk = false;

		for (String line : diffOutput.split("\n", -1)) {
			if (line.startsWith("@@")) {
				inHunk = true;
				continue;
			}
			if (!inHunk)
				continue;
			if (line.startsWith("diff --git") || line.startsWith("index ")) {
				inHunk = false;
				continue;
			}
			if (line.startsWith("-")) {
				oldLines.add(line.length() > 1 ? line.substring(1) : "");
			} else if (line.startsWith("+")) {
				newLines.add(line.length() > 1 ? line.substring(1) : "");
			} else if (line.startsWith(" ") || line.isEmpty()) {
				String content = line.isEmpty() ? "" : line.substring(1);
				oldLines.add(content);
				newLines.add(content);
			} else if (line.startsWith("\\ No newline at end of file")) {
				// skip
			}
		}

		List<List<String>> result = new ArrayList<>();
		result.add(oldLines);
		result.add(newLines);
		return result;
	}

	@Override
	protected void onDetach() {
		repositoriesModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WorkspaceChangesCssResourceReference()));
		if (selectedFile != null) {
			response.render(OnLoadHeaderItem.forScript("""
					var active = document.querySelector('.file-sections .file-list > li.active');
					if (active) active.scrollIntoView({block: 'nearest'});
					"""));
		}
	}

	private RepositoryInfo findSelectedRepository() {
		for (RepositoryInfo repository : repositoriesModel.getObject()) {
			if (repository.path.equals(selectedRepository))
				return repository;
		}
		return null;
	}

	private FileEntry findSelectedFileEntry(RepositoryInfo repository) {
		if (repository == null)
			return null;
		for (FileEntry e : repository.status.entries) {
			if (e.path.equals(selectedFile)) {
				if (isConflicted(e))
					return new FileEntry(e.path, e.indexStatus, e.workTreeStatus);
				if (e.indexStatus != ' ' && e.indexStatus != '?')
					return new FileEntry(e.indexStatus, e.path, true);
				if (e.workTreeStatus != ' ' || e.indexStatus == '?')
					return new FileEntry(
							e.indexStatus == '?' ? 'U' : e.workTreeStatus,
							e.path, false);
				break;
			}
		}
		return null;
	}

	private boolean isSelected(RepositoryInfo repository, FileEntry entry) {
		return selectedFile != null
				&& selectedRepository.equals(repository.path)
				&& selectedFile.equals(entry.path);
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		if (data instanceof Selection selection) {
			selectedRepository = selection.repository;
			selectedFile = selection.file;
		} else if (data instanceof String file) {
			selectedRepository = "";
			selectedFile = file;
		} else {
			selectedRepository = "";
			selectedFile = null;
		}
		refreshAll(target);
	}

	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getWorkspace(), selectedRepository, selectedFile);
		CharSequence url = RequestCycle.get().urlFor(WorkspaceChangesPage.class, params);
		pushState(target, url.toString(), new Selection(selectedRepository, selectedFile));
	}

	private void replaceState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getWorkspace(), selectedRepository, selectedFile);
		CharSequence url = RequestCycle.get().urlFor(WorkspaceChangesPage.class, params);
		replaceState(target, url.toString(), selectedFile != null
				? new Selection(selectedRepository, selectedFile) : null);
	}

	public static PageParameters paramsOf(Workspace workspace) {
		return WorkspaceDetailPage.paramsOf(workspace);
	}

	public static PageParameters paramsOf(Project project, Long workspaceNumber) {
		return WorkspaceDetailPage.paramsOf(project, workspaceNumber);
	}

	public static PageParameters paramsOf(Workspace workspace, String file) {
		return paramsOf(workspace, "", file);
	}

	private static PageParameters paramsOf(Workspace workspace, String repository, String file) {
		PageParameters params = WorkspaceDetailPage.paramsOf(workspace);
		if (!repository.isEmpty())
			params.add(PARAM_REPOSITORY, repository);
		if (file != null)
			params.add(PARAM_FILE, file);
		return params;
	}

	private static class Selection implements Serializable {
		private static final long serialVersionUID = 1L;

		final String repository;
		final String file;

		Selection(String repository, String file) {
			this.repository = repository;
			this.file = file;
		}
	}

	private static class RepositoryInfo implements Serializable {
		private static final long serialVersionUID = 1L;

		final String path;
		final StatusInfo status;

		RepositoryInfo(String path, StatusInfo status) {
			this.path = path;
			this.status = status;
		}
	}

	private static class FileEntry implements Serializable {
		private static final long serialVersionUID = 1L;

		final char indexStatus;
		final char workTreeStatus;
		final String path;
		final char displayStatus;
		final boolean staged;
		final boolean conflicted;
		final boolean submodule;

		FileEntry(char indexStatus, char workTreeStatus, String path) {
			this.indexStatus = indexStatus;
			this.workTreeStatus = workTreeStatus;
			this.path = path;
			this.displayStatus = ' ';
			this.staged = false;
			this.conflicted = false;
			this.submodule = false;
		}

		FileEntry(char displayStatus, String path, boolean staged) {
			this(displayStatus, path, staged, false);
		}

		FileEntry(char displayStatus, String path, boolean staged, boolean submodule) {
			this.indexStatus = ' ';
			this.workTreeStatus = ' ';
			this.path = path;
			this.displayStatus = displayStatus;
			this.staged = staged;
			this.conflicted = false;
			this.submodule = submodule;
		}

		FileEntry(String path, char indexStatus, char workTreeStatus) {
			this.indexStatus = indexStatus;
			this.workTreeStatus = workTreeStatus;
			this.path = path;
			this.displayStatus = 'C';
			this.staged = false;
			this.conflicted = true;
			this.submodule = false;
		}
	}

	private static class StatusInfo implements Serializable {
		private static final long serialVersionUID = 1L;

		final List<FileEntry> entries;
		final String errorMessage;
		// Number of commits to push. Parsed from the branch header when the
		// branch has an upstream; otherwise filled in with the count of local
		// commits reachable from HEAD. null only when status loading failed.
		final Integer ahead;
		final boolean mergeInProgress;

		private StatusInfo(List<FileEntry> entries, String errorMessage,
				Integer ahead, boolean mergeInProgress) {
			this.entries = entries;
			this.errorMessage = errorMessage;
			this.ahead = ahead;
			this.mergeInProgress = mergeInProgress;
		}

		static StatusInfo success(List<FileEntry> entries, Integer ahead, boolean mergeInProgress) {
			return new StatusInfo(entries, null, ahead, mergeInProgress);
		}

		static StatusInfo failure(String errorMessage) {
			return new StatusInfo(new ArrayList<>(), errorMessage, null, false);
		}
	}

}
