package io.onedev.server.web.component.commit.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.ProjectAndRevision;
import io.onedev.server.search.commit.CommitCriteria;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.search.commit.MessageCriteria;
import io.onedev.server.search.commit.PathCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Constants;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.behavior.CommitQueryBehavior;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.build.status.CommitStatusPanel;
import io.onedev.server.web.component.commit.graph.CommitGraphResourceReference;
import io.onedev.server.web.component.commit.graph.CommitGraphUtils;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.user.contributoravatars.ContributorAvatars;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.savedquery.SavedQueriesClosed;
import io.onedev.server.web.page.project.savedquery.SavedQueriesOpened;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.VisibleVisitor;

@SuppressWarnings("serial")
public class CommitListPanel extends Panel {

	private static final Logger logger = LoggerFactory.getLogger(CommitListPanel.class);
	
	private static final int COMMITS_PER_PAGE = 50;
	
	private static final int MAX_PAGES = 50;
	
	private final IModel<Project> projectModel;
	
	private final String query;
	
	private int currentPage = 1;
	
	private final IModel<CommitQuery> parsedQueryModel = new LoadableDetachableModel<CommitQuery>() {

		@Override
		protected CommitQuery load() {
			try {
				CommitQuery additionalQuery = CommitQuery.parse(getProject(), query);
				if (SecurityUtils.getUser() == null && additionalQuery.needsLogin()) { 
					error("Please login to perform this query");
				} else { 
					if (SecurityUtils.getUser() == null && getBaseQuery().needsLogin())
						error("Please login to show commits");
					else
						return CommitQuery.merge(getBaseQuery(), additionalQuery);
				}
			} catch (Exception e) {
				logger.error("Error parsing commit query: " + query, e);
				error(e.getMessage());
			}
			return null;
		}
		
	};
	
