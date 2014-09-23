package com.pmease.gitplex.web.page.repository.info.pullrequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.hibernate.HibernateUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.comment.InlineContext;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.InlineInfo;
import com.pmease.gitplex.core.model.IntegrationInfo;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.comment.event.CommentReplied;
import com.pmease.gitplex.web.component.diff.CompareResultPanel;
import com.pmease.gitplex.web.page.repository.info.pullrequest.activity.RequestActivitiesModel;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class RequestComparePage extends RequestDetailPage {

	private static final String TARGET_BRANCH_HEAD = "Head of Target Branch";
	
	private static final String INTEGRATION_PREVIEW = "Integration Preview";
	
	public static final String LATEST_COMMIT = "latest";

	private String file;
	
	private String oldCommit;
	
	private String newCommit;
	
	private final IModel<PullRequestComment> concernedCommentModel;
	
	private final RequestActivitiesModel activitiesModel = new RequestActivitiesModel() {
		
		@Override
		protected PullRequest getPullRequest() {
			return RequestComparePage.this.getPullRequest();
		}
	};
	
	private final IModel<Map<String, CommitDescription>> commitsModel = 
			new LoadableDetachableModel<Map<String, CommitDescription>>() {

		@Override
		protected LinkedHashMap<String, CommitDescription> load() {
			LinkedHashMap<String, CommitDescription> choices = new LinkedHashMap<>();
			PullRequest request = getPullRequest();

			String concernedCommit;
			if (getConcernedComment() != null)
				concernedCommit = getConcernedComment().getCommit();
			else
				concernedCommit = null;

			String name = "Base of Pull Request";
			if (request.getBaseCommit().equals(concernedCommit))
				name += " - Concerned";
			CommitDescription description = new CommitDescription(name, 
					getRepository().git().showRevision(request.getBaseCommit()).getSubject());
			choices.put(request.getBaseCommit(), description);
			
			for (int i=0; i<request.getSortedUpdates().size(); i++) {
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				int updateNo = i+1;
				name = "Head of Update #" + updateNo;
				String commitHash = update.getHeadCommit();
				if (commitHash.equals(concernedCommit))
					name += " - Concerned";
				description = new CommitDescription(name, getRepository().getCommit(commitHash).getSubject());
				choices.put(commitHash, description);
			}

			String targetHead = request.getTarget().getHeadCommit();
			if (!choices.containsKey(targetHead)) {
				description = new CommitDescription(TARGET_BRANCH_HEAD, 
						getRepository().git().showRevision(targetHead).getSubject());
				choices.put(targetHead, description);
			}

			IntegrationInfo integrationInfo = request.getIntegrationInfo();
			if (request.isOpen() 
					&& integrationInfo.getIntegrationHead() != null 
					&& !integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())) { 
				Commit commit = getRepository().git().showRevision(request.getIntegrationInfo().getIntegrationHead());
				choices.put(request.getIntegrationInfo().getIntegrationHead(), 
						new CommitDescription(INTEGRATION_PREVIEW, commit.getSubject()));
			}
			
			return choices;
		}
		
	};
	
	private CompareResultPanel compareResult;
	
	public RequestComparePage(PageParameters params) {
		super(params);

		file = params.get("file").toString();
		final Long concernedCommentId = params.get("comment").toOptionalLong();
		
		concernedCommentModel = new LoadableDetachableModel<PullRequestComment>() {

			@Override
			protected PullRequestComment load() {
				if (concernedCommentId != null)
					return GitPlex.getInstance(Dao.class).load(PullRequestComment.class, concernedCommentId);
				else 
					return null;
			}
			
		};
		
		oldCommit = params.get("original").toString();
		newCommit = params.get("revised").toString();

		if (oldCommit != null) {
			if (oldCommit.equals(LATEST_COMMIT))
				oldCommit = getPullRequest().getLatestUpdate().getHeadCommit();
			else if (!commitsModel.getObject().containsKey(oldCommit))
				throw new IllegalArgumentException("Commit '" + oldCommit + "' is not relevant to current pull request.");
		}

		if (newCommit != null) {
			if (newCommit.equals(LATEST_COMMIT))
				newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
			else if (!commitsModel.getObject().containsKey(newCommit))
				throw new IllegalArgumentException("Commit '" + newCommit + "' is not relevant to current pull request.");
		}
		
		PullRequestComment comment = getConcernedComment();
		if (comment != null) {
			if (file == null)
				file = comment.getInlineInfo().getFile();
			if (oldCommit == null)
				oldCommit = comment.getInlineInfo().getOldCommit();
			if (newCommit == null)
				newCommit = comment.getInlineInfo().getNewCommit();
		} else {
			if (oldCommit == null)
				oldCommit = getPullRequest().getBaseCommit();
			if (newCommit == null)
				newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer options = new WebMarkupContainer("compareOptions");
		options.add(new StickyBehavior());
		
		add(options);

		WebMarkupContainer oldSelector = new WebMarkupContainer("oldSelector");
		options.add(oldSelector);
		oldSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommit);
				Preconditions.checkNotNull(description);
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(oldCommit) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(oldCommit);
			}
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}, new TooltipConfig().withPlacement(Placement.bottom))));
		
		DropdownPanel oldChoicesDropdown = new DropdownPanel("oldChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, true);
			}
			
		}; 
		options.add(oldChoicesDropdown);
		oldSelector.add(new DropdownBehavior(oldChoicesDropdown).alignWithTrigger(0, 0, 0, 100));
		
		WebMarkupContainer newSelector = new WebMarkupContainer("newSelector");
		options.add(newSelector);
		newSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommit);
				Preconditions.checkNotNull(description);
				
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(newCommit) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(newCommit);
			}
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}, new TooltipConfig().withPlacement(Placement.bottom))));
		
		DropdownPanel newChoicesDropdown = new DropdownPanel("newChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, false);
			}
			
		}; 
		options.add(newChoicesDropdown);
		newSelector.add(new DropdownBehavior(newChoicesDropdown).alignWithTrigger(0, 0, 0, 100));

		MenuPanel commonComparisons = new MenuPanel("comparisonChoices") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> items = new ArrayList<>();
				
				if (getConcernedComment() != null) {
					items.add(new ComparisonChoiceItem("Base", "Concerned") {
	
						@Override
						protected void onSelect() {
							PageParameters params = paramsOf(getPullRequest(), 
									getPullRequest().getBaseCommit(), getConcernedComment().getCommit(), null,
									getConcernedComment());
							setResponsePage(RequestComparePage.class, params);
						}
	
					});
					items.add(new ComparisonChoiceItem("Concerned", "Latest Update") {
						
						@Override
						protected void onSelect() {
							PageParameters params = paramsOf(getPullRequest(), 
									getConcernedComment().getCommit(), null, null,
									getConcernedComment());
							setResponsePage(RequestComparePage.class, params);
						}
	
					});
				}

				items.add(new ComparisonChoiceItem("Base", "Latest Update") {

					@Override
					protected void onSelect() {
						PageParameters params = paramsOf(getPullRequest(), 
								getPullRequest().getBaseCommit(), 
								getPullRequest().getLatestUpdate().getHeadCommit(), 
								file, getConcernedComment());
						setResponsePage(RequestComparePage.class, params);
					}

				});
				
				final IntegrationInfo integrationInfo = getPullRequest().getIntegrationInfo();
				if (getPullRequest().isOpen() 
						&& integrationInfo.getIntegrationHead() != null 
						&& !integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())
						&& integrationInfo.hasChanges()) { 
					items.add(new ComparisonChoiceItem("Latest Update", "Integration Preview") {

						@Override
						protected void onSelect() {
							PageParameters params = paramsOf(getPullRequest(), 
									getPullRequest().getLatestUpdate().getHeadCommit(), 
									integrationInfo.getIntegrationHead(),
									file, getConcernedComment());
							setResponsePage(RequestComparePage.class, params);
						}
						
					});
				}
				
				for (int i=0; i<getPullRequest().getSortedUpdates().size(); i++) {
					PullRequestUpdate update = getPullRequest().getSortedUpdates().get(i);
					final String baseCommit = update.getBaseCommit();
					final String headCommit = update.getHeadCommit();
					int index = i+1;
					String oldLabel;
					if (index > 1) 
						oldLabel = "Update #" + (index-1);
					else
						oldLabel = "Base";
					
					items.add(new ComparisonChoiceItem(oldLabel, "Update #" + index) {

						@Override
						protected void onSelect() {
							PageParameters params = paramsOf(getPullRequest(), baseCommit, headCommit, 
									file, getConcernedComment());
							setResponsePage(RequestComparePage.class, params);
						}
						
					});
				}
				
				return items;
			}
			
		};
		
		options.add(commonComparisons);
		options.add(new WebMarkupContainer("comparisonSelector")
				.add(new MenuBehavior(commonComparisons)
				.alignWithTrigger(50, 100, 50, 0)));
		
		options.add(new CheckBox("changedOnly", new LoadableDetachableModel<Boolean>() {

			@Override
			protected Boolean load() {
				return compareResult.isChangedOnly();
			}
			
		}).add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				compareResult.toggleChangedOnly(target);
			}
			
		}));

		add(compareResult = new CompareResultPanel("compareResult", repoModel, oldCommit, newCommit, file) {
			
			@Override
			protected InlineCommentSupport getInlineCommentSupport(final Change change) {
				Map<String, CommitDescription> commits = commitsModel.getObject();
				String oldCommitName = Preconditions.checkNotNull(commits.get(oldCommit)).getName();
				String newCommitName = Preconditions.checkNotNull(commits.get(newCommit)).getName();
				if (oldCommitName.equals(TARGET_BRANCH_HEAD) || oldCommitName.equals(INTEGRATION_PREVIEW)
						|| newCommitName.equals(TARGET_BRANCH_HEAD) || newCommitName.equals(INTEGRATION_PREVIEW)) {
					return null;
				} else {
					return new InlineCommentSupport() {
						
						@Override
						public Map<Integer, List<InlineComment>> getOldComments() {
							RevAwareChange revAwareChange = new RevAwareChange(change, oldCommit, newCommit);
							return getPullRequest().getComments(revAwareChange).getOldComments();
						}
						
						@Override
						public Map<Integer, List<InlineComment>> getNewComments() {
							RevAwareChange revAwareChange = new RevAwareChange(change, oldCommit, newCommit);
							return getPullRequest().getComments(revAwareChange).getNewComments();
						}
						
						@Override
						public InlineComment getConcernedComment() {
							return RequestComparePage.this.getConcernedComment();
						}
						
						@Override
						public InlineComment addComment(String commit, String file, int line, String content) {
							User user = GitPlex.getInstance(UserManager.class).getCurrent();
							Preconditions.checkNotNull(user);
							PullRequestComment comment = new PullRequestComment();
							getPullRequest().getComments().add(comment);
							comment.setUser(user);
							comment.setDate(new Date());
							comment.setContent(content);
							comment.setRequest(getPullRequest());
							InlineInfo inlineInfo = new InlineInfo();
							comment.setInlineInfo(inlineInfo);
							inlineInfo.setCommit(commit);
							inlineInfo.setOldCommit(oldCommit);
							inlineInfo.setNewCommit(newCommit);
							inlineInfo.setFile(file);
							inlineInfo.setLine(line);
							InlineContext context = getInlineContext(comment);
							Preconditions.checkNotNull(context);
							inlineInfo.setContext(context);
							comment.setInlineInfo(inlineInfo);
							GitPlex.getInstance(Dao.class).persist(comment);
							return comment;
						}
					};
				}
			}

			@Override
			protected void onChangeSelection(AjaxRequestTarget target, Change change) {
				file = change.getPath();
			}
			
		});

	}
	
	@Override
	public void onDetach() {
		commitsModel.detach();
		concernedCommentModel.detach();
		activitiesModel.detach();
		
		super.onDetach();
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable String oldCommit, 
			@Nullable String newCommit, @Nullable String file, @Nullable PullRequestComment concernedComment) {
		PageParameters params = RequestDetailPage.paramsOf(request);
		
		if (oldCommit != null)
			params.set("original", oldCommit);
		if (newCommit != null)
			params.set("revised", newCommit);
		if (file != null)
			params.set("file", file);
		if (concernedComment != null)
			params.set("comment", concernedComment.getId());
		
		return params;
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof CommentRemoved) {
			CommentRemoved commentRemoved = (CommentRemoved) event.getPayload();
			PullRequestComment comment = (PullRequestComment) commentRemoved.getComment();
			
			// compare identifier instead of comment object as comment may have been deleted
			// to cause LazyInitializationException
			if (getConcernedComment() != null 
					&& HibernateUtils.getId(comment).equals(HibernateUtils.getId(getConcernedComment()))) {
				PageParameters params = paramsOf(getPullRequest(), oldCommit, newCommit, 
						comment.getInlineInfo().getFile(), null);
				setResponsePage(RequestComparePage.class, params);
			}
		} else if (event.getPayload() instanceof CommentReplied) {
			CommentReplied commentReplied = (CommentReplied) event.getPayload();
			PullRequestComment comment = (PullRequestComment) commentReplied.getReply().getComment();
			comment.getInlineInfo().setOldCommit(oldCommit);
			comment.getInlineInfo().setNewCommit(newCommit);

			InlineContext context = compareResult.getInlineContext(comment);
			Preconditions.checkNotNull(context);
			comment.getInlineInfo().setContext(context);
			GitPlex.getInstance(Dao.class).persist(comment);
		}
	}

	private static class CommitDescription implements Serializable {
		private final String name;
		
		private final String subject;
		
		CommitDescription(final String name, final String subject) {
			this.name = name;
			this.subject = subject;
		}

		public String getName() {
			return name;
		}

		public String getSubject() {
			return subject;
		}
		
	}
	
	private class CommitChoicePanel extends Fragment {

		private final boolean forBase;
		
		CommitChoicePanel(String id, boolean forBase) {
			super(id, "commitChoiceFrag", RequestComparePage.this);
			
			this.forBase = forBase;
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			setOutputMarkupId(true);
			
			IModel<List<Map.Entry<String, CommitDescription>>> model = 
					new LoadableDetachableModel<List<Map.Entry<String, CommitDescription>>>() {

				@Override
				protected List<Entry<String, CommitDescription>> load() {
					List<Entry<String, CommitDescription>> entries = new ArrayList<>();
					entries.addAll(commitsModel.getObject().entrySet());
					return entries;
				}
				
			};
			
			add(new ListView<Map.Entry<String, CommitDescription>>("commits", model) {

				@Override
				protected void populateItem(final ListItem<Entry<String, CommitDescription>> item) {
					Link<Void> link = new Link<Void>("commit") {

						@Override
						public void onClick() {
							Map.Entry<String, CommitDescription> entry = item.getModelObject();
							if (forBase) {
								setResponsePage(RequestComparePage.class, 
										paramsOf(getPullRequest(), entry.getKey(), newCommit, file, getConcernedComment()));
							} else {
								setResponsePage(RequestComparePage.class, 
										paramsOf(getPullRequest(), oldCommit, entry.getKey(), file, getConcernedComment()));
							}
						}
						
					};
					Map.Entry<String, CommitDescription> entry = item.getModelObject();
					String hash = GitUtils.abbreviateSHA(entry.getKey(), 7);
					String name = entry.getValue().getName();
					link.add(new Label("commit", hash));
					link.add(new Label("name", name).setVisible(name != null));
					if (entry.getValue().getSubject() != null)
						link.add(new Label("subject", entry.getValue().getSubject()));
					else
						link.add(new WebMarkupContainer("subject").setVisible(false));
					item.add(link);
				}
				
			});
		}

	}
	
	private abstract class ComparisonChoiceItem extends MenuItem {

		private final String oldName;
		
		private final String newName;
		
		ComparisonChoiceItem(String oldName, String newName) {
			this.oldName = oldName;
			this.newName = newName;
		}

		protected abstract void onSelect();

		@Override
		public Component newContent(String componentId) {
			Fragment fragment = new Fragment(componentId, "comparisonChoiceFrag", RequestComparePage.this);
			Link<Void> link = new Link<Void>("link") {

				@Override
				public void onClick() {
					onSelect();
				}
				
			};
			fragment.add(link);
			
			link.add(new Label("old", oldName));
			link.add(new Label("new", newName));
			
			return fragment;
		}
	}

	private PullRequestComment getConcernedComment() {
		return concernedCommentModel.getObject();
	}
}
