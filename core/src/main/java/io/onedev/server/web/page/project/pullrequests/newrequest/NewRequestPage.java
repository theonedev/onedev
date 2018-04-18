package io.onedev.server.web.page.project.pullrequests.newrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
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
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.manager.CodeCommentManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.ReviewInvitation;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CloseInfo;
import io.onedev.server.model.support.MarkPos;
import io.onedev.server.model.support.MergeStrategy;
import io.onedev.server.model.support.ProjectAndBranch;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.CommitIndexed;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.QualityCheckStatus;
import io.onedev.server.util.Verification;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.component.branchpicker.AffinalBranchPicker;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.commitlist.CommitListPanel;
import io.onedev.server.web.component.diff.revision.CommentSupport;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.component.link.BranchLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.requestreviewer.ReviewerListPanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.verification.RequiredVerificationsPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.RequestDetailPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.overview.RequestOverviewPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public class NewRequestPage extends ProjectPage implements CommentSupport {

	private static final String TABS_ID = "tabs";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private ProjectAndBranch target;
	
	private ProjectAndBranch source;
	
	private IModel<List<RevCommit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private Long commentId;
	
	private MarkPos mark;
	
	private String pathFilter;
	
	private String blameFile;
	
	private WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
	
	public static PageParameters paramsOf(Project project, ProjectAndBranch target, ProjectAndBranch source) {
		PageParameters params = paramsOf(project);
		params.set("target", target.toString());
		params.set("source", source.toString());
		return params;
	}

	public NewRequestPage(PageParameters params) {
		super(params);
		
		User currentUser = getLoginUser();
		if (currentUser == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);

		PullRequest prevRequest = null;
		String targetParam = params.get("target").toString();
		String sourceParam = params.get("source").toString();
		if (targetParam != null) {
			target = new ProjectAndBranch(targetParam);
		} else {
			prevRequest = OneDev.getInstance(PullRequestManager.class).findLatest(getProject(), getLoginUser());
			if (prevRequest != null && prevRequest.getTarget().getObjectId(false) != null) {
				target = prevRequest.getTarget();
			} else if (getProject().getForkedFrom() != null) {
				target = new ProjectAndBranch(getProject().getForkedFrom(), 
						getProject().getForkedFrom().getDefaultBranch());
			} else {
				target = new ProjectAndBranch(getProject(), getProject().getDefaultBranch());
			}
		}
		
		if (sourceParam != null) {
			source = new ProjectAndBranch(sourceParam);
		} else {
			if (prevRequest == null)
				prevRequest = OneDev.getInstance(PullRequestManager.class).findLatest(getProject(), getLoginUser());
			if (prevRequest != null && prevRequest.getSource().getObjectId(false) != null) {
				source = prevRequest.getSource();
			} else if (getProject().getForkedFrom() != null) {
				source = new ProjectAndBranch(getProject(), getProject().getDefaultBranch());
			} else {
				source = new ProjectAndBranch(getProject(), getProject().getDefaultBranch());
			}
		}

		AtomicReference<PullRequest> pullRequestRef = new AtomicReference<>(null);
		if (prevRequest != null && source.equals(prevRequest.getSource()) && target.equals(prevRequest.getTarget()) && prevRequest.isOpen())
			pullRequestRef.set(prevRequest);
		else
			pullRequestRef.set(OneDev.getInstance(PullRequestManager.class).findEffective(target, source));
		
		if (pullRequestRef.get() == null) {
			ObjectId baseCommitId = GitUtils.getMergeBase(
					target.getProject().getRepository(), target.getObjectId(), 
					source.getProject().getRepository(), source.getObjectId(), 
					GitUtils.branch2ref(source.getBranch()));
			if (baseCommitId != null) {
				PullRequest request = new PullRequest();
				pullRequestRef.set(request);
				request.setTarget(target);
				request.setSource(source);
				request.setSubmitter(currentUser);
				
				request.setBaseCommitHash(baseCommitId.name());
				request.setHeadCommitHash(source.getObjectName());
				if (request.getBaseCommitHash().equals(source.getObjectName())) {
					CloseInfo closeInfo = new CloseInfo();
					closeInfo.setCloseDate(new Date());
					closeInfo.setCloseStatus(CloseInfo.Status.MERGED);
					request.setCloseInfo(closeInfo);
				}
	
				PullRequestUpdate update = new PullRequestUpdate();
				request.addUpdate(update);
				update.setRequest(request);
				update.setHeadCommitHash(request.getHeadCommitHash());
				update.setMergeBaseCommitHash(request.getBaseCommitHash());
				
				if (request.getQualityCheckStatus().getAwaitingReviewers().isEmpty()) { 
					// this often means that we are merging from trunk to our own branch 
					request.setMergeStrategy(MergeStrategy.MERGE_IF_NECESSARY);
				} else {
					request.setMergeStrategy(MergeStrategy.ALWAYS_MERGE);
				}
			}
			requestModel = new LoadableDetachableModel<PullRequest>() {

				@Override
				protected PullRequest load() {
					if (pullRequestRef.get() != null) {
						pullRequestRef.get().setTarget(target);
						pullRequestRef.get().setSource(source);
						pullRequestRef.get().setSubmitter(SecurityUtils.getUser());
					}
					return pullRequestRef.get();
				}
				
			};
		} else {
			Long requestId = pullRequestRef.get().getId();
			requestModel = new LoadableDetachableModel<PullRequest>() {

				@Override
				protected PullRequest load() {
					return OneDev.getInstance(PullRequestManager.class).load(requestId);
				}
				
			};
		}
		requestModel.setObject(pullRequestRef.get());
		
		commitsModel = new LoadableDetachableModel<List<RevCommit>>() {

			@Override
			protected List<RevCommit> load() {
				PullRequest request = getPullRequest();
				List<RevCommit> commits = new ArrayList<>();
				try (RevWalk revWalk = new RevWalk(source.getProject().getRepository())) {
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
		
		add(new AffinalBranchPicker("target", target.getProjectId(), target.getBranch()) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project, String branch) {
				PageParameters params = paramsOf(project, new ProjectAndBranch(project, branch), source); 
				
				/*
				 * Use below code instead of calling setResponsePage() to make sure the dropdown is 
				 * closed while creating the new page as otherwise clicking other places in original page 
				 * while new page is loading will result in ComponentNotFound issue for the dropdown 
				 * component
				 */
				String url = RequestCycle.get().urlFor(NewRequestPage.class, params).toString();
				target.appendJavaScript(String.format("window.location.href='%s';", url));
			}
			
		});
		PageParameters params = CommitDetailPage.paramsOf(target.getProject(), target.getObjectName());
		Link<Void> targetCommitLink = new ViewStateAwarePageLink<Void>("targetCommitLink", CommitDetailPage.class, params);
		targetCommitLink.add(new Label("message", target.getCommit().getShortMessage()));
		add(targetCommitLink);
		
		add(new AffinalBranchPicker("source", source.getProjectId(), source.getBranch()) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project, String branch) {
				PageParameters params = paramsOf(getProject(), NewRequestPage.this.target,
						new ProjectAndBranch(project, branch)); 
				
				// Refer to comments in target branch picker for not using setResponsePage 
				String url = RequestCycle.get().urlFor(NewRequestPage.class, params).toString();
				target.appendJavaScript(String.format("window.location.href='%s';", url));
			}
			
		});
		params = CommitDetailPage.paramsOf(source.getProject(), source.getObjectName());
		Link<Void> sourceCommitLink = new ViewStateAwarePageLink<Void>("sourceCommitLink", CommitDetailPage.class, params);
		sourceCommitLink.add(new Label("message", source.getCommit().getShortMessage()));
		add(sourceCommitLink);
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				PageParameters params = paramsOf(source.getProject(), source, target); 
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
		return new CommitListPanel(TAB_PANEL_ID, projectModel, commitsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getPullRequest().isMerged());
			}
			
		}.setOutputMarkupId(true);
	}
	
	private RevisionDiffPanel newRevDiffPanel() {
		PullRequest request = getPullRequest();
		
		IModel<Project> projectModel = new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				Project project = source.getProject();
				project.cacheObjectId(source.getRevision(), 
						ObjectId.fromString(getPullRequest().getHeadCommitHash()));
				return project;
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
		 * it above when loading the project  
		 */
		RevisionDiffPanel diffPanel = new RevisionDiffPanel(TAB_PANEL_ID, projectModel, 
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
		fragment.add(new BranchLink("sourceBranch", getPullRequest().getSource(), null));
		fragment.add(new BranchLink("targetBranch", getPullRequest().getTarget(), null));
		fragment.add(new Link<Void>("swapBranches") {

			@Override
			public void onClick() {
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getProject(), getPullRequest().getSource(), getPullRequest().getTarget()));
			}
			
		});
		return fragment;
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}
	
	private Fragment newCanSendFrag() {
		Fragment fragment = new Fragment("status", "canSendFrag", this);
		Form<?> form = new Form<Void>("form");
		fragment.add(form);
		
		String autosaveKey = "autosave:newPullRequest:" + getProject().getId();
		
		form.add(new Button("send") {

			@Override
			public void onSubmit() {
				super.onSubmit();

				Dao dao = OneDev.getInstance(Dao.class);
				ProjectAndBranch target = getPullRequest().getTarget();
				ProjectAndBranch source = getPullRequest().getSource();
				if (!target.getObjectName().equals(getPullRequest().getTarget().getObjectName()) 
						|| !source.getObjectName().equals(getPullRequest().getSource().getObjectName())) {
					getSession().warn("Either target branch or source branch has new commits just now, please re-check.");
					setResponsePage(NewRequestPage.class, paramsOf(getProject(), target, source));
				} else {
					getPullRequest().setSource(source);
					getPullRequest().setTarget(target);
					for (ReviewInvitation invitation: getPullRequest().getReviewInvitations())
						invitation.setUser(dao.load(User.class, invitation.getUser().getId()));
					
					OneDev.getInstance(PullRequestManager.class).open(getPullRequest());
					
					PageParameters params = RequestOverviewPage.paramsOf(getPullRequest());
					params.add(BasePage.PARAM_AUTOSAVE_KEY_TO_CLEAR, autosaveKey);
					setResponsePage(RequestOverviewPage.class, params);
				}			
				
			}
		});
		
		WebMarkupContainer titleContainer = new WebMarkupContainer("title");
		form.add(titleContainer);
		final TextField<String> titleInput = new TextField<String>("title", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return getPullRequest().getTitle();
			}

			@Override
			public void setObject(String object) {
				getPullRequest().setTitle(object);
			}
			
		});
		titleInput.setRequired(true).setLabel(Model.of("Title"));
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
				return new ProjectAttachmentSupport(getProject(), getPullRequest().getUUID());
			}

			@Override
			protected Project getProject() {
				return NewRequestPage.this.getProject();
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
				send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
			}
			
		});
		
		form.add(mergeStrategyDropdown);
		
		form.add(new Label("mergeStrategyHelp", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getMergeStrategy().getDescription();
			}
			
		}) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PageDataChanged) {
					PageDataChanged payload = (PageDataChanged) event.getPayload();
					payload.getHandler().add(this);
				}
			}
			
		}.setOutputMarkupId(true));
		
		form.add(new WebMarkupContainer("immediateMergeNote") {
			
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PageDataChanged) {
					PageDataChanged payload = (PageDataChanged) event.getPayload();
					payload.getHandler().add(this);
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				
				QualityCheckStatus qualityCheckStatus = request.getQualityCheckStatus();
				boolean hasUnsuccessVerifications = false;
				for (Verification verification: qualityCheckStatus.getEffectiveVerifications().values()) {
					if (verification.getStatus() != Verification.Status.SUCCESS) {
						hasUnsuccessVerifications = true;
						break;
					}
				}
				setVisible(request.getMergeStrategy() != MergeStrategy.DO_NOT_MERGE 
						&& qualityCheckStatus.getAwaitingReviewers().isEmpty()
						&& !hasUnsuccessVerifications
						&& qualityCheckStatus.getAwaitingVerifications().isEmpty());
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		form.add(new ReviewerListPanel("reviewers", requestModel));

		form.add(new RequiredVerificationsPanel("verifications", requestModel));
		
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
	public MarkPos getMark() {
		return mark;
	}

	@Override
	public String getMarkUrl(MarkPos mark) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		state.mark = mark;
		state.leftSide = new ProjectAndBranch(source.getProject(), getPullRequest().getBaseCommitHash());
		state.rightSide = new ProjectAndBranch(source.getProject(), getPullRequest().getHeadCommitHash());
		state.pathFilter = pathFilter;
		state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
		state.whitespaceOption = whitespaceOption;
		state.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, RevisionComparePage.paramsOf(source.getProject(), state)).toString();
	}

	@Override
	public Collection<String> getWebSocketObservables() {
		Collection<String> regions = super.getWebSocketObservables();
		PullRequest request = getPullRequest();
		if (request != null) {
			regions.add(CommitIndexed.getWebSocketObservable(request.getBaseCommit().name()));
			regions.add(CommitIndexed.getWebSocketObservable(request.getHeadCommit().name()));
		}
		return regions;
	}

	@Override
	public void onMark(AjaxRequestTarget target, MarkPos mark) {
		this.mark = mark;
	}

	@Override
	public CodeComment getOpenComment() {
		if (commentId != null)
			return OneDev.getInstance(CodeCommentManager.class).load(commentId);
		else
			return null;
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, MarkPos mark) {
		this.commentId = null;
		this.mark = mark;
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		if (comment != null) {
			commentId = comment.getId();
			mark = comment.getMarkPos();
		} else {
			commentId = null;
		}
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		mark = comment.getMarkPos();
		state.commentId = comment.getId();
		state.leftSide = new ProjectAndBranch(source.getProject(), getPullRequest().getBaseCommitHash());
		state.rightSide = new ProjectAndBranch(source.getProject(), getPullRequest().getHeadCommitHash());
		state.pathFilter = pathFilter;
		state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
		state.whitespaceOption = whitespaceOption;
		state.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, RevisionComparePage.paramsOf(source.getProject(), state)).toString();
	}

}