	private final IModel<Commits> commitsModel = new LoadableDetachableModel<Commits>() {

		private List<RevCommit> separateByDate(List<RevCommit> commits) {
			List<RevCommit> separated = new ArrayList<>();
			DateTime groupTime = null;
			for (RevCommit commit: commits) {
				DateTime commitTime = new DateTime(commit.getCommitterIdent().getWhen());
				if (groupTime == null || commitTime.getYear() != groupTime.getYear() 
						|| commitTime.getDayOfYear() != groupTime.getDayOfYear()) {
					groupTime = commitTime;
					separated.add(null);
				} 
				separated.add(commit);
			}
			return separated;
		}
		
		@Override
		protected Commits load() {
			CommitQuery query = parsedQueryModel.getObject();
			Commits commits = new Commits();
			List<String> commitHashes;
			if (query != null) {
				try {
					RevListCommand command = new RevListCommand(getProject().getGitDir());
					command.ignoreCase(true);
					
					if (currentPage > MAX_PAGES)
						throw new OneException("Page should be no more than " + MAX_PAGES);
					
					command.count(currentPage * COMMITS_PER_PAGE);
					
					query.fill(getProject(), command);
					
					if (command.revisions().isEmpty() && getCompareWith() != null)
						command.revisions(Lists.newArrayList(getCompareWith()));
					
					commitHashes = command.call();
				} catch (Exception e) {
					if (e.getMessage() != null)
						error(e.getMessage());
					else
						error("Error calculating commits: check log for details");
					commitHashes = new ArrayList<>();
					logger.error("Error calculating commits: ", e);
				}
			} else {
				commitHashes = new ArrayList<>();
			}				
			
			commits.hasMore = (commitHashes.size() == currentPage * COMMITS_PER_PAGE);
			
			try (RevWalk revWalk = new RevWalk(getProject().getRepository())) {
				int lastMaxCount = Math.min((currentPage - 1) * COMMITS_PER_PAGE, commitHashes.size());
				
				commits.last = new ArrayList<>();
				
				for (int i=0; i<lastMaxCount; i++) { 
					commits.last.add(revWalk.parseCommit(ObjectId.fromString(commitHashes.get(i))));
				}
				
				CommitGraphUtils.sort(commits.last, 0);
				
				commits.current = new ArrayList<>(commits.last);
				for (int i=lastMaxCount; i<commitHashes.size(); i++)
					commits.current.add(revWalk.parseCommit(ObjectId.fromString(commitHashes.get(i))));
				
				CommitGraphUtils.sort(commits.current, lastMaxCount);

				commits.last = separateByDate(commits.last);
				commits.current = separateByDate(commits.current);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			return commits;
		}
		
	};
	
	private final IModel<Map<String, List<String>>> labelsModel = new LoadableDetachableModel<Map<String, List<String>>>() {
		
		@Override
		protected Map<String, List<String>> load() {
			Map<String, List<String>> labels = new HashMap<>();
			List<RefInfo> refInfos = getProject().getBranches();
			refInfos.addAll(getProject().getTags());
			for (RefInfo refInfo: refInfos) {
				if (refInfo.getPeeledObj() instanceof RevCommit) {
					RevCommit commit = (RevCommit) refInfo.getPeeledObj();
					List<String> commitLabels = labels.get(commit.name());
					if (commitLabels == null) {
						commitLabels = new ArrayList<>();
						labels.put(commit.name(), commitLabels);
					}
					commitLabels.add(Repository.shortenRefName(refInfo.getRef().getName()));
				}
			}
			return labels;
		}
	};
	
	private transient Collection<ObjectId> commitIdsToQueryStatus;
	
	private WebMarkupContainer body;
	
	private WebMarkupContainer foot;
	
	private RepeatingView commitsView;
	
	public CommitListPanel(String id, Project project, @Nullable String query) {
		super(id);
		this.projectModel = new EntityModel<Project>(project);
		this.query = query;
	}
	
	@Override
	protected void onDetach() {
		parsedQueryModel.detach();
		commitsModel.detach();
		labelsModel.detach();
		projectModel.detach();
		super.onDetach();
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}
	
	@Nullable
	protected String getCompareWith() {
		return null;
	}

	protected CommitQuery getBaseQuery() {
		return new CommitQuery(new ArrayList<>());
	}

	protected void onQueryUpdated(AjaxRequestTarget target, @Nullable String query) {
	}
	
	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer others = new WebMarkupContainer("others") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(visitChildren(Component.class, new VisibleVisitor()) != null);
			}
			
		};
		add(others);
		
		others.add(new AjaxLink<Void>("showSavedQueries") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SavedQueriesClosed) {
					((SavedQueriesClosed) event.getPayload()).getHandler().add(this);
				}
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuerySaveSupport() != null && !getQuerySaveSupport().isSavedQueriesVisible());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				send(getPage(), Broadcast.BREADTH, new SavedQueriesOpened(target));
				target.add(this);
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		Component querySave;
		others.add(querySave = new AjaxLink<Void>("saveQuery") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(StringUtils.isNotBlank(query));
				setVisible(SecurityUtils.getUser() != null && getQuerySaveSupport() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) {
					tag.put("disabled", "disabled");
					tag.put("title", "Input query to save");
				}
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getQuerySaveSupport().onSaveQuery(target, query);
			}		
			
		});
		
		TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "query"));
		input.add(new CommitQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}));
		input.add(new AjaxFormComponentUpdatingBehavior("input"){
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
					target.add(querySave);
			}
			
		});
		
		Form<?> form = new Form<Void>("query");
		form.add(input);
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(body);
				target.add(foot);
				onQueryUpdated(target, query);
			}
			
		});
		add(form);
		
		body = new WebMarkupContainer("body") {
			
			@Override
			protected void onBeforeRender() {
				addOrReplace(commitsView = newCommitsView());
				super.onBeforeRender();
			}

		};
		body.setOutputMarkupId(true);
		add(body);
		
		NotificationPanel feedback;
		body.add(feedback = new NotificationPanel("feedback", this));
		
		body.add(new WebMarkupContainer("noCommits") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!feedback.anyErrorMessage() && commitsModel.getObject().current.isEmpty());
			}
			
		});		
				
		foot = new WebMarkupContainer("foot");
		foot.setOutputMarkupId(true);
		add(foot);
		
		foot.add(new AjaxLink<Void>("more") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				currentPage++;
				
				Commits commits = commitsModel.getObject();
				int commitIndex = 0;
				int lastCommitIndex = 0;
				for (int i=0; i<commits.last.size(); i++) {
					RevCommit lastCommit = commits.last.get(i);
					RevCommit currentCommit = commits.current.get(i);
					if (lastCommit == null) {
						if (currentCommit == null) {
							if (!commits.last.get(i+1).name().equals(commits.current.get(i+1).name())) 
								replaceItem(target, i);
						} else {
							addCommitClass(replaceItem(target, i), commitIndex);
						}
					} else {
						if (currentCommit == null) {
							replaceItem(target, i);
						} else if (commitIndex != lastCommitIndex 
								|| !lastCommit.name().equals(currentCommit.name())){
							addCommitClass(replaceItem(target, i), commitIndex);
						}						
					}
					if (lastCommit != null)
						lastCommitIndex++;
					if (currentCommit != null)
						commitIndex++;
				}

				StringBuilder builder = new StringBuilder();
				for (int i=commits.last.size(); i<commits.current.size(); i++) {
					Component item = newCommitItem(commitsView.newChildId(), i);
					if (commits.current.get(i) != null)
						addCommitClass(item, commitIndex++);
					commitsView.add(item);
					target.add(item);
					builder.append(String.format("$('#%s>.list').append(\"<li id='%s'></li>\");", 
							body.getMarkupId(), item.getMarkupId()));
				}
				target.prependJavaScript(builder);
				target.add(foot);
				target.appendJavaScript(renderCommitGraph());
				
				getProject().cacheCommitStatus(getBuildManager().queryStatus(getProject(), getCommitIdsToQueryStatus()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commitsModel.getObject().hasMore && currentPage < MAX_PAGES);
			}
			
		});
		
		foot.add(new WebMarkupContainer("tooMany") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(currentPage == MAX_PAGES);
			}
			
		});
	}
	
	private BuildManager getBuildManager() {
		return OneDev.getInstance(BuildManager.class);
	}
	
	private Collection<ObjectId> getCommitIdsToQueryStatus() {
		if (commitIdsToQueryStatus == null)
			commitIdsToQueryStatus = new HashSet<>();
		return commitIdsToQueryStatus;
	}
	
	private RepeatingView newCommitsView() {
		RepeatingView commitsView = new RepeatingView("commits");
		commitsView.setOutputMarkupId(true);
		
		int commitIndex = 0;
		List<RevCommit> commits = commitsModel.getObject().current;
		for (int i=0; i<commits.size(); i++) {
			Component item = newCommitItem(commitsView.newChildId(), i);
			if (commits.get(i) != null)
				addCommitClass(item, commitIndex++);
			commitsView.add(item);
		}
		getProject().cacheCommitStatus(getBuildManager().queryStatus(getProject(), getCommitIdsToQueryStatus()));
		return commitsView;
	}

	@SuppressWarnings("deprecation")
	private Component replaceItem(AjaxRequestTarget target, int index) {
		Component item = commitsView.get(index);
		Component newItem = newCommitItem(item.getId(), index);
		item.replaceWith(newItem);
		target.add(newItem);
		return newItem;
	}
	
	private void addCommitClass(Component item, int commitIndex) {
		item.add(AttributeAppender.append("class", " commit-item-" + commitIndex));
	}
	
	private Component newCommitItem(String itemId, int index) {
		List<RevCommit> current = commitsModel.getObject().current;
		RevCommit commit = current.get(index);
		
		Fragment item;
		if (commit != null) {
			item = new Fragment(itemId, "commitFrag", this);
			item.add(new ContributorAvatars("avatar", commit.getAuthorIdent(), commit.getCommitterIdent()));

			item.add(new CommitMessagePanel("message", getProject(), new LoadableDetachableModel<RevCommit>() {

				@Override
				protected RevCommit load() {
					return commitsModel.getObject().current.get(index);
				}
				
			}, new LoadableDetachableModel<List<Pattern>>() {

				@Override
				protected List<Pattern> load() {
					List<Pattern> patterns =  new ArrayList<>();
					for (CommitCriteria criteria: parsedQueryModel.getObject().getCriterias()) {
						if (criteria instanceof MessageCriteria) {
							for (String value: ((MessageCriteria) criteria).getValues())
								patterns.add(Pattern.compile(value, Pattern.CASE_INSENSITIVE));
						}
					}
					return patterns;
				}
				
			}));

			RepeatingView labelsView = new RepeatingView("labels");

			List<String> commitLabels = labelsModel.getObject().get(commit.name());
			if (commitLabels == null)
				commitLabels = new ArrayList<>();
			for (String label: commitLabels) 
				labelsView.add(new Label(labelsView.newChildId(), label));
			item.add(labelsView);
			
			item.add(new ContributorPanel("contribution", commit.getAuthorIdent(), commit.getCommitterIdent()));
			
			/*
			 * If we query a single definitive path, let's record it to be used for 
			 * diff comparison and code browsing  
			 */
			String path = null;
			
			for (CommitCriteria criteria: parsedQueryModel.getObject().getCriterias()) {
				if (criteria instanceof PathCriteria) {
					for (String value: ((PathCriteria) criteria).getValues()) {
						if (value.contains("*") || path != null) {
							path = null;
							break;
						} else {
							path = value;
						}
					}
				}
			}

			BlobIdent blobIdent;
			if (path != null) {
				blobIdent = new BlobIdent(commit.name(), path, null);
			} else {
				blobIdent = new BlobIdent(commit.name(), null, FileMode.TREE.getBits());
			}
			ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
			PageParameters params = ProjectBlobPage.paramsOf(getProject(), browseState);
			item.add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
			
			if (getCompareWith() != null) {
				RevisionComparePage.State compareState = new RevisionComparePage.State();
				compareState.leftSide = new ProjectAndRevision(getProject(), commit.name());
				compareState.rightSide = new ProjectAndRevision(getProject(), getCompareWith());
				if (path != null)
					compareState.pathFilter = PatternSet.quoteIfNecessary(path);
				compareState.tabPanel = RevisionComparePage.TabPanel.FILE_CHANGES;
				
				params = RevisionComparePage.paramsOf(getProject(), compareState);
				item.add(new ViewStateAwarePageLink<Void>("compare", RevisionComparePage.class, params));
			} else {
				item.add(new WebMarkupContainer("compare").setVisible(false));
			}

			CommitDetailPage.State commitState = new CommitDetailPage.State();
			commitState.revision = commit.name();
			params = CommitDetailPage.paramsOf(getProject(), commitState);
			Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
			item.add(hashLink);
			hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
			item.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));
			
			getCommitIdsToQueryStatus().add(commit.copy());
			item.add(new CommitStatusPanel("buildStatus", getProject(), commit.copy()));

			item.add(AttributeAppender.append("class", "commit clearfix"));
		} else {
			item = new Fragment(itemId, "dateFrag", this);
			DateTime dateTime = new DateTime(current.get(index+1).getCommitterIdent().getWhen());
			item.add(new Label("date", Constants.DATE_FORMATTER.print(dateTime)));
			item.add(AttributeAppender.append("class", "date"));
		}
		item.setOutputMarkupId(true);
		
		return item;
	}
	
	private String renderCommitGraph() {
		String jsonOfCommits = CommitGraphUtils.asJSON(commitsModel.getObject().current);
		return String.format("onedev.server.commitGraph.render('%s', %s);", body.getMarkupId(), jsonOfCommits);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CommitListResourceReference()));
		response.render(JavaScriptHeaderItem.forReference(new CommitGraphResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(renderCommitGraph()));
	}
	
	/*
	 * We do not use "--date-order", "--topo-order" or "--author-order" option of git log to 
	 * retrieve commits as they can be slow. However use the default log ordering has the 
	 * possibility of returning parent before child which can corrupt the commit lane. To 
	 * solve this issue, we sort the commits in memory so that parent always comes after 
	 * child. When more commits are loaded via "more" button, it is possible that some 
	 * commits displayed previously can be parent of commits loaded lately and should be 
	 * moved. To work around this issue, we calculate exact order of commits being displayed 
	 * as "last", and calculate commits will be displayed as "current". Then we compare them 
	 * to see which commit item in the page should be replaced, and which should be added. 
	 *  
	 * @author robin
	 *
	 */
	private static class Commits {
		
		List<RevCommit> last;
		
		List<RevCommit> current;
		
		boolean hasMore;
	}
	
}
