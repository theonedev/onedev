package com.gitplex.server.web.page.depot.pullrequest.newrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.model.support.CloseInfo;
import com.gitplex.server.model.support.CommentPos;
import com.gitplex.server.model.support.DepotAndBranch;
import com.gitplex.server.model.support.MergeStrategy;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.diff.WhitespaceOption;
import com.gitplex.server.web.component.branchpicker.AffinalBranchPicker;
import com.gitplex.server.web.component.comment.CommentInput;
import com.gitplex.server.web.component.comment.DepotAttachmentSupport;
import com.gitplex.server.web.component.commitlist.CommitListPanel;
import com.gitplex.server.web.component.diff.revision.CommentSupport;
import com.gitplex.server.web.component.diff.revision.RevisionDiffPanel;
import com.gitplex.server.web.component.link.BranchLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.markdown.AttachmentSupport;
import com.gitplex.server.web.component.requestreviewer.ReviewerListPanel;
import com.gitplex.server.web.component.tabbable.AjaxActionTab;
import com.gitplex.server.web.component.tabbable.Tab;
import com.gitplex.server.web.component.tabbable.Tabbable;
import com.gitplex.server.web.page.base.BasePage;
import com.gitplex.server.web.page.depot.DepotPage;
import com.gitplex.server.web.page.depot.NoBranchesPage;
import com.gitplex.server.web.page.depot.commit.CommitDetailPage;
import com.gitplex.server.web.page.depot.compare.RevisionComparePage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.gitplex.server.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.gitplex.server.web.page.security.LoginPage;
import com.gitplex.server.web.websocket.PullRequestChanged;
import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class NewRequestPage extends DepotPage implements CommentSupport {

	private static final String TABS_ID = "tabs";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private DepotAndBranch target;
	
	private DepotAndBranch source;
	
	private IModel<List<RevCommit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private Long commentId;
	
	private CommentPos mark;
	
	private String pathFilter;
	
	private String blameFile;
	
	private WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
	
	public static PageParameters paramsOf(Depot depot, DepotAndBranch target, DepotAndBranch source) {
		PageParameters params = paramsOf(depot);
		params.set("target", target.toString());
		params.set("source", source.toString());
		return params;
	}

	public NewRequestPage(PageParameters params) {
		super(params);
		
		if (getDepot().getDefaultBranch() == null) 
			throw new RestartResponseException(NoBranchesPage.class, paramsOf(getDepot()));

		Account currentUser = getLoginUser();
		if (currentUser == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);

		PullRequest prevRequest = null;
		String targetParam = params.get("target").toString();
		String sourceParam = params.get("source").toString();
		if (targetParam != null) {
			target = new DepotAndBranch(targetParam);
		} else {
			prevRequest = GitPlex.getInstance(PullRequestManager.class).findLatest(getDepot(), getLoginUser());
			if (prevRequest != null && prevRequest.getTarget().getObjectId(false) != null) {
				target = prevRequest.getTarget();
			} else if (getDepot().getForkedFrom() != null) {
				target = new DepotAndBranch(getDepot().getForkedFrom(), 
						getDepot().getForkedFrom().getDefaultBranch());
			} else {
				target = new DepotAndBranch(getDepot(), getDepot().getDefaultBranch());
			}
		}
		
		if (sourceParam != null) {
			source = new DepotAndBranch(sourceParam);
		} else {
			if (prevRequest == null)
				prevRequest = GitPlex.getInstance(PullRequestManager.class).findLatest(getDepot(), getLoginUser());
			if (prevRequest != null && prevRequest.getSource().getObjectId(false) != null) {
				source = prevRequest.getSource();
			} else if (getDepot().getForkedFrom() != null) {
				source = new DepotAndBranch(getDepot(), getDepot().getDefaultBranch());
			} else {
				source = new DepotAndBranch(getDepot(), getDepot().getDefaultBranch());
			}
		}

		AtomicReference<PullRequest> pullRequestRef = new AtomicReference<>(null);
		if (prevRequest != null && source.equals(prevRequest.getSource()) && target.equals(prevRequest.getTarget()) && prevRequest.isOpen())
			pullRequestRef.set(prevRequest);
		else
			pullRequestRef.set(GitPlex.getInstance(PullRequestManager.class).findEffective(target, source));
		
		if (pullRequestRef.get() == null) {
			ObjectId baseCommitId = GitUtils.getMergeBase(
					target.getDepot().getRepository(), target.getObjectId(), 
					source.getDepot().getRepository(), source.getObjectId(), 
					GitUtils.branch2ref(source.getBranch()));
			if (baseCommitId != null) {
				pullRequestRef.set(new PullRequest());
				pullRequestRef.get().setTarget(target);
				pullRequestRef.get().setSource(source);
				pullRequestRef.get().setSubmitter(currentUser);
				
				pullRequestRef.get().setMergeStrategy(MergeStrategy.ALWAYS_MERGE);
				
				pullRequestRef.get().setBaseCommitHash(baseCommitId.name());
				if (pullRequestRef.get().getBaseCommitHash().equals(source.getObjectName())) {
					CloseInfo closeInfo = new CloseInfo();
					closeInfo.setCloseDate(new Date());
					closeInfo.setCloseStatus(CloseInfo.Status.MERGED);
					pullRequestRef.get().setCloseInfo(closeInfo);
				}
	
				PullRequestUpdate update = new PullRequestUpdate();
				pullRequestRef.get().addUpdate(update);
				update.setRequest(pullRequestRef.get());
				update.setHeadCommitHash(source.getObjectName());
				update.setMergeBaseCommitHash(pullRequestRef.get().getBaseCommitHash());
			}
			requestModel = new LoadableDetachableModel<PullRequest>() {

				@Override
				protected PullRequest load() {
					if (pullRequestRef.get() != null) {
						pullRequestRef.get().setTarget(target);
						pullRequestRef.get().setSource(source);
						pullRequestRef.get().setSubmitter(SecurityUtils.getAccount());
					}
					return pullRequestRef.get();
				}
				
			};
		} else {
			Long requestId = pullRequestRef.get().getId();
			requestModel = new LoadableDetachableModel<PullRequest>() {

				@Override
				protected PullRequest load() {
					return GitPlex.getInstance(PullRequestManager.class).load(requestId);
				}
				
			};
		}
		requestModel.setObject(pullRequestRef.get());
		
		commitsModel = new LoadableDetachableModel<List<RevCommit>>() {

			@Override
			protected List<RevCommit> load() {
				PullRequest request = getPullRequest();
				List<RevCommit> commits = new ArrayList<>();
				try (RevWalk revWalk = new RevWalk(source.getDepot().getRepository())) {
					ObjectId headId = ObjectId.fromString(request.getHeadCommitHash());
					revWalk.markStart(revWalk.parseCommit(headId));
					ObjectId baseId = ObjectId.fromString(request.getBaseCommitHash());
					revWalk.markUninteresting(revWalk.parseCommit(baseId));
					revWalk.forEach(c->commits.add(c));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return commits;
			}
			
		};
		
	}
	
	private PullRequest getPullRequest() {
		return requestModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new AffinalBranchPicker("target", target.getDepotId(), target.getBranch()) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String branch) {
				PageParameters params = paramsOf(depot, new DepotAndBranch(depot, branch), source); 
				setResponsePage(NewRequestPage.class, params);
			}
			
		});
		PageParameters params = CommitDetailPage.paramsOf(target.getDepot(), target.getObjectName());
		Link<Void> targetCommitLink = new ViewStateAwarePageLink<Void>("targetCommitLink", CommitDetailPage.class, params);
		targetCommitLink.add(new Label("message", target.getCommit().getShortMessage()));
		add(targetCommitLink);
		
		add(new AffinalBranchPicker("source", source.getDepotId(), source.getBranch()) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String branch) {
				PageParameters params = paramsOf(getDepot(), NewRequestPage.this.target,
						new DepotAndBranch(depot, branch)); 
				setResponsePage(NewRequestPage.class, params);
			}
			
		});
		params = CommitDetailPage.paramsOf(source.getDepot(), source.getObjectName());
		Link<Void> sourceCommitLink = new ViewStateAwarePageLink<Void>("sourceCommitLink", CommitDetailPage.class, params);
		sourceCommitLink.add(new Label("message", source.getCommit().getShortMessage()));
		add(sourceCommitLink);
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				PageParameters params = paramsOf(source.getDepot(), source, target); 
				setResponsePage(NewRequestPage.class, params);
			}
			
		});
		
		Fragment fragment;
		PullRequest request = getPullRequest();
		if (request == null) {
			fragment = newUnrelatedHistoryFrag();
		} else if (request.getId() != null && (request.isOpen() || !request.isMergeIntoTarget())) {
			fragment = newEffectiveFrag();
		} else if (request.getSource().equals(request.getTarget())) {
			fragment = newSameBranchFrag();
		} else if (request.isMerged()) {
			fragment = newAcceptedFrag();
		} else { 
			fragment = newCanSendFrag();
		} 
		add(fragment);

		if (getPullRequest() != null) {
			List<Tab> tabs = new ArrayList<>();
			
			tabs.add(new AjaxActionTab(Model.of("Commits")) {
				
				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component panel = newCommitsPanel();
					getPage().replace(panel);
					target.add(panel);
				}
				
			});

			tabs.add(new AjaxActionTab(Model.of("Files")) {
				
				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component panel = newRevDiffPanel();
					getPage().replace(panel);
					target.add(panel);
				}
				
			});

			add(new Tabbable(TABS_ID, tabs) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getPullRequest().isMerged());
				}
				
			});
			
			add(newCommitsPanel());
		} else {
			add(new WebMarkupContainer(TABS_ID).setVisible(false));
			add(new WebMarkupContainer(TAB_PANEL_ID).setVisible(false));
		}
	}
	
	private Component newCommitsPanel() {
		return new CommitListPanel(TAB_PANEL_ID, depotModel, commitsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getPullRequest().isMerged());
			}
			
		}.setOutputMarkupId(true);
	}
	
	private RevisionDiffPanel newRevDiffPanel() {
		PullRequest request = getPullRequest();
		
		IModel<Depot> depotModel = new LoadableDetachableModel<Depot>() {

			@Override
			protected Depot load() {
				Depot depot = source.getDepot();
				depot.cacheObjectId(source.getRevision(), 
						ObjectId.fromString(getPullRequest().getHeadCommitHash()));
				return depot;
			}
			
		};
		
		IModel<String> blameModel = new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return blameFile;
			}

			@Override
			public void setObject(String object) {
				blameFile = object;
			}
			
		};
		IModel<String> pathFilterModel = new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return pathFilter;
			}

			@Override
			public void setObject(String object) {
				pathFilter = object;
			}
			
		};
		IModel<WhitespaceOption> whitespaceOptionModel = new IModel<WhitespaceOption>() {

			@Override
			public void detach() {
			}

			@Override
			public WhitespaceOption getObject() {
				return whitespaceOption;
			}

			@Override
			public void setObject(WhitespaceOption object) {
				whitespaceOption = object;
			}
			
		};

		/*
		 * we are passing source revision here instead of head commit hash of latest update
		 * as we want to preserve the branch name in case they are useful at some point 
		 * later. Also it is guaranteed to be resolved to the same commit has as we've cached
		 * it above when loading the depot  
		 */
		RevisionDiffPanel diffPanel = new RevisionDiffPanel(TAB_PANEL_ID, depotModel, 
				new Model<PullRequest>(null), request.getBaseCommitHash(), 
				source.getRevision(), pathFilterModel, whitespaceOptionModel, blameModel, this) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getPullRequest().isMerged());
			}
			
		};
		diffPanel.setOutputMarkupId(true);
		return diffPanel;
	}

	private Fragment newEffectiveFrag() {
		Fragment fragment = new Fragment("status", "effectiveFrag", this);

		fragment.add(new Label("description", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (requestModel.getObject().isOpen())
					return "This change is already opened for merge by a pull request";
				else 
					return "This change is squashed/rebased onto base branch via a pull request";
			}
			
		}));
		
		fragment.add(new Link<Void>("link") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						PullRequest request = getPullRequest();
						return "#" + request.getNumber() + " " + request.getTitle();
					}
					
				}));
			}

			@Override
			public void onClick() {
				PageParameters params = RequestDetailPage.paramsOf(getPullRequest());
				setResponsePage(RequestOverviewPage.class, params);
			}
			
		});
		
		return fragment;
	}
	
	private Fragment newSameBranchFrag() {
		return new Fragment("status", "sameBranchFrag", this);
	}
	
	private Fragment newUnrelatedHistoryFrag() {
		return new Fragment("status", "unrelatedHistoryFrag", this);
	}
	
	private Fragment newAcceptedFrag() {
		Fragment fragment = new Fragment("status", "mergedFrag", this);
		fragment.add(new BranchLink("sourceBranch", getPullRequest().getSource()));
		fragment.add(new BranchLink("targetBranch", getPullRequest().getTarget()));
		fragment.add(new Link<Void>("swapBranches") {

			@Override
			public void onClick() {
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getDepot(), getPullRequest().getSource(), getPullRequest().getTarget()));
			}
			
		});
		return fragment;
	}
	
	private Fragment newCanSendFrag() {
		Fragment fragment = new Fragment("status", "canSendFrag", this);
		Form<?> form = new Form<Void>("form");
		fragment.add(form);
		
		String autosaveKey = "autosave:newPullRequest:" + getDepot().getId();
		
		form.add(new Button("send") {

			@Override
			public void onSubmit() {
				super.onSubmit();

				Dao dao = GitPlex.getInstance(Dao.class);
				DepotAndBranch target = getPullRequest().getTarget();
				DepotAndBranch source = getPullRequest().getSource();
				if (!target.getObjectName().equals(getPullRequest().getTarget().getObjectName()) 
						|| !source.getObjectName().equals(getPullRequest().getSource().getObjectName())) {
					getSession().warn("Either target branch or source branch has new commits just now, please re-check.");
					setResponsePage(NewRequestPage.class, paramsOf(getDepot(), target, source));
				} else {
					getPullRequest().setSource(source);
					getPullRequest().setTarget(target);
					for (ReviewInvitation invitation: getPullRequest().getReviewInvitations())
						invitation.setUser(dao.load(Account.class, invitation.getUser().getId()));
					
					GitPlex.getInstance(PullRequestManager.class).open(getPullRequest());
					
					PageParameters params = RequestOverviewPage.paramsOf(getPullRequest());
					params.add(BasePage.PARAM_AUTOSAVE_KEY_TO_CLEAR, autosaveKey);
					setResponsePage(RequestOverviewPage.class, params);
				}			
				
			}
		});
		
		form.add(new WebMarkupContainer("immediateMergeNote") {
			
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged payload = (PullRequestChanged) event.getPayload();
					payload.getPartialPageRequestHandler().add(this);
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				
				setVisible(request.getMergeStrategy() != MergeStrategy.DO_NOT_MERGE 
						&& request.getReviewStatus().getAwaitingReviewers().isEmpty());
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		WebMarkupContainer titleContainer = new WebMarkupContainer("title");
		form.add(titleContainer);
		final TextField<String> titleInput = new TextField<String>("title", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				if (request.getTitle() == null) {
					List<RevCommit> commits = commitsModel.getObject();
					Preconditions.checkState(!commits.isEmpty());
					if (commits.size() == 1) {
						request.setTitle(commits.get(0).getShortMessage());
					} else if (!request.getSourceBranch().equals("master") 
							&& !request.getSourceBranch().equals(request.getTargetBranch())) {
						request.setTitle(request.getSourceBranch());
					}
				}
				return request.getTitle();
			}

			@Override
			public void setObject(String object) {
				getPullRequest().setTitle(object);
			}
			
		});
		titleInput.setRequired(true);
		titleContainer.add(titleInput);
		
		titleContainer.add(new NotificationPanel("feedback", titleInput));
		
		titleContainer.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !titleInput.isValid()?" has-error":"";
			}
			
		}));

		form.add(new CommentInput("comment", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return getPullRequest().getDescription();
			}

			@Override
			public void setObject(String object) {
				getPullRequest().setDescription(object);
			}
			
		}, false) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new DepotAttachmentSupport(getDepot(), getPullRequest().getUUID());
			}

			@Override
			protected Depot getDepot() {
				return NewRequestPage.this.getDepot();
			}
			
			@Override
			protected String getAutosaveKey() {
				return autosaveKey;
			}
			
		});

		IModel<MergeStrategy> mergeStrategyModel = new IModel<MergeStrategy>() {

			@Override
			public void detach() {
			}

			@Override
			public MergeStrategy getObject() {
				return getPullRequest().getMergeStrategy();
			}

			@Override
			public void setObject(MergeStrategy object) {
				getPullRequest().setMergeStrategy(object);
			}
			
		};
		
		List<MergeStrategy> mergeStrategies = Arrays.asList(MergeStrategy.values());
		DropDownChoice<MergeStrategy> mergeStrategyDropdown = 
				new DropDownChoice<MergeStrategy>("mergeStrategy", mergeStrategyModel, mergeStrategies);

		mergeStrategyDropdown.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				send(getPage(), Broadcast.BREADTH, new PullRequestChanged(target));								
			}
			
		});
		
		form.add(mergeStrategyDropdown);
		
		form.add(new Label("mergeStrategyHelp", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getMergeStrategy().getDescription();
			}
			
		}));
		
		form.add(new ReviewerListPanel("reviewers", requestModel));
		
		return fragment;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new NewRequestResourceReference()));
	}

	@Override
	protected void onDetach() {
		commitsModel.detach();
		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RequestListPage.class, paramsOf(depot));
	}

	@Override
	public CommentPos getMark() {
		return mark;
	}

	@Override
	public String getMarkUrl(CommentPos mark) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		state.mark = mark;
		state.leftSide = new DepotAndBranch(source.getDepot(), getPullRequest().getBaseCommitHash());
		state.rightSide = new DepotAndBranch(source.getDepot(), getPullRequest().getHeadCommitHash());
		state.pathFilter = pathFilter;
		state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
		state.whitespaceOption = whitespaceOption;
		state.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, RevisionComparePage.paramsOf(source.getDepot(), state)).toString();
	}

	@Override
	public CodeComment getOpenComment() {
		if (commentId != null)
			return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
		else
			return null;
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		if (comment != null) {
			commentId = comment.getId();
			mark = comment.getCommentPos();
		} else {
			commentId = null;
		}
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		mark = comment.getCommentPos();
		state.commentId = comment.getId();
		state.leftSide = new DepotAndBranch(source.getDepot(), getPullRequest().getBaseCommitHash());
		state.rightSide = new DepotAndBranch(source.getDepot(), getPullRequest().getHeadCommitHash());
		state.pathFilter = pathFilter;
		state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
		state.whitespaceOption = whitespaceOption;
		state.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, RevisionComparePage.paramsOf(source.getDepot(), state)).toString();
	}

	@Override
	public void onMark(AjaxRequestTarget target, CommentPos mark) {
		this.mark = mark;
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, CommentPos mark) {
		this.commentId = null;
		this.mark = mark;
	}

	@Override
	public String getAnchor() {
		return null;
	}

}
