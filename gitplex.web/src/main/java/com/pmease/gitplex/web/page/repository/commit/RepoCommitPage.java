package com.pmease.gitplex.web.page.repository.commit;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.ajaxlistener.IndicateLoadingListener;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.component.diff.revision.option.DiffOptionPanel;
import com.pmease.gitplex.web.component.personlink.PersonLink;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
public class RepoCommitPage extends RepositoryPage {

	private static final String PARAM_REVISION = "revision";
	
	// make sure to use a different value from wicket:id according to wicket bug:
	// https://issues.apache.org/jira/browse/WICKET-6069
	private static final String PARAM_COMPARE_WITH = "compareWith";
	
	private static final String PARAM_PATH = "path";
	
	protected String revision;
	
	private HistoryState state;
	
	private IModel<RevCommit> commitModel = new LoadableDetachableModel<RevCommit>() {

		@Override
		protected RevCommit load() {
			try(	FileRepository jgitRepo = getRepository().openAsJGitRepo();
					RevWalk revWalk = new RevWalk(jgitRepo);) {
				ObjectId objectId;
				if (GitUtils.isHash(revision))
					objectId = ObjectId.fromString(revision);
				else
					objectId = jgitRepo.resolve(revision);
				return revWalk.parseCommit(objectId);
			} catch (RevisionSyntaxException | IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	};
	
	private DiffOptionPanel diffOption;
	
	private RevisionDiffPanel compareResult;
	
	public RepoCommitPage(PageParameters params) {
		super(params);
		
		revision = GitUtils.normalizePath(params.get(PARAM_REVISION).toString());
		state = new HistoryState(params);
	}

	private RevCommit getCommit() {
		return commitModel.getObject();
	}
	
	private List<String> getParents() {
		List<String> parents = new ArrayList<>();
		for (RevCommit parent: getCommit().getParents())
			parents.add(parent.name());
		return parents;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", GitUtils.getShortMessage(getCommit())));
		add(new CommitHashPanel("hash", Model.of(getCommit().name())));
		
		RepoFileState state = new RepoFileState();
		state.blobIdent.revision = getCommit().name();
		add(new BookmarkablePageLink<Void>("code", RepoFilePage.class, 
				RepoFilePage.paramsOf(repoModel.getObject(), state)));

		String detailMessage = GitUtils.getDetailMessage(getCommit());
		if (detailMessage != null)
			add(new Label("detail", detailMessage));
		else
			add(new WebMarkupContainer("detail").setVisible(false));
		
		add(new ListView<String>("refs", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return new ArrayList<>();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String ref = item.getModelObject();
				String branch = GitUtils.ref2branch(ref); 
				if (branch != null) {
					RepoFileState state = new RepoFileState();
					state.blobIdent.revision = branch;
					Link<Void> link = new BookmarkablePageLink<Void>("link", RepoFilePage.class, 
							RepoFilePage.paramsOf(repoModel.getObject(), state));
					link.add(new Label("label", branch));
					link.add(AttributeAppender.append("class", "branch"));
					item.add(link);
				} else {
					String tag = Preconditions.checkNotNull(GitUtils.ref2tag(ref));
					RepoFileState state = new RepoFileState();
					state.blobIdent.revision = tag;
					Link<Void> link = new BookmarkablePageLink<Void>("link", RepoFilePage.class, 
							RepoFilePage.paramsOf(repoModel.getObject(), state));
					link.add(new Label("label", tag));
					link.add(AttributeAppender.append("class", "tag"));
					item.add(link);
				}
			}
			
		});
		add(new PersonLink("author", Model.of(getCommit().getAuthorIdent())));
		add(new Label("age", DateUtils.formatAge(getCommit().getAuthorIdent().getWhen())));
		
		add(new ListView<String>("parents", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return getParents();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String parent = item.getModelObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", RepoCommitPage.class, 
						paramsOf(repoModel.getObject(), parent));
				link.add(new Label("label", GitUtils.abbreviateSHA(parent)));
				item.add(link);
			}
			
		});
		
		if (getCommit().getParentCount() > 1) {
			add(new DropDownChoice<String>("parentChoice", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return getCompareWith();
				}

				@Override
				public void setObject(String object) {
					setCompareWith(object);
				}
				
			}, getParents(), new IChoiceRenderer<String>() {

				@Override
				public Object getDisplayValue(String object) {
					return GitUtils.abbreviateSHA(object);
				}

				@Override
				public String getIdValue(String object, int index) {
					return object;
				}
				
			}).add(new OnChangeAjaxBehavior() {
	
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
				}
	
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					newCompareResult(target);
					pushState(target);
				}
	
			}));
		} else {
			add(new WebMarkupContainer("parentChoice").setVisible(false));
		}
		
		if (getCommit().getParentCount() != 0) {
			add(diffOption = new DiffOptionPanel("diffOption", repoModel, getCommit().name()) {
	
				@Override
				protected void onSelectPath(AjaxRequestTarget target, String path) {
					RepoCommitPage.this.state.path = path;
					newCompareResult(target);
					pushState(target);
				}
	
				@Override
				protected void onLineProcessorChange(AjaxRequestTarget target) {
					newCompareResult(target);
				}
	
				@Override
				protected void onDiffModeChange(AjaxRequestTarget target) {
					newCompareResult(target);
				}
	
			});
			newCompareResult(null);
		} else {
			add(new WebMarkupContainer("diffOption").setVisible(false));
			add(new WebMarkupContainer("compareResult").setVisible(false));
		}
	}
	
	private String getCompareWith() {
		if (state.compareWith == null)
			state.compareWith = getParents().get(0);
		return state.compareWith;
	}
	
	private void setCompareWith(String compareWith) {
		state.compareWith = compareWith;
	}
	
	private void newCompareResult(@Nullable AjaxRequestTarget target) {
		compareResult = new RevisionDiffPanel("compareResult", repoModel,  
				Model.of((PullRequest)null), Model.of((Comment)null), getCompareWith(), 
				getCommit().name(), state.path, state.path, diffOption.getLineProcessor(), 
				diffOption.getDiffMode()) {

			@Override
			protected void onClearPath(AjaxRequestTarget target) {
				state.path = null;
				newCompareResult(target);
				pushState(target);
			}
			
		};
		compareResult.setOutputMarkupId(true);
		if (target != null) {
			replace(compareResult);
			target.add(compareResult);
		} else {
			add(compareResult);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				RepoCommitPage.class, "repo-commit.css")));
	}

	public static PageParameters paramsOf(Repository repository, String revision) {
		return paramsOf(repository, revision, new HistoryState());
	}
	
	public static PageParameters paramsOf(Repository repository, String revision, HistoryState state) {
		PageParameters params = paramsOf(repository);
		params.set(PARAM_REVISION, revision);
		if (state.compareWith != null)
			params.set(PARAM_COMPARE_WITH, state.compareWith);
		if (state.path != null)
			params.set(PARAM_PATH, state.path);
		return params;
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoCommitsPage.class, paramsOf(repository));
	}
	
	@Override
	protected void onDetach() {
		commitModel.detach();
		
		super.onDetach();
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getRepository(), getCommit().name(), state);
		CharSequence url = RequestCycle.get().urlFor(RepoCommitPage.class, params);
		pushState(target, url.toString(), state);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		state = (HistoryState) data;
		newCompareResult(target);
	}
	
	public static class HistoryState implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public String compareWith;
		
		public String path;
		
		public HistoryState() {
		}
		
		public HistoryState(PageParameters params) {
			compareWith = params.get(PARAM_COMPARE_WITH).toString();
			path = params.get(PARAM_PATH).toString();
		}
		
	}
}
