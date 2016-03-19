package com.pmease.gitplex.web.page.depot.commit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
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
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.java.JavaEscape;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.concurrent.PrioritizedCallable;
import com.pmease.commons.wicket.ajaxlistener.IndicateLoadingListener;
import com.pmease.commons.wicket.assets.clearable.ClearableResourceReference;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.component.DepotAndRevision;
import com.pmease.gitplex.core.manager.WorkManager;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.ContributorAvatars;
import com.pmease.gitplex.web.component.commitgraph.CommitGraphResourceReference;
import com.pmease.gitplex.web.component.commitgraph.CommitGraphUtils;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.contributionpanel.ContributionPanel;
import com.pmease.gitplex.web.component.hashandcode.HashAndCodePanel;
import com.pmease.gitplex.web.model.CommitRefsModel;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.commit.CommitQueryParser.CriteriaContext;
import com.pmease.gitplex.web.page.depot.commit.CommitQueryParser.QueryContext;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import jersey.repackaged.com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class DepotCommitsPage extends DepotPage {

	private static final Logger logger = LoggerFactory.getLogger(DepotCommitsPage.class);
	
	private static final int LOG_PRIORITY = 1;
	
	private static final String GIT_ERROR_START = "Command error output: ";
	
	private static final int COUNT = 50;
	
	private static final int MAX_STEPS = 50;
	
	private static final String PARAM_COMPARE_WITH = "compareWith";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_STEP = "step";
	
	private HistoryState state = new HistoryState();
	
	private boolean hasMore;
	
	private WebMarkupContainer body;
	
	private Form<?> queryForm;
	
	private RepeatingView commitsView;
	
	private NotificationPanel feedback;
	
	private WebMarkupContainer foot;
	
	private IModel<Commits> commitsModel = new LoadableDetachableModel<Commits>() {

		@Override
		protected Commits load() {
			Commits commits = new Commits();
			
			LogCommand logCommand = new LogCommand(getDepot().git().depotDir());
			logCommand.ignoreCase(true);
			
			List<Commit> logCommits;
			try {
				state.applyTo(logCommand);
				logCommits = GitPlex.getInstance(WorkManager.class).submit(new PrioritizedCallable<List<Commit>>(LOG_PRIORITY) {

					@Override
					public List<Commit> call() throws Exception {
						return logCommand.call();
					}
					
				}).get();
			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().contains(GIT_ERROR_START)) {
					queryForm.error(StringUtils.substringAfter(e.getMessage(), GIT_ERROR_START));
					logCommits = new ArrayList<>();
				} else {
					throw Throwables.propagate(e);
				}
			}
			
			hasMore = logCommits.size() == state.getStep()*COUNT;
			
			int lastMaxCount = (state.getStep()-1)*COUNT;

			commits.last = new ArrayList<>();
			
			for (int i=0; i<lastMaxCount; i++) 
				commits.last.add(logCommits.get(i));
			
			CommitGraphUtils.sort(commits.last, 0);
			
			commits.current = new ArrayList<>(commits.last);
			for (int i=lastMaxCount; i<logCommits.size(); i++)
				commits.current.add(logCommits.get(i));
			
			CommitGraphUtils.sort(commits.current, lastMaxCount);

			commits.last = separateByDate(commits.last);
			commits.current = separateByDate(commits.current);
			
			return commits;
		}
		
	};
	
	private CommitRefsModel labelsModel = new CommitRefsModel(depotModel);
	
	public DepotCommitsPage(PageParameters params) {
		super(params);
		
		state = new HistoryState(params);
	}
	
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

		queryForm = new Form<Void>("query") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				try {
					state.getParseTree(); // validate query
					updateCommits(target);
				} catch (Exception e) {
					logger.error("Error parsing commit query string: " + state.getQuery(), e);
					if (StringUtils.isNotBlank(e.getMessage()))
						error(e.getMessage());
					else
						error("Syntax error");
					target.add(feedback);
				}
			}

			@Override
			protected void onError() {
				super.onError();
				
				RequestCycle.get().find(AjaxRequestTarget.class).add(feedback);
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("gitplex.repocommits.initQuery();"));
			}
			
		};
		queryForm.add(new TextField<String>("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.getQuery();
			}

			@Override
			public void setObject(String object) {
				state.setQuery(object);
			}
			
		}).add(new QueryAssistBehavior(depotModel)));
		
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

		foot = new WebMarkupContainer("foot");
		foot.setOutputMarkupId(true);
		
		foot.add(new AjaxLink<Void>("more") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				state.setStep(state.getStep()+1);
				
				Commits commits = commitsModel.getObject();
				int commitIndex = 0;
				int lastCommitIndex = 0;
				for (int i=0; i<commits.last.size(); i++) {
					Commit lastCommit = commits.last.get(i);
					Commit currentCommit = commits.current.get(i);
					if (lastCommit == null) {
						if (currentCommit == null) {
							if (!commits.last.get(i+1).getHash().equals(commits.current.get(i+1).getHash())) 
								replaceItem(target, i);
						} else {
							addCommitClass(replaceItem(target, i), commitIndex);
						}
					} else {
						if (currentCommit == null) {
							replaceItem(target, i);
						} else if (commitIndex != lastCommitIndex 
								|| !lastCommit.getHash().equals(currentCommit.getHash())){
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
				setVisible(hasMore && state.getStep() < MAX_STEPS);
			}
			
		});
		foot.add(new WebMarkupContainer("tooMany") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(state.getStep() == MAX_STEPS);
			}
			
		});
		add(foot);
	}
	
	private void updateCommits(AjaxRequestTarget target) {
		state.setStep(1);

		target.add(feedback);
		body.replace(commitsView = newCommitsView());
		target.add(body);
		target.add(foot);
		target.appendJavaScript(renderCommitGraph());
		pushState(target);
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getDepot(), state);
		CharSequence url = RequestCycle.get().urlFor(DepotCommitsPage.class, params);
		pushState(target, url.toString(), state);
	}
	
	private RepeatingView newCommitsView() {
		RepeatingView commitsView = new RepeatingView("commits");
		commitsView.setOutputMarkupId(true);
		
		int commitIndex = 0;
		List<Commit> commits = commitsModel.getObject().current;
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
	
	private Component newCommitItem(String itemId, final int index) {
		List<Commit> current = commitsModel.getObject().current;
		Commit commit = current.get(index);
		
		Fragment item;
		if (commit != null) {
			item = new Fragment(itemId, "commitFrag", this);
			item.add(new ContributorAvatars("avatar", commit.getAuthor(), commit.getCommitter()));

			item.add(new CommitMessagePanel("message", depotModel, new LoadableDetachableModel<Commit>() {

				@Override
				protected Commit load() {
					return commitsModel.getObject().current.get(index);
				}
				
			}, new LoadableDetachableModel<List<Pattern>>() {

				@Override
				protected List<Pattern> load() {
					List<Pattern> patterns =  new ArrayList<>();
					QueryContext parseTree = state.getParseTree();
					if (parseTree != null) {
						for (CriteriaContext criteria: parseTree.criteria()) {
							if (criteria.message() != null) {
								String message = criteria.message().Value().getText();
								message = message.substring(1);
								message = message.substring(0, message.length()-1);
								message = JavaEscape.unescapeJava(message);
								patterns.add(Pattern.compile(message, Pattern.CASE_INSENSITIVE));
							}
						}
					}
					return patterns;
				}
				
			}));

			RepeatingView labelsView = new RepeatingView("labels");

			List<String> commitLabels = labelsModel.getObject().get(commit.getHash());
			if (commitLabels == null)
				commitLabels = new ArrayList<>();
			for (String label: commitLabels) 
				labelsView.add(new Label(labelsView.newChildId(), label));
			item.add(labelsView);
			
			item.add(new ContributionPanel("contribution", commit.getAuthor(), commit.getCommitter()));
			
			/*
			 * If we query a single definitive path, let's record it to be used for 
			 * diff comparison and code browsing  
			 */
			String path = null;
			
			QueryContext parseTree = state.getParseTree();
			if (parseTree != null) {
				for (CriteriaContext criteria: parseTree.criteria()) {
					if (criteria.path() != null) {
						String value = criteria.path().Value().getText();
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
			if (state.getCompareWith() != null) {
				PageParameters params = RevisionComparePage.paramsOf(getDepot(), 
						new DepotAndRevision(getDepot(), commit.getHash()), 
						new DepotAndRevision(getDepot(), state.getCompareWith()), 
						true, path);
				item.add(new BookmarkablePageLink<Void>("compare", RevisionComparePage.class, params));
			} else {
				item.add(new WebMarkupContainer("compare").setVisible(false));
			}
			item.add(new HashAndCodePanel("hashAndCode", depotModel, commit.getHash(), path));

			item.add(AttributeAppender.append("class", "commit clearfix"));
		} else {
			item = new Fragment(itemId, "dateFrag", this);
			DateTime dateTime = new DateTime(current.get(index+1).getCommitter().getWhen());
			item.add(new Label("date", Constants.DATE_FORMATTER.print(dateTime)));
			item.add(AttributeAppender.append("class", "date"));
		}
		item.setOutputMarkupId(true);
		
		return item;
	}
	
	public static PageParameters paramsOf(Depot depot, HistoryState state) {
		PageParameters params = paramsOf(depot);
		if (state.getCompareWith() != null)
			params.set(PARAM_COMPARE_WITH, state.getCompareWith());
		if (state.getQuery() != null)
			params.set(PARAM_QUERY, state.getQuery());
		if (state.getStep() != 1)
			params.set(PARAM_STEP, state.getStep());
		return params;
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotCommitsPage.class, paramsOf(depot));
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		state = (HistoryState) data;

		target.add(queryForm);
		
		body.replace(commitsView = newCommitsView());
		target.add(body);
		target.add(foot);
		
		target.appendJavaScript(renderCommitGraph());
	}
	
	private String renderCommitGraph() {
		String jsonOfCommits = CommitGraphUtils.asJSON(commitsModel.getObject().current);
		return String.format("gitplex.commitgraph.render('%s', %s);", body.getMarkupId(), jsonOfCommits);
	}

	@Override
	protected void onDetach() {
		commitsModel.detach();
		labelsModel.detach();
		
		super.onDetach();
	}

	private List<Commit> separateByDate(List<Commit> commits) {
		List<Commit> separated = new ArrayList<>();
		DateTime groupTime = null;
		for (Commit commit: commits) {
			DateTime commitTime = new DateTime(commit.getCommitter().getWhen());
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
		
		response.render(JavaScriptHeaderItem.forReference(ClearableResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CommitGraphResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(DepotCommitsPage.class, "depot-commits.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(DepotCommitsPage.class, "depot-commits.css")));
		
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
		List<Commit> last;
		
		List<Commit> current;
	}
	
	public static class HistoryState implements Serializable {

		private static final long serialVersionUID = 1L;

		private String compareWith;
		
		private String query;
		
		private int step = 1;
		
		private transient Optional<QueryContext> parseTree;
		
		public HistoryState() {
		}
		
		public HistoryState(PageParameters params) {
			compareWith = params.get(PARAM_COMPARE_WITH).toString();
			query = params.get(PARAM_QUERY).toString();
			
			Integer step = params.get(PARAM_STEP).toOptionalInteger();
			if (step != null)
				this.step = step.intValue();		
		}

		public String getCompareWith() {
			return compareWith;
		}

		public void setCompareWith(String compareWith) {
			this.compareWith = compareWith;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
			parseTree = null;
		}

		public int getStep() {
			return step;
		}

		public void setStep(int step) {
			this.step = step;
		}
		
		@Nullable
		public QueryContext getParseTree() {
			if (parseTree == null) {
				if (query != null) {
					ANTLRInputStream is = new ANTLRInputStream(query); 
					CommitQueryLexer lexer = new CommitQueryLexer(is);
					lexer.removeErrorListeners();
					CommonTokenStream tokens = new CommonTokenStream(lexer);
					CommitQueryParser parser = new CommitQueryParser(tokens);
					parser.removeErrorListeners();
					parser.setErrorHandler(new BailErrorStrategy());
					parseTree = Optional.of(parser.query());
				} else {
					parseTree = Optional.fromNullable(null);
				}
			}
			return parseTree.orNull();
		}
		
		public void applyTo(LogCommand logCommand) {
			if (step > MAX_STEPS)
				throw new RuntimeException("Step should be no more than " + MAX_STEPS);
			
			logCommand.count(step*COUNT);

			QueryContext parseTree = getParseTree();
			if (parseTree != null) { 
				new ParseTreeWalker().walk(new LogCommandFiller(logCommand), parseTree);
				if (logCommand.revisions().isEmpty() && compareWith != null)
					logCommand.revisions(Lists.newArrayList(compareWith));
			}
		}
		
	}

}