package com.pmease.gitplex.web.page.repository.commit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.lib.Ref;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.ajaxlistener.IndicateLoadingListener;
import com.pmease.commons.wicket.assets.clearable.ClearableResourceReference;
import com.pmease.commons.wicket.assets.snapsvg.SnapSvgResourceReference;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.personlink.PersonLink;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;
import com.pmease.gitplex.web.utils.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class RepoCommitsPage extends RepositoryPage {

	private static final Logger logger = LoggerFactory.getLogger(RepoCommitsPage.class);
	
	private static final String GIT_ERROR_START = "Command error output: ";
	
	private static final int COUNT = 50;
	
	private static final int MAX_STEPS = 50;
	
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
			
			LogCommand logCommand = new LogCommand(getRepository().git().repoDir());
			logCommand.parentRewriting(true).ignoreCase(true);
			
			List<Commit> logCommits;
			try {
				state.applyTo(logCommand);
				logCommits = logCommand.call();
			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().contains(GIT_ERROR_START)) {
					queryForm.error(StringUtils.substringAfter(e.getMessage(), GIT_ERROR_START));
					logCommits = new ArrayList<>();
				} else {
					throw Throwables.propagate(e);
				}
			}
			
			hasMore = logCommits.size() == state.step*COUNT;
			
			int lastMaxCount = (state.step-1)*COUNT;

			commits.last = new ArrayList<>();
			
			for (int i=0; i<lastMaxCount; i++) 
				commits.last.add(logCommits.get(i));
			
			sort(commits.last, 0);
			
			commits.current = new ArrayList<>(commits.last);
			for (int i=lastMaxCount; i<logCommits.size(); i++)
				commits.current.add(logCommits.get(i));
			
			sort(commits.current, lastMaxCount);

			commits.last = separateByDate(commits.last);
			commits.current = separateByDate(commits.current);
			
			return commits;
		}
		
	};
	
	private IModel<Map<String, List<String>>> labelsModel = new LoadableDetachableModel<Map<String, List<String>>>() {

		@Override
		protected Map<String, List<String>> load() {
			Map<String, List<String>> labels = new HashMap<>();
			Map<String, Ref> refs = new HashMap<>();
			refs.putAll(getRepository().getRefs(Git.REFS_HEADS));
			for (Map.Entry<String, Ref> entry: refs.entrySet()) {
				String commitHash = entry.getValue().getObjectId().name();
				List<String> commitLabels = labels.get(commitHash);
				if (commitLabels == null) {
					commitLabels = new ArrayList<>();
					labels.put(commitHash, commitLabels);
				}
				commitLabels.add(entry.getKey());
			}
			return labels;
		}
		
	};
	
	public RepoCommitsPage(PageParameters params) {
		super(params);
		
		state = new HistoryState(params);
	}
	
	private void sort(List<Commit> commits, int from) {
		final Map<String, Long> hash2index = new HashMap<>();
		Map<String, Commit> hash2commit = new HashMap<>();
		for (int i=0; i<commits.size(); i++) {
			Commit commit = commits.get(i);
			hash2index.put(commit.getHash(), 1L*i*commits.size());
			hash2commit.put(commit.getHash(), commit);
		}

		Stack<Commit> stack = new Stack<>();
		
		for (int i=commits.size()-1; i>=from; i--)
			stack.push(commits.get(i));

		// commits are nearly ordered, so this should be fast
		while (!stack.isEmpty()) {
			Commit commit = stack.pop();
			long commitIndex = hash2index.get(commit.getHash());
			int count = 1;
			for (String parentHash: commit.getParentHashes()) {
				Long parentIndex = hash2index.get(parentHash);
				if (parentIndex != null && parentIndex.longValue()<commitIndex) {
					stack.push(hash2commit.get(parentHash));
					hash2index.put(parentHash, commitIndex+(count++));
				}
			}
		}
		
		Collections.sort(commits, new Comparator<Commit>() {

			@Override
			public int compare(Commit o1, Commit o2) {
				long value = hash2index.get(o1.getHash()) - hash2index.get(o2.getHash());
				if (value < 0)
					return -1;
				else if (value > 0)
					return 1;
				else
					return 0;
			}
			
		});
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
					if (state.query != null) // validate query
						LogCommandDecorator.parse(state.query);
					updateCommits(target);
				} catch (Exception e) {
					logger.error("Error parsing commit query string: " + state.query, e);
					error("Syntax error in query string");
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
				return state.query;
			}

			@Override
			public void setObject(String object) {
				state.query = object;
			}
			
		}).add(new QueryAssistBehavior(repoModel)));
		
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
				state.step++;
				
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
				String script = String.format("gitplex.repocommits.onCommitsLoaded(%s);", getCommitsJson());
				target.appendJavaScript(script);
				pushState(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(hasMore && state.step < MAX_STEPS);
			}
			
		});
		foot.add(new WebMarkupContainer("tooMany") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(state.step==MAX_STEPS);
			}
			
		});
		add(foot);
	}
	
	private void updateCommits(AjaxRequestTarget target) {
		state.step = 1;

		target.add(feedback);
		body.replace(commitsView = newCommitsView());
		target.add(body);

		target.add(foot);
		
		String script = String.format("gitplex.repocommits.onCommitsLoaded(%s);", getCommitsJson());
		target.appendJavaScript(script);
		
		pushState(target);
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getRepository(), state);
		CharSequence url = RequestCycle.get().urlFor(RepoCommitsPage.class, params);
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
			item.add(new PersonLink("avatar", Model.of(commit.getAuthor()), AvatarMode.AVATAR));

			item.add(new CommitMessagePanel("message", repoModel, new LoadableDetachableModel<Commit>() {

				@Override
				protected Commit load() {
					return commitsModel.getObject().current.get(index);
				}
				
			}));

			RepeatingView labelsView = new RepeatingView("labels");

			List<String> commitLabels = labelsModel.getObject().get(commit.getHash());
			if (commitLabels == null)
				commitLabels = new ArrayList<>();
			for (String label: commitLabels) 
				labelsView.add(new Label(labelsView.newChildId(), label));
			item.add(labelsView);
			
			item.add(new PersonLink("name", Model.of(commit.getAuthor()), AvatarMode.NAME));
			item.add(new Label("age", DateUtils.formatAge(commit.getAuthor().getWhen())));
			
			item.add(new CommitHashPanel("hash", Model.of(commit.getHash())));
			
			RepoFileState state = new RepoFileState();
			state.blobIdent.revision = commit.getHash();
			item.add(new BookmarkablePageLink<Void>("codeLink", RepoFilePage.class, 
					RepoFilePage.paramsOf(repoModel.getObject(), state)));

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
	
	public static PageParameters paramsOf(Repository repository, HistoryState state) {
		PageParameters params = paramsOf(repository);
		if (state.query != null)
			params.set(PARAM_QUERY, state.query);
		if (state.step != 1)
			params.set(PARAM_STEP, state.step);
		return params;
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoCommitsPage.class, paramsOf(repository));
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		state = (HistoryState) data;

		target.add(queryForm);
		
		body.replace(commitsView = newCommitsView());
		target.add(body);
		
		target.add(foot);

		String script = String.format("gitplex.repocommits.onCommitsLoaded(%s);", getCommitsJson());
		target.appendJavaScript(script);
	}

	@Override
	protected void onDetach() {
		commitsModel.detach();
		labelsModel.detach();
		
		super.onDetach();
	}

	private String getCommitsJson() {
		List<Commit> commits = commitsModel.getObject().current;
		Map<String, Integer> hash2index = new HashMap<>();
		int commitIndex = 0;
		for (int i=0; i<commits.size(); i++) { 
			Commit commit = commits.get(i);
			if (commit != null)
				hash2index.put(commit.getHash(), commitIndex++);
		}
		List<List<Integer>> commitIndexes = new ArrayList<>();
		for (Commit commit: commits) {
			if (commit != null) {
				List<Integer> parentIndexes = new ArrayList<>();
				for (String parentHash: commit.getParentHashes()) {
					Integer parentIndex = hash2index.get(parentHash);
					if (parentIndex != null)
						parentIndexes.add(parentIndex);
				}
				commitIndexes.add(parentIndexes);
			}
		}
		try {
			return GitPlex.getInstance(ObjectMapper.class).writeValueAsString(commitIndexes);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
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
		response.render(JavaScriptHeaderItem.forReference(SnapSvgResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RepoCommitsPage.class, "repo-commits.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RepoCommitsPage.class, "repo-commits.css")));
		
		String script = String.format("gitplex.repocommits.init(%s);", getCommitsJson());
		response.render(OnDomReadyHeaderItem.forScript(script));
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

		private String query;
		
		private int step = 1;
		
		public HistoryState() {
		}
		
		public HistoryState(String query) {
			this.query = query;
		}
		
		public HistoryState(PageParameters params) {
			query = params.get(PARAM_QUERY).toString();
			
			Integer step = params.get(PARAM_STEP).toOptionalInteger();
			if (step != null)
				this.step = step.intValue();		
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public int getStep() {
			return step;
		}

		public void setStep(int step) {
			this.step = step;
		}
		
		public void applyTo(LogCommand logCommand) {
			if (step > MAX_STEPS)
				throw new RuntimeException("Step should be no more than " + MAX_STEPS);
			
			logCommand.count(step*COUNT);

			if (query != null) 
				new ParseTreeWalker().walk(new LogCommandDecorator(logCommand), LogCommandDecorator.parse(query));
		}
		
	}

}