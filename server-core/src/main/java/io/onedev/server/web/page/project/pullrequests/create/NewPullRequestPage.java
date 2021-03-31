package io.onedev.server.web.page.project.pullrequests.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.feedback.FencedFeedbackPanel;
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
import org.joda.time.DateTime;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.code.CodeProblemContribution;
import io.onedev.server.code.LineCoverageContribution;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.Mark;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.search.commit.Revision;
import io.onedev.server.search.commit.RevisionCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.ajaxlistener.DisableGlobalLoadingIndicatorListener;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.branch.picker.AffinalBranchPicker;
import io.onedev.server.web.component.commit.list.CommitListPanel;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.component.pullrequest.assignment.AssignmentListPanel;
import io.onedev.server.web.component.pullrequest.review.ReviewListPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.page.simple.security.LoginPage;
import io.onedev.server.web.util.ProjectAttachmentSupport;
import io.onedev.server.web.util.RevisionDiff;

@SuppressWarnings("serial")
public class NewPullRequestPage extends ProjectPage implements RevisionDiff.AnnotationSupport {

	private static final String TABS_ID = "tabs";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private ProjectAndBranch target;
	
	private ProjectAndBranch source;
	
	private final IModel<PullRequest> requestModel;
	
	private final IModel<Collection<CodeComment>> commentsModel = 
			new LoadableDetachableModel<Collection<CodeComment>>() {

		@Override
		protected Collection<CodeComment> load() {
			CodeCommentManager manager = OneDev.getInstance(CodeCommentManager.class);
			return manager.query(projectModel.getObject(), 
					getPullRequest().getBaseCommit(), source.getObjectId());
		}
		
	};
	
	private Long commentId;
	
	private Mark mark;
	
	private String pathFilter;
	
	private String blameFile;
	
	private WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
	
	public static PageParameters paramsOf(Project project, ProjectAndBranch target, ProjectAndBranch source) {
		PageParameters params = paramsOf(project);
		if (target.getBranch() != null)
			params.set("target", target.toString());
		else
			params.set("target", target.getProjectId());
		if (source.getBranch() != null)
			params.set("source", source.toString());
		else
			params.set("source", source.getProjectId());
		return params;
	}

	private String suggestSourceBranch() {
		User user = getLoginUser();
		List<Pair<String, Integer>> branchUpdates = new ArrayList<>(); 
		for (RefInfo refInfo: getProject().getBranchRefInfos()) {
			RevCommit commit = (RevCommit) refInfo.getPeeledObj();
			if (commit.getAuthorIdent().getEmailAddress().equals(user.getEmail()))
				branchUpdates.add(new Pair<>(GitUtils.ref2branch(refInfo.getRef().getName()), commit.getCommitTime()));
		}
		branchUpdates.sort(Comparator.comparing(Pair::getSecond));
		if (!branchUpdates.isEmpty())
			return branchUpdates.get(branchUpdates.size()-1).getFirst();
		else
			return getProject().getDefaultBranch();
	}
	
	public NewPullRequestPage(PageParameters params) {
		super(params);
		
		User currentUser = getLoginUser();
		if (currentUser == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);

		String targetParam = params.get("target").toString();
		String sourceParam = params.get("source").toString();
		String suggestedSourceBranch = null;
		if (targetParam != null) {
			target = new ProjectAndBranch(targetParam);
		} else {
			suggestedSourceBranch = suggestSourceBranch();
			if (suggestedSourceBranch != null) {
				if (!suggestedSourceBranch.equals(getProject().getDefaultBranch())) {
					target = new ProjectAndBranch(getProject(), getProject().getDefaultBranch());
	 			} else if (getProject().getForkedFrom() != null && SecurityUtils.canReadCode(getProject().getForkedFrom())) {
					target = new ProjectAndBranch(getProject().getForkedFrom(), 
							getProject().getForkedFrom().getDefaultBranch());
				} else {
					target = new ProjectAndBranch(getProject(), getProject().getDefaultBranch());
				}
			} else {
				target = new ProjectAndBranch(getProject(), null);
			}
		}
		
		if (sourceParam != null) {
			source = new ProjectAndBranch(sourceParam);
		} else {
			if (suggestedSourceBranch == null) 
				suggestedSourceBranch = suggestSourceBranch();
			source = new ProjectAndBranch(getProject(), suggestedSourceBranch);
		}

		AtomicReference<PullRequest> pullRequestRef = new AtomicReference<>(null);
		PullRequest prevRequest = OneDev.getInstance(PullRequestManager.class).findLatest(target.getProject());
		if (prevRequest != null && source.equals(prevRequest.getSource()) && target.equals(prevRequest.getTarget()) && prevRequest.isOpen())
			pullRequestRef.set(prevRequest);
		else if (target.getBranch() != null && source.getBranch() != null)
			pullRequestRef.set(OneDev.getInstance(PullRequestManager.class).findEffective(target, source));
		
		if (pullRequestRef.get() == null) {
			ObjectId baseCommitId;
			if (target.getBranch() != null && source.getBranch() != null) {
				baseCommitId = GitUtils.getMergeBase(
						target.getProject().getRepository(), target.getObjectId(), 
						source.getProject().getRepository(), source.getObjectId());
			} else {
				baseCommitId = null;
			}
			if (baseCommitId != null) {
				PullRequest request = new PullRequest();
				request.setTitle(StringUtils.capitalize(source.getBranch().replace('-', ' ').replace('_', ' ').toLowerCase()));
				pullRequestRef.set(request);
				request.setTarget(target);
				request.setSource(source);
				request.setSubmitter(currentUser);
				
				request.setBaseCommitHash(baseCommitId.name());
				if (request.getBaseCommitHash().equals(source.getObjectName())) {
					CloseInfo closeInfo = new CloseInfo();
					closeInfo.setDate(new Date());
					closeInfo.setStatus(CloseInfo.Status.MERGED);
					request.setCloseInfo(closeInfo);
				}
	
				PullRequestUpdate update = new PullRequestUpdate();
				update.setDate(new DateTime(request.getSubmitDate()).plusSeconds(1).toDate());
				request.getUpdates().add(update);
				request.setUpdates(request.getUpdates());
				update.setRequest(request);
				update.setHeadCommitHash(source.getObjectName());
				update.setTargetHeadCommitHash(request.getTarget().getObjectName());

				OneDev.getInstance(PullRequestManager.class).checkReviews(request, Lists.newArrayList());

				if (SecurityUtils.canWriteCode(target.getProject())) {
					PullRequestAssignment assignment = new PullRequestAssignment();
					assignment.setRequest(request);
					assignment.setUser(SecurityUtils.getUser());
					request.getAssignments().add(assignment);
				} else {
					List<User> codeWriters = new ArrayList<>(
							SecurityUtils.getAuthorizedUsers(target.getProject(), new WriteCode()));
					OneDev.getInstance(CommitInfoManager.class).sortUsersByContribution(
							codeWriters, target.getProject(), update.getChangedFiles());
					if (!codeWriters.isEmpty()) {
						PullRequestAssignment assignment = new PullRequestAssignment();
						assignment.setRequest(request);
						assignment.setUser(codeWriters.iterator().next());
						request.getAssignments().add(assignment);
					}
				}
				request.setMergeStrategy(MergeStrategy.CREATE_MERGE_COMMIT);
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
		
	}
	
	private PullRequest getPullRequest() {
		return requestModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AffinalBranchPicker("target", target.getProjectId(), target.getBranch()) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project, String branch) {
				PageParameters params = paramsOf(getProject(), new ProjectAndBranch(project, branch), source); 
				
				/*
				 * Use below code instead of calling setResponsePage() to make sure the dropdown is 
				 * closed while creating the new page as otherwise clicking other places in original page 
				 * while new page is loading will result in ComponentNotFound issue for the dropdown 
				 * component
				 */
				String url = RequestCycle.get().urlFor(NewPullRequestPage.class, params).toString();
				target.appendJavaScript(String.format("window.location.href='%s';", url));
			}
			
		});
		
