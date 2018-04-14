package io.onedev.server.web.page.project.commits;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
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
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.java.JavaEscape;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.manager.VerificationManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.ProjectAndRevision;
import io.onedev.server.util.Constants;
import io.onedev.server.util.Verification;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.avatar.ContributorAvatars;
import io.onedev.server.web.component.commitgraph.CommitGraphResourceReference;
import io.onedev.server.web.component.commitgraph.CommitGraphUtils;
import io.onedev.server.web.component.commitmessage.ExpandableCommitMessagePanel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.verification.VerificationStatusPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitQueryParser.CriteriaContext;
import io.onedev.server.web.page.project.commits.CommitQueryParser.FuzzyCriteriaContext;
import io.onedev.server.web.page.project.commits.CommitQueryParser.QueryContext;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.util.ajaxlistener.ShowGlobalLoadingIndicatorImmediatelyListener;
import io.onedev.server.web.util.model.CommitRefsModel;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public class ProjectCommitsPage extends ProjectPage {

	private static final Logger logger = LoggerFactory.getLogger(ProjectCommitsPage.class);
	
	private static final String GIT_ERROR_START = "Command error output: ";
	
	private static final int COUNT = 50;
	
	private static final int MAX_PAGES = 50;
	
	private static final String PARAM_COMPARE_WITH = "compareWith";
	
	private static final String PARAM_COMMIT_QUERY = "commitQuery";
	
	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private State state = new State();
	
	private boolean hasMore;
	
	private WebMarkupContainer body;
	
	private Form<?> queryForm;
	
	private transient Optional<QueryContext> queryContext;
	
	private RepeatingView commitsView;
	
	private NotificationPanel feedback;
	
	private WebMarkupContainer foot;
	
	private IModel<Commits> commitsModel = new LoadableDetachableModel<Commits>() {

		@Override
		protected Commits load() {
			Commits commits = new Commits();
			List<String> commitHashes;
			try {
				RevListCommand command = new RevListCommand(getProject().getGitDir());
				command.ignoreCase(true);
				
				if (state.page > MAX_PAGES)
					throw new RuntimeException("Page should be no more than " + MAX_PAGES);
				
				command.count(state.page*COUNT);

				QueryContext queryContext = getQueryContext();
				if (queryContext != null) {
					for (CriteriaContext criteria: queryContext.criteria()) {
						if (criteria.authorCriteria() != null) {
							String value = criteria.authorCriteria().Value().getText();
							value = StringUtils.replace(JavaEscape.unescapeJava(removeParens(value)), "*", ".*");
							command.authors().add(value);
						} else if (criteria.committerCriteria() != null) {
							String value = criteria.committerCriteria().Value().getText();
							value = StringUtils.replace(JavaEscape.unescapeJava(removeParens(value)), "*", ".*");
							command.committers().add(value);
						} else if (criteria.pathCriteria() != null) {
							command.paths().add(removeParens(criteria.pathCriteria().Value().getText()));
						} else if (criteria.beforeCriteria() != null) {
							command.before(removeParens(criteria.beforeCriteria().Value().getText()));
						} else if (criteria.afterCriteria() != null) {
							command.after(removeParens(criteria.afterCriteria().Value().getText()));
						} 
					}
					command.messages(getMessages(queryContext));
					
					boolean ranged = false;
					for (Revision revision: getRevisions(queryContext)) {
						if (revision.since) {
							command.revisions().add("^" + revision.value);
							ranged = true;
						} else if (revision.until) {
							command.revisions().add(revision.value);
							ranged = true;
						} else if (getProject().getBranchRef(revision.value) != null) {
							ranged = true;
							command.revisions().add(revision.value);
						} else {
							command.revisions().add(revision.value);
						}
					}
					if (command.revisions().size() == 1 && !ranged) {
						command.count(1);
					}
				}
				
				if (command.revisions().isEmpty() && state.compareWith != null)
					command.revisions(Lists.newArrayList(state.compareWith));
				
				commitHashes = command.call();
			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().contains(GIT_ERROR_START)) {
					queryForm.error(StringUtils.substringAfter(e.getMessage(), GIT_ERROR_START));
					commitHashes = new ArrayList<>();
				} else {
					throw Throwables.propagate(e);
				}
			}
			
			hasMore = commitHashes.size() == state.page*COUNT;
			
			try (RevWalk revWalk = new RevWalk(getProject().getRepository())) {
				int lastMaxCount = Math.min((state.page-1)*COUNT, commitHashes.size());
				
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
				
				return commits;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	};
	
	private CommitRefsModel labelsModel = new CommitRefsModel(projectModel);
	
	public ProjectCommitsPage(PageParameters params) {
		super(params);
		
		state.compareWith = params.get(PARAM_COMPARE_WITH).toString();
		state.query = params.get(PARAM_COMMIT_QUERY).toString();
		Integer page = params.get(PARAM_CURRENT_PAGE).toOptionalInteger();
		if (page != null)
			state.page = page.intValue();		
	}
	
	@SuppressWarnings("deprecation")
	private Component replaceItem(AjaxRequestTarget target, int index) {
		Component item = commitsView.get(index);
		Component newItem = newCommitItem(item.getId(), index);
		item.replaceWith(newItem);
		target.add(newItem);
		return newItem;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		TextField<String> queryInput = new TextField<String>("input", Model.of(state.query));
		
		queryForm = new Form<Void>("query") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				try {
					String query = queryInput.getModelObject();
					queryContext = Optional.fromNullable(parse(query)); // validate query
					state.query = query;
					updateCommits(target);
				} catch (Exception e) {
					logger.error("Error parsing commit query string: " + state.query, e);
					if (StringUtils.isNotBlank(e.getMessage()))
						error(e.getMessage());
					else
						error("Malformed commit query");
					target.add(feedback);
				}
			}

			@Override
			protected void onError() {
				super.onError();
				
				RequestCycle.get().find(AjaxRequestTarget.class).add(feedback);
			}

		};
		
		queryForm.add(queryInput.add(new CommitQueryBehavior(projectModel)));
		
		queryForm.add(new AjaxButton("submit") {});
		queryForm.setOutputMarkupId(true);
		add(queryForm);
		
		add(feedback = new NotificationPanel("feedback", queryForm));
		feedback.setOutputMarkupPlaceholderTag(true);
		
		body = new WebMarkupContainer("body");
		body.setOutputMarkupId(true);
		add(body);
		body.add(commitsView = newCommitsView());
		body.add(new WebMarkupContainer("noCommits") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!queryForm.hasErrorMessage() && commitsModel.getObject().current.isEmpty());
			}
			
		});

		foot = new WebMarkupContainer("commitsFoot");
		foot.setOutputMarkupId(true);
		
		foot.add(new AjaxLink<Void>("more") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ShowGlobalLoadingIndicatorImmediatelyListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				state.page = state.page+1;
				
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
					builder.append(String.format("$('#repo-commits>.body>.list').append(\"<li id='%s'></li>\");", 
							item.getMarkupId()));
				}
				target.prependJavaScript(builder);
				target.add(feedback);
				target.add(foot);
				target.appendJavaScript(renderCommitGraph());
				pushState(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(hasMore && state.page < MAX_PAGES);
			}
			
		});
		foot.add(new WebMarkupContainer("tooMany") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(state.page == MAX_PAGES);
			}
			
		});
		add(foot);
	}
	
	private void updateCommits(AjaxRequestTarget target) {
		state.page = 1;

		target.add(feedback);
		body.replace(commitsView = newCommitsView());
		target.add(body);
		target.add(foot);
		target.appendJavaScript(renderCommitGraph());
		pushState(target);
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getProject(), state);
		CharSequence url = RequestCycle.get().urlFor(ProjectCommitsPage.class, params);
		pushState(target, url.toString(), state);
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
		
		return commitsView;
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

			item.add(new ExpandableCommitMessagePanel("message", projectModel, new LoadableDetachableModel<RevCommit>() {

				@Override
				protected RevCommit load() {
					return commitsModel.getObject().current.get(index);
				}
				
			}, new LoadableDetachableModel<List<Pattern>>() {

				@Override
				protected List<Pattern> load() {
					List<Pattern> patterns =  new ArrayList<>();
					QueryContext queryContext = getQueryContext();
					if (queryContext != null) {
						for (String message: getMessages(queryContext)) {
							patterns.add(Pattern.compile(message, Pattern.CASE_INSENSITIVE));
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
			
			item.add(new ContributorPanel("contribution", 
					commit.getAuthorIdent(), commit.getCommitterIdent(), true));
			
			/*
			 * If we query a single definitive path, let's record it to be used for 
			 * diff comparison and code browsing  
			 */
			String path = null;
			
			QueryContext queryContext = getQueryContext();
			if (queryContext != null) {
				for (CriteriaContext criteria: queryContext.criteria()) {
					if (criteria.pathCriteria() != null) {
						String value = criteria.pathCriteria().Value().getText();
						value = value.substring(1);
						value = value.substring(0, value.length()-1);
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
			PageParameters params = ProjectBlobPage.paramsOf(projectModel.getObject(), browseState);
			item.add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
			
			if (state.compareWith != null) {
				RevisionComparePage.State compareState = new RevisionComparePage.State();
				compareState.leftSide = new ProjectAndRevision(getProject(), commit.name());
				compareState.rightSide = new ProjectAndRevision(getProject(), state.compareWith);
				compareState.pathFilter = path;
				compareState.tabPanel = RevisionComparePage.TabPanel.CHANGES;
				
				params = RevisionComparePage.paramsOf(getProject(), compareState);
				item.add(new ViewStateAwarePageLink<Void>("compare", RevisionComparePage.class, params));
			} else {
				item.add(new WebMarkupContainer("compare").setVisible(false));
			}

			CommitDetailPage.State commitState = new CommitDetailPage.State();
			commitState.revision = commit.name();
			params = CommitDetailPage.paramsOf(projectModel.getObject(), commitState);
			Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
			item.add(hashLink);
			hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
			item.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));
			
			item.add(new VerificationStatusPanel("verificationStatus", new LoadableDetachableModel<Map<String, Verification>>() {

				@Override
				protected Map<String, Verification> load() {
					return OneDev.getInstance(VerificationManager.class).getVerifications(getProject(), commit.name());
				}
				
			}));

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
	
	public static PageParameters paramsOf(Project project, State state) {
		PageParameters params = paramsOf(project);
		if (state.compareWith != null)
			params.set(PARAM_COMPARE_WITH, state.compareWith);
		if (state.query != null)
			params.set(PARAM_COMMIT_QUERY, state.query);
		if (state.page != 1)
			params.set(PARAM_CURRENT_PAGE, state.page);
		return params;
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		state = (State) data;

		target.add(queryForm);
		
		body.replace(commitsView = newCommitsView());
		target.add(body);
		target.add(foot);
		
		target.appendJavaScript(renderCommitGraph());
	}
	
	private String renderCommitGraph() {
		String jsonOfCommits = CommitGraphUtils.asJSON(commitsModel.getObject().current);
		return String.format("onedev.server.commitgraph.render('%s', %s);", body.getMarkupId(), jsonOfCommits);
	}

	@Override
	protected void onDetach() {
		commitsModel.detach();
		labelsModel.detach();
		
		super.onDetach();
	}

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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new CommitGraphResourceReference()));
		response.render(CssHeaderItem.forReference(new ProjectCommitsResourceReference()));
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
	}
	
	private String removeParens(String value) {
		if (value.startsWith("("))
			value = value.substring(1);
		if (value.endsWith(")"))
			value = value.substring(0, value.length()-1);
		return value;
	}
	
	private List<String> getMessages(QueryContext queryContext) {
		List<String> messages = new ArrayList<>();
		List<String> fuzzyMessages = new ArrayList<>();
		for (CriteriaContext criteria: queryContext.criteria()) {
			if (criteria.messageCriteria() != null) {
				String message = JavaEscape.unescapeJava(removeParens(criteria.messageCriteria().Value().getText()));
				messages.add(StringUtils.replace(message, "*", ".*"));
			} else {
				FuzzyCriteriaContext fuzzyCriteria = criteria.fuzzyCriteria();
				if (fuzzyCriteria != null && fuzzyCriteria.UNTIL() == null 
						&& fuzzyCriteria.SINCE() == null 
						&& getProject().getObjectId(fuzzyCriteria.getText(), false) == null) {
					fuzzyMessages.add(fuzzyCriteria.getText());
				}
			}
		}
		if (!fuzzyMessages.isEmpty()) 
			messages.add(Joiner.on(".*").join(fuzzyMessages));
		return messages;
	}
	
	private List<Revision> getRevisions(QueryContext queryContext) {
		List<Revision> revisions = new ArrayList<>();
		for (CriteriaContext criteria: queryContext.criteria()) {
			if (criteria.revisionCriteria() != null) {
				Revision revision = new Revision();
				revision.value = removeParens(criteria.revisionCriteria().Value().getText());
				if (criteria.revisionCriteria().SINCE() != null)
					revision.since = true;
				else if (criteria.revisionCriteria().UNTIL() != null)
					revision.until = true;
				revisions.add(revision);
			} else {
				FuzzyCriteriaContext fuzzyCriteria = criteria.fuzzyCriteria();
				if (fuzzyCriteria != null && getProject().getObjectId(fuzzyCriteria.FuzzyValue().getText(), false) != null) {
					Revision revision = new Revision();
					revision.value = fuzzyCriteria.FuzzyValue().getText();
					if (fuzzyCriteria.SINCE() != null)
						revision.since = true;
					else if (fuzzyCriteria.UNTIL() != null)
						revision.until = true;
					revisions.add(revision);
				}
			}
		}
		return revisions;
	}
	
	@Nullable
	private QueryContext getQueryContext() {
		if (queryContext == null)
			queryContext = Optional.fromNullable(parse(state.query));
		return queryContext.orNull();
	}
	
	@Nullable
	private QueryContext parse(@Nullable String query) {
		if (query != null) {
			ANTLRInputStream is = new ANTLRInputStream(query); 
			CommitQueryLexer lexer = new CommitQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					if (e != null) {
						logger.error("Error lexing commit query", e);
					} else if (msg != null) {
						logger.error("Error lexing commit query: " + msg);
					}
					throw new RuntimeException("Malformed commit query");
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			CommitQueryParser parser = new CommitQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			return parser.query();
		} else {
			return null;
		}
	}
	
	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;

		public String compareWith;
		
		public String query;
		
		public int page = 1;
		
	}

	private static class Revision implements Serializable {
		
		public boolean since;
		
		public boolean until;
		
		public String value;
		
	}
}