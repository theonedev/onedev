package com.pmease.gitop.web.page.project.source.commit;

import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.component.avatar.GitPersonAvatar;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.git.command.DiffTreeCommand;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.source.commit.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.patch.Patch;

@SuppressWarnings("serial")
public class SourceCommitPage extends ProjectCategoryPage {
	public static PageParameters newParams(Project project, String revision) {
		PageParameters params = PageSpec.forProject(project);
		params.add(PageSpec.OBJECT_ID, revision);
		return params;
	}
	
	private final IModel<Commit> commitModel;
	private final IModel<Patch> patchModel;
	
	private static final int DEFAULT_CONTEXT_LINES = 3;
	
	public SourceCommitPage(PageParameters params) {
		super(params);
		
		this.commitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				String revision = getRevision();
				Project project = getProject();
				return project.code().showRevision(revision);
			}
		};
		
		this.patchModel = new LoadableDetachableModel<Patch>() {

			@Override
			protected Patch load() {
				Commit commit = commitModel.getObject();

				String rev;
				if (commit.getParentHashes().isEmpty()) {
					// no parent, initial commit
					rev = getRevision();
				} else {
					rev = getRevision() + "^.." + getRevision();
				}
				
				Patch patch = new DiffTreeCommand(getProject().code().repoDir())
					.revision(rev)
					.contextLines(DEFAULT_CONTEXT_LINES)
					.findRenames(true)
					.call();
				return patch;
			}
		};
	}

	@Override
	protected void onUpdateRevision(String rev) {
		// don't update revision in session
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new Label("shortmessage", new PropertyModel<String>(commitModel, "subject")));
		add(new Label("detailedmessage", new PropertyModel<String>(commitModel, "message")));
		
		IModel<GitPerson> authorModel = new AbstractReadOnlyModel<GitPerson>() {

			@Override
			public GitPerson getObject() {
				return GitPerson.of(getCommit().getAuthor());
			}
		};
		
		add(new GitPersonAvatar("authoravatar", authorModel));

		add(new GitPersonLink("author", authorModel, Mode.NAME_ONLY));
		
		add(new AgeLabel("authorday", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommit().getAuthor().getDate();
			}
		}));
		
		add(new GitPersonLink("committer", new AbstractReadOnlyModel<GitPerson>() {

			@Override
			public GitPerson getObject() {
				Commit commit = getCommit();
				return GitPerson.of(commit.getCommitter());
			}
			
		}, Mode.NAME_ONLY) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisibilityAllowed(!Objects.equal(getCommit().getAuthor(), 
													getCommit().getCommitter()));
			}
		});
		
		add(new AgeLabel("committerday", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommit().getCommitter().getDate();
			}
			
		}));
		
		add(new Label("commitsha", new PropertyModel<String>(commitModel, "hash")));
		
		IModel<List<String>> parentsModel = new AbstractReadOnlyModel<List<String>>() {

			@Override
			public List<String> getObject() {
				return getCommit().getParentHashes();
			}
		};
		
		ListView<String> parentsView = new ListView<String>("parents", parentsModel) {
			@Override
			protected void populateItem(ListItem<String> item) {
				String sha = item.getModelObject();
				
				AbstractLink link = new BookmarkablePageLink<Void>("link", SourceCommitPage.class,
						SourceCommitPage.newParams(getProject(), sha));
				item.add(link);
				link.add(new Label("sha", GitUtils.abbreviateSHA(sha)));
				WebMarkupContainer connector = new WebMarkupContainer("connector");
				int idx = item.getIndex();
				connector.setVisibilityAllowed(idx > 0);
				item.add(connector);
			}
		};
		
		add(parentsView);
		
		createDiffToc();
		
		IModel<List<? extends FileHeader>> model = new LoadableDetachableModel<List<? extends FileHeader>>() {

			@Override
			protected List<? extends FileHeader> load() {
				List<? extends FileHeader> total = getDiffPatch().getFiles();
				if (total.size() > 300) {
					return Lists.newArrayList(total.subList(0, 300));
				} else {
					return total;
				}
			}
			
		};
		
		add(new ListView<FileHeader>("filelist", model) {

			@Override
			protected void populateItem(ListItem<FileHeader> item) {
				item.setMarkupId("diff-" + item.getIndex());
				item.add(new BlobDiffPanel("file", projectModel, item.getModel(), commitModel));
			}
			
		});
	}

	private void createDiffToc() {

		add(new Label("changes", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getDiffPatch().getFiles().size();
			}
			
		}));
		
		add(new Label("additions", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getDiffPatch().getDiffStat().getAdditions();
			}
			
		}));
		
		add(new Label("deletions", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getDiffPatch().getDiffStat().getDeletions();
			}
			
		}));
		
		add(new ListView<FileHeader>("files", new AbstractReadOnlyModel<List<? extends FileHeader>>() {

			@Override
			public List<? extends FileHeader> getObject() {
				return getDiffPatch().getFiles();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<FileHeader> item) {
				FileHeader file = item.getModelObject();
				ChangeType changeType = file.getChangeType();
				item.add(new Icon("icon", getChangeIcon(changeType)).add(AttributeModifier.replace("title", changeType.name())));
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(AttributeModifier.replace("href", "#diff-" + item.getIndex()));
				item.add(link);
				link.add(new Label("oldpath", Model.of(file.getOldPath())).setVisibilityAllowed(file.getChangeType() == ChangeType.RENAME));
				link.add(new Label("path", Model.of(file.getNewPath())));
				
				WebMarkupContainer statlink = new WebMarkupContainer("statlink");
				statlink.add(AttributeModifier.replace("href", "#diff-" + item.getIndex()));
				statlink.add(AttributeModifier.replace("title", file.getDiffStat().toString()));
				
				item.add(statlink);
				statlink.add(new Label("additions", Model.of(
						file.getDiffStat().getAdditions() > 0 ?
								"+" + file.getDiffStat().getAdditions() : "-")));
				statlink.add(new Label("deletions", Model.of(
						file.getDiffStat().getDeletions() > 0 ?
								"-" + file.getDiffStat().getDeletions() : "-")));
			}
		});
	}
	
	private static String getChangeIcon(ChangeType changeType) {
		switch (changeType) {
		case ADD:
			return "icon-diff-added";
			
		case MODIFY:
			return "icon-diff-modified";
			
		case DELETE:
			return "icon-diff-deleted";
			
		case RENAME:
			return "icon-diff-renamed";
			
		case COPY:
			return "icon-diff-copy";
		}
		
		throw new IllegalArgumentException("change type " + changeType);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript("$('#diff-toc .btn').click(function() { $('#diff-toc').toggleClass('open');})"));
	}
	
	protected Patch getDiffPatch() {
		return patchModel.getObject();
	}
	
	protected Commit getCommit() {
		return commitModel.getObject();
	}
	
	@Override
	protected boolean isRevisionAware() {
		return false;
	}
	
	@Override
	public void onDetach() {
		if (commitModel != null) {
			commitModel.detach();
		}

		if (patchModel != null) {
			patchModel.detach();
		}
		
		super.onDetach();
	}
	
	@Override
	protected String getPageTitle() {
		return getCommit().getSubject() + " - "
				+ GitUtils.abbreviateSHA(getRevision(), 5) 
				+ " - " + getProject().getPathName();
	}
}