		if (target.getBranch() != null) {
			PageParameters params = CommitDetailPage.paramsOf(target.getProject(), target.getObjectName());
			Link<Void> targetCommitLink = new ViewStateAwarePageLink<Void>("targetCommitLink", CommitDetailPage.class, params);
			targetCommitLink.add(new Label("message", target.getCommit().getShortMessage()));
			add(targetCommitLink);
		} else {
			WebMarkupContainer targetCommitLink = new WebMarkupContainer("targetCommitLink");
			targetCommitLink.add(new WebMarkupContainer("message"));
			targetCommitLink.setVisible(false);
			add(targetCommitLink);
		}
		
		add(new AffinalBranchPicker("source", source.getProjectId(), source.getBranch()) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project, String branch) {
				PageParameters params = paramsOf(getProject(), NewPullRequestPage.this.target,
						new ProjectAndBranch(project, branch)); 
				
				// Refer to comments in target branch picker for not using setResponsePage 
				String url = RequestCycle.get().urlFor(NewPullRequestPage.class, params).toString();
				target.appendJavaScript(String.format("window.location.href='%s';", url));
			}
			
		});
		
		if (source.getBranch() != null) {
			PageParameters params = CommitDetailPage.paramsOf(source.getProject(), source.getObjectName());
			Link<Void> sourceCommitLink = new ViewStateAwarePageLink<Void>("sourceCommitLink", CommitDetailPage.class, params);
			sourceCommitLink.add(new Label("message", source.getCommit().getShortMessage()));
			add(sourceCommitLink);
		} else {
			WebMarkupContainer sourceCommitLink = new WebMarkupContainer("sourceCommitLink");
			sourceCommitLink.add(new WebMarkupContainer("message"));
			sourceCommitLink.setVisible(false);
			add(sourceCommitLink);
		}
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				PageParameters params = paramsOf(getProject(), source, target); 
				setResponsePage(NewPullRequestPage.class, params);
			}
			
		});
		
		Fragment fragment;
		PullRequest request = getPullRequest();
		if (target.getBranch() == null || source.getBranch() == null) 
			fragment = newBranchNotSpecifiedFrag();
		else if (request == null) 
			fragment = newUnrelatedHistoryFrag();
		else if (request.getId() != null && (request.isOpen() || !request.isMergedIntoTarget())) 
			fragment = newEffectiveFrag();
		else if (request.getSource().equals(request.getTarget())) 
			fragment = newSameBranchFrag();
		else if (request.isMerged()) 
			fragment = newAcceptedFrag();
		else 
			fragment = newCanSendFrag();
		add(fragment);

		if (getPullRequest() != null) {
			List<Tab> tabs = new ArrayList<>();
			
			tabs.add(new AjaxActionTab(Model.of("Commits")) {
				
				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component panel = newCommitsPanel();
					getPage().replace(panel);
					target.add(panel);
					resizeWindow(target);
				}
				
			});

			tabs.add(new AjaxActionTab(Model.of("File Changes")) {
				
				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component panel = newRevisionDiffPanel();
					getPage().replace(panel);
					target.add(panel);
					resizeWindow(target);
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
		
		setOutputMarkupId(true);
	}
	
	private Component newCommitsPanel() {
		return new CommitListPanel(TAB_PANEL_ID, Model.of((String)null)) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!NewPullRequestPage.this.getPullRequest().isMerged());
			}

			@Override
			protected CommitQuery getBaseQuery() {
				PullRequest request = NewPullRequestPage.this.getPullRequest();
				List<Revision> revisions = new ArrayList<>();
				revisions.add(new Revision(request.getBaseCommitHash(), Revision.Scope.SINCE));
				revisions.add(new Revision(request.getLatestUpdate().getHeadCommitHash(), Revision.Scope.UNTIL));
				return new CommitQuery(Lists.newArrayList(new RevisionCriteria(revisions)));
			}

			@Override
			protected Project getProject() {
				return NewPullRequestPage.this.getPullRequest().getSourceProject();
			}

		}.setOutputMarkupId(true);
	}
	
	private RevisionDiffPanel newRevisionDiffPanel() {
		PullRequest request = getPullRequest();
		
		IModel<Project> projectModel = new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				Project project = source.getProject();
				project.cacheObjectId(source.getRevision(), 
						ObjectId.fromString(getPullRequest().getLatestUpdate().getHeadCommitHash()));
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
		RevisionDiffPanel diffPanel = new RevisionDiffPanel(TAB_PANEL_ID, request.getBaseCommitHash(), 
				source.getRevision(), pathFilterModel, whitespaceOptionModel, blameModel, this) {

			@Override
			protected Project getProject() {
				return projectModel.getObject();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!NewPullRequestPage.this.getPullRequest().isMerged());
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
					return "This change is already opened for merge by ";
				else 
					return "This change is squashed/rebased onto base branch via ";
			}
			
		}).setEscapeModelStrings(false));
		
		fragment.add(new Link<Void>("link") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return "pull request #" + getPullRequest().getNumber();
					}
					
				}));
			}

			@Override
			public void onClick() {
				PageParameters params = PullRequestDetailPage.paramsOf(getPullRequest());
				setResponsePage(PullRequestActivitiesPage.class, params);
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
	
	private Fragment newBranchNotSpecifiedFrag() {
		return new Fragment("status", "branchNotSpecifiedFrag", this);
	}
	
	private Fragment newAcceptedFrag() {
		Fragment fragment = new Fragment("status", "mergedFrag", this);
		fragment.add(new BranchLink("sourceBranch", getPullRequest().getSource()));
		fragment.add(new BranchLink("targetBranch", getPullRequest().getTarget()));
		fragment.add(new Link<Void>("swapBranches") {

			@Override
			public void onClick() {
				setResponsePage(
						NewPullRequestPage.class, 
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
					setResponsePage(NewPullRequestPage.class, paramsOf(getProject(), target, source));
				} else {
					getPullRequest().setSource(source);
					getPullRequest().setTarget(target);
					for (PullRequestReview review: getPullRequest().getReviews())
						review.setUser(dao.load(User.class, review.getUser().getId()));
					
					OneDev.getInstance(PullRequestManager.class).open(getPullRequest());
					
					setResponsePage(PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(getPullRequest()));
				}			
				
			}
		});
		
		TextField<String> titleInput = new TextField<String>("title", new IModel<String>() {

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
		titleInput.add(new ReferenceInputBehavior(false) {
			
			@Override
			protected Project getProject() {
				return NewPullRequestPage.this.getProject();
			}
			
		});
		titleInput.setRequired(true).setLabel(Model.of("Title"));
		
		form.add(new FencedFeedbackPanel("titleFeedback", titleInput));
		
		titleInput.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !titleInput.isValid()?" is-invalid":"";
			}
			
		}));
		
		form.add(titleInput);

		form.add(new CommentInput("comment", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return NewPullRequestPage.this.getPullRequest().getDescription();
			}

			@Override
			public void setObject(String object) {
				NewPullRequestPage.this.getPullRequest().setDescription(object);
			}
			
		}, false) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(target.getProject(), 
						NewPullRequestPage.this.getPullRequest().getUUID(), 
						SecurityUtils.canManagePullRequests(target.getProject()));
			}

			@Override
			protected Project getProject() {
				return target.getProject();
			}
			
		});

		form.add(newMergeStrategyContainer());
		form.add(new ReviewListPanel("reviewers") {

			@Override
			protected PullRequest getPullRequest() {
				return NewPullRequestPage.this.getPullRequest();
			}
			
		});
		form.add(new AssignmentListPanel("assignees") {

			@Override
			protected PullRequest getPullRequest() {
				return NewPullRequestPage.this.getPullRequest();
			}
			
		});
		
		return fragment;
	}
	
	private Component newMergeStrategyContainer() {
		WebMarkupContainer container = new WebMarkupContainer("mergeStrategy");
		
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
				new DropDownChoice<MergeStrategy>("select", mergeStrategyModel, mergeStrategies);

		mergeStrategyDropdown.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				Component newContainer = newMergeStrategyContainer();
				container.replaceWith(newContainer);
				target.add(newContainer);
			}
			
		});
		
		container.add(mergeStrategyDropdown);
		
		container.add(new Label("help", getPullRequest().getMergeStrategy().getDescription()));
		
		container.add(new AjaxLazyLoadPanel("status") {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new DisableGlobalLoadingIndicatorListener());
			}

			@Override
			public Component getLazyLoadComponent(String componentId) {
				PullRequest request = getPullRequest();
				MergePreview mergePreview = new MergePreview(request.getTarget().getObjectName(), 
						request.getLatestUpdate().getHeadCommitHash(), request.getMergeStrategy(), null);
				ObjectId merged = mergePreview.getMergeStrategy().merge(request, "Pull request merge preview");
				if (merged != null)
					mergePreview.setMergeCommitHash(merged.name());
				request.setLastMergePreview(mergePreview);
				
				if (merged != null) {
					String html = String.format("<svg class='icon mt-n1 mr-1'><use xlink:href='%s'/></svg> Able to merge without conflicts", 
							SpriteImage.getVersionedHref("tick-circle-o"));
					Component result = new Label(componentId, html);
					result.add(AttributeAppender.append("class", "no-conflict"));
					result.setEscapeModelStrings(false);
					return result;
				} else { 
					String html = String.format("<svg class='icon mt-n1 mr-1'><use xlink:href='%s'/></svg> There are merge conflicts. "
							+ "You can still create the pull request though", SpriteImage.getVersionedHref("warning-o"));
					Component result = new Label(componentId, html);
					result.add(AttributeAppender.append("class", "conflict"));
					result.setEscapeModelStrings(false);
					return result;
				}
			}

			@Override
			public Component getLoadingComponent(String markupId) {
				String html = String.format("<svg class='icon spin mt-n1 mr-1'><use xlink:href='%s'/></svg> Calculating merge preview...", 
						SpriteImage.getVersionedHref("loading"));
				Component component = new Label(markupId, html);
				component.add(AttributeAppender.append("class", "calculating"));
				component.setEscapeModelStrings(false);
				return component;
			}
			
		});
		
		container.setOutputMarkupId(true);		
		
		return container;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new NewPullRequestCssResourceReference()));
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		commentsModel.detach();
		
		super.onDetach();
	}

	@Override
	public Mark getMark() {
		return mark;
	}

	@Override
	public String getMarkUrl(Mark mark) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		state.mark = mark;
		state.leftSide = new ProjectAndBranch(source.getProject(), getPullRequest().getBaseCommitHash());
		state.rightSide = new ProjectAndBranch(source.getProject(), getPullRequest().getLatestUpdate().getHeadCommitHash());
		state.pathFilter = pathFilter;
		state.tabPanel = RevisionComparePage.TabPanel.FILE_CHANGES;
		state.whitespaceOption = whitespaceOption;
		state.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, RevisionComparePage.paramsOf(source.getProject(), state)).toString();
	}

	@Override
	public void onMark(AjaxRequestTarget target, Mark mark) {
		this.mark = mark;
	}

	@Override
	public void onUnmark(AjaxRequestTarget target) {
		this.mark = null;
	}
	
	@Override
	public CodeComment getOpenComment() {
		if (commentId != null)
			return OneDev.getInstance(CodeCommentManager.class).load(commentId);
		else
			return null;
	}

	@Override
	public Map<CodeComment, PlanarRange> getOldComments(String blobPath) {
		Map<CodeComment, PlanarRange> oldComments = new HashMap<>();
		for (CodeComment comment: commentsModel.getObject()) {
			if (comment.getMark().getCommitHash().equals(getPullRequest().getBaseCommitHash())
					&& comment.getMark().getPath().equals(blobPath)) {
				oldComments.put(comment, comment.getMark().getRange());
			}
		}
		return oldComments;
	}

	@Override
	public Map<CodeComment, PlanarRange> getNewComments(String blobPath) {
		Map<CodeComment, PlanarRange> newComments = new HashMap<>();
		for (CodeComment comment: commentsModel.getObject()) {
			if (comment.getMark().getCommitHash().equals(source.getObjectName())
					&& comment.getMark().getPath().equals(blobPath)) {
				newComments.put(comment, comment.getMark().getRange());
			}
		}
		return newComments;
	}
	
	@Override
	public Collection<CodeProblem> getOldProblems(String blobPath) {
		Set<CodeProblem> problems = new HashSet<>();
		ObjectId baseCommitId = ObjectId.fromString(getPullRequest().getBaseCommitHash());
		for (Build build: getBuilds(baseCommitId)) {
			for (CodeProblemContribution contribution: OneDev.getExtensions(CodeProblemContribution.class))
				problems.addAll(contribution.getCodeProblems(build, blobPath, null));
		}
		return problems;
	}

	@Override
	public Collection<CodeProblem> getNewProblems(String blobPath) {
		Set<CodeProblem> problems = new HashSet<>();
		for (Build build: getBuilds(source.getObjectId())) {
			for (CodeProblemContribution contribution: OneDev.getExtensions(CodeProblemContribution.class))
				problems.addAll(contribution.getCodeProblems(build, blobPath, null));
		}
		return problems;
	}

	@Override
	public Map<Integer, Integer> getOldCoverages(String blobPath) {
		Map<Integer, Integer> coverages = new HashMap<>();
		ObjectId baseCommitId = ObjectId.fromString(getPullRequest().getBaseCommitHash());
		for (Build build: getBuilds(baseCommitId)) {
			for (LineCoverageContribution contribution: OneDev.getExtensions(LineCoverageContribution.class)) {
				contribution.getLineCoverages(build, blobPath, null).forEach((key, value) -> {
					coverages.merge(key, value, (v1, v2) -> v1+v2);
				});
			}
		}
		return coverages;
	}

	@Override
	public Map<Integer, Integer> getNewCoverages(String blobPath) {
		Map<Integer, Integer> coverages = new HashMap<>();
		for (Build build: getBuilds(source.getObjectId())) {
			for (LineCoverageContribution contribution: OneDev.getExtensions(LineCoverageContribution.class)) {
				contribution.getLineCoverages(build, blobPath, null).forEach((key, value) -> {
					coverages.merge(key, value, (v1, v2) -> v1+v2);
				});
			}
		}
		return coverages;
	}
	
	@Override
	public void onAddComment(AjaxRequestTarget target, Mark mark) {
		this.commentId = null;
		this.mark = mark;
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		commentId = comment.getId();
		mark = comment.getMark();
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target) {
		commentId = null;
		mark = null;
	}
	
	@Override
	public void onSaveComment(CodeComment comment) {
		OneDev.getInstance(CodeCommentManager.class).save(comment);
	}
	
	@Override
	public void onSaveCommentReply(CodeCommentReply reply) {
		OneDev.getInstance(CodeCommentReplyManager.class).save(reply);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(target.getProject()) && SecurityUtils.canReadCode(source.getProject());
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-nowrap'>Create Pull Request</span>").setEscapeModelStrings(false);
	}
	
}
