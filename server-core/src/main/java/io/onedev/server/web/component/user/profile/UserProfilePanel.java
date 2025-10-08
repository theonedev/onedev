package io.onedev.server.web.component.user.profile;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.service.CodeCommentReplyService;
import io.onedev.server.service.CodeCommentStatusChangeService;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.mail.MailService;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.DateRange;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.datepicker.DateRangePicker;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.component.user.UserDeleteLink;
import io.onedev.server.web.component.user.profile.activity.ApprovePullRequest;
import io.onedev.server.web.component.user.profile.activity.CommentIssue;
import io.onedev.server.web.component.user.profile.activity.CommentPullRequest;
import io.onedev.server.web.component.user.profile.activity.CommitCode;
import io.onedev.server.web.component.user.profile.activity.CreateCodeComment;
import io.onedev.server.web.component.user.profile.activity.DiscardPullRequest;
import io.onedev.server.web.component.user.profile.activity.MergePullRequest;
import io.onedev.server.web.component.user.profile.activity.OpenIssue;
import io.onedev.server.web.component.user.profile.activity.OpenPullRequest;
import io.onedev.server.web.component.user.profile.activity.ReopenPullRequest;
import io.onedev.server.web.component.user.profile.activity.ReplyCodeComment;
import io.onedev.server.web.component.user.profile.activity.RequestChangesToPullRequest;
import io.onedev.server.web.component.user.profile.activity.ResolveCodeComment;
import io.onedev.server.web.component.user.profile.activity.TransitIssue;
import io.onedev.server.web.component.user.profile.activity.UnresolveCodeComment;
import io.onedev.server.web.component.user.profile.activity.UserActivity;
import io.onedev.server.web.page.user.password.UserPasswordPage;
import io.onedev.server.web.util.ConfirmClickModifier;
import io.onedev.server.web.util.TextUtils;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.xodus.CommitInfoService;

public abstract class UserProfilePanel extends GenericPanel<User> {

    private static final int PAGE_SIZE = 100;

    private final Map<Long, Integer> activityStatsByDay = new HashMap<>();
    
    private final Map<String, Integer> activityStatsByType = new HashMap<>();
    
    private final List<UserActivity> accessibleActivities = new ArrayList<>();

    private final long inaccessibleActivityCount;

    private final LocalDate fromDate;

    private final LocalDate toDate;

    private LocalDate currentDate;

    private int currentPage;

    private RepeatingView activitiesView;

    private final Map<String, String> translations = new HashMap<>();

    public UserProfilePanel(String id, IModel<User> model, @Nullable DateRange dateRange) {
        super(id, model);

        translations.put("user-activities-commits-note", _T("Commits are taken from default branch of non-forked repositories"));
    
        translations.put("mon", _T("week:Mon"));
        translations.put("tue", _T("week:Tue"));
        translations.put("wed", _T("week:Wed"));
        translations.put("thu", _T("week:Thu"));
        translations.put("fri", _T("week:Fri"));
        translations.put("sat", _T("week:Sat"));
        translations.put("sun", _T("week:Sun"));
        translations.put("jan", _T("month:Jan"));
        translations.put("feb", _T("month:Feb"));
        translations.put("mar", _T("month:Mar"));
        translations.put("apr", _T("month:Apr"));
        translations.put("may", _T("month:May"));
        translations.put("jun", _T("month:Jun"));
        translations.put("jul", _T("month:Jul"));
        translations.put("aug", _T("month:Aug"));
        translations.put("sep", _T("month:Sep"));
        translations.put("oct", _T("month:Oct"));
        translations.put("nov", _T("month:Nov"));
        translations.put("dec", _T("month:Dec"));
        translations.put("activity-by-type", _T("Activity by type"));
        translations.put("less", _T("Less"));
        translations.put("more", _T("More"));
        translations.put("cell-tooltip", _T("{0} activities on {1}"));

        Arrays.stream(UserActivity.Type.values()).forEach(it -> {
            translations.put(it.name(), _T(TextUtils.getDisplayValue(it).toLowerCase()));
        });

        if (dateRange != null) {
            fromDate = dateRange.getFrom();
            toDate = dateRange.getTo();
            if (toDate.toEpochDay() - fromDate.toEpochDay() > 365) 
                throw new ExplicitException("Date range must not exceed 365 days");
        } else {
            toDate = DateUtils.toLocalDate(new Date());
            fromDate = toDate.minusDays(365);
        }             
        var activities = getActivities(DateUtils.toDate(fromDate.atStartOfDay()), DateUtils.toDate(toDate.atTime(23, 59, 59)));
        var inaccessibleActivityCount = 0;
        for (var activity: activities) {
            if (activity.isAccessible()) {
                accessibleActivities.add(activity);
            } else {
                inaccessibleActivityCount++;
            }
            activityStatsByType.merge(translations.get(activity.getType().name()), 1, Integer::sum);
            activityStatsByDay.merge(DateUtils.toLocalDate(activity.getDate()).toEpochDay(), 1, Integer::sum);
        }
        this.inaccessibleActivityCount = inaccessibleActivityCount;
    }

    private AuditService getAuditService() {
        return OneDev.getInstance(AuditService.class);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        var user = getUser();
        add(new UserAvatar("avatar", user));

        add(new Label("name", "@" + user.getName()));
        add(new Label("fullName", user.getFullName()).setVisible(user.getFullName() != null));

        var emailAddress = user.getPublicEmailAddress();
        if (emailAddress != null) {
            add(new WebMarkupContainer("emailAddress") {

                @Override
                protected void onInitialize() {
                    super.onInitialize();
                    add(new Label("label", emailAddress.getValue()));
                }

                @Override
                protected void onComponentTag(ComponentTag tag) {
                    super.onComponentTag(tag);
                    tag.put("href", "mailto:" + emailAddress.getValue());
                }

            });
        } else {
            add(new WebMarkupContainer("emailAddress") {

                @Override
                protected void onInitialize() {
                    super.onInitialize();
                    add(new Label("label"));
                }

            }.setVisible(false));
        }

		WebMarkupContainer noteContainer;
		if (user.isDisabled() && WicketUtils.isSubscriptionActive()) {
			if (user.isServiceAccount())
				noteContainer = new Fragment("note", "disabledServiceAccountFrag", this);
			else
				noteContainer = new Fragment("note", "disabledFrag", this);
		} else if (user.isServiceAccount() && WicketUtils.isSubscriptionActive()) {
			noteContainer = new Fragment("note", "serviceAccountFrag", this);
		} else if (user.getPassword() != null) {
			noteContainer = new Fragment("note", "authViaInternalDatabaseFrag", this);
            var actions = new WebMarkupContainer("actions");
            actions.setVisible(SecurityUtils.isAdministrator());
            noteContainer.add(actions);
			actions.add(new Link<Void>("removePassword") {

				@Override
				public void onClick() {
                    var user = getUser();
					user.setPassword(null);
					getUserService().update(user, null);
					Session.get().success(_T("Password has been removed"));
                    setResponsePage(getPage().getClass(), getPage().getPageParameters());
				}

			}.add(new ConfirmClickModifier(_T("Do you really want to remove password of this user?"))));
            
            noteContainer.setVisible(SecurityUtils.isAdministrator() || getUser().equals(SecurityUtils.getAuthUser()));
		} else {
			noteContainer = new Fragment("note", "authViaExternalSystemFrag", this);
			var form = new Form<Void>("form");
            form.setVisible(SecurityUtils.isAdministrator());
			noteContainer.add(form);
			form.add(new BookmarkablePageLink<Void>("setPasswordForUser",
					UserPasswordPage.class, UserPasswordPage.paramsOf(getUser())));
			var passwordResetRequestSentMessage = _T("Password reset request has been sent");
			var noVerifiedPrimaryEmailAddressMessage = _T("No verified primary email address");
			var mailServiceNotConfiguredMessage = _T("Unable to notify user as mail service is not configured");
			var mailSubject = _T("[Reset Password] Please Reset Your OneDev Password");
			form.add(new TaskButton("tellUserToResetPassword") {

				@Override
				protected TaskResult runTask(TaskLogger logger) {
					var sessionService = OneDev.getInstance(SessionService.class);
					return sessionService.call(() -> {
						SettingService settingService = OneDev.getInstance(SettingService.class);
						if (settingService.getMailConnector() != null) {
                            var user = getUser();
							if (user.getPrimaryEmailAddress() != null && user.getPrimaryEmailAddress().isVerified()) {
								String passwordResetCode = CryptoUtils.generateSecret();
								user.setPasswordResetCode(passwordResetCode);
								getUserService().update(user, null);

								MailService mailService = OneDev.getInstance(MailService.class);

								Map<String, Object> bindings = new HashMap<>();
								bindings.put("passwordResetUrl", settingService.getSystemSetting().getServerUrl() + "/~reset-password/" + passwordResetCode);
								bindings.put("user", user);

								var template = settingService.getEmailTemplates().getPasswordReset();
								var htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
								var textBody = EmailTemplates.evalTemplate(false, template, bindings);

								mailService.sendMail(Arrays.asList(user.getPrimaryEmailAddress().getValue()),
										Lists.newArrayList(), Lists.newArrayList(),
										mailSubject,
										htmlBody, textBody, null, null, null);

								return new TaskResult(true, new TaskResult.PlainMessage(passwordResetRequestSentMessage));
							} else {
								return new TaskResult(false, new TaskResult.PlainMessage(noVerifiedPrimaryEmailAddressMessage));
							}
						} else {
							return new TaskResult(false, new TaskResult.PlainMessage(mailServiceNotConfiguredMessage));
						}
					});
				}

			});
            noteContainer.setVisible(SecurityUtils.isAdministrator() || getUser().equals(SecurityUtils.getUser()));
		}

		add(noteContainer);		
        
		add(new Link<Void>("enable") {

			@Override
			public void onClick() {
				getUserService().enable(getUser());
                getAuditService().audit(null, "enabled account \"" + getUser().getName() + "\"", null, null);
				Session.get().success("User enabled");
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator() && getUser().isDisabled() && WicketUtils.isSubscriptionActive());
			}

		}.add(new ConfirmClickModifier(_T("Do you really want to enable this account?"))));

		add(new Link<Void>("disable") {

			@Override
			public void onClick() {
				getUserService().disable(getUser());
                getAuditService().audit(null, "disabled account \"" + getUser().getName() + "\"", null, null);
				Session.get().success(_T("User disabled"));
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator() 
                        && !getUser().equals(SecurityUtils.getAuthUser()) 
                        && !getUser().isDisabled() 
                        && WicketUtils.isSubscriptionActive());
			}

		}.add(new ConfirmClickModifier(_T("Disabling account will reset password, clear access tokens, "
				+ "and remove all references from other entities except for past activities. Do you "
				+ "really want to continue?"))));
		
		add(new UserDeleteLink("delete") {

			@Override
			protected User getUser() {
				return UserProfilePanel.this.getUser();
			}

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (getUser().isRoot()) {
                    setVisible(false);
                } else if (SecurityUtils.isAdministrator()) {
                    setVisible(true);
                } else {
                    setVisible(getUser().equals(SecurityUtils.getAuthUser()) 
                            && getSettingService().getSecuritySetting().isEnableSelfDeregister());
                }
            }
		});        

        var dateRangePicker = new DateRangePicker("dateRange", Model.of(new DateRange(fromDate, toDate)));
        dateRangePicker.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                var newPanel = new UserProfilePanel(UserProfilePanel.this.getId(), getModel(), dateRangePicker.getModelObject()) {

                    @Override
                    protected void onDateRangeChanged(AjaxRequestTarget target, DateRange dateRange) {
                        UserProfilePanel.this.onDateRangeChanged(target, dateRange);
                    }

                };
                replaceWith(newPanel);
                target.add(newPanel);
                onDateRangeChanged(target, dateRangePicker.getModelObject());
            }
        });
        dateRangePicker.setRequired(true);
        add(dateRangePicker);

        add(new Label("activityCount", accessibleActivities.size() + inaccessibleActivityCount));

        activitiesView = new RepeatingView("activities");
        accessibleActivities.stream().limit(PAGE_SIZE).forEach(it -> {
            for (var row: newActivityRows(activitiesView, it)) {
                activitiesView.add(row);
            }
        });
        add(activitiesView);

        var moreContainer = new WebMarkupContainer("more") {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(PAGE_SIZE * (currentPage + 1) < accessibleActivities.size());
            }

        };
        moreContainer.setOutputMarkupId(true);
        
        moreContainer.add(new AjaxLink<Void>("link") {

            @SuppressWarnings("deprecation")
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(moreContainer);
                currentPage++;
                accessibleActivities.stream().skip(PAGE_SIZE * currentPage).limit(PAGE_SIZE).forEach(it -> {
                    for (var row: newActivityRows(activitiesView, it)) {
                        var script = String.format("$('#%s').after('<li id=\"%s\"></li>');", activitiesView.get(activitiesView.size() - 1).getMarkupId(), row.getMarkupId());
                        target.prependJavaScript(script);
                        activitiesView.add(row);
                        target.add(row);                        
                    }
                });
                target.focusComponent(null);
            }

        });
        add(moreContainer);

        if (inaccessibleActivityCount > 0) {
            add(new Label("inaccessibles", MessageFormat.format(_T("{0} inaccessible activities"), inaccessibleActivityCount)));
        } else {
            add(new WebMarkupContainer("inaccessibles").setVisible(false));
        }

        setOutputMarkupId(true);
    }

    private UserService getUserService() {
        return OneDev.getInstance(UserService.class);
    }

    private SettingService getSettingService() {
        return OneDev.getInstance(SettingService.class);
    }

    private User getUser() {
        return getModelObject();
    }

    private List<Component> newActivityRows(RepeatingView activitiesView, UserActivity activity) {
        var rows = new ArrayList<Component>();
        var activityDate = DateUtils.toLocalDate(activity.getDate());
        if (currentDate == null || !currentDate.equals(activityDate)) {
            currentDate = activityDate;
            var fragment = new Fragment(activitiesView.newChildId(), "dateFrag", this);            
            fragment.add(new Label("date", currentDate.format(DateUtils.DATE_FORMATTER)));
            fragment.setOutputMarkupId(true);
            rows.add(fragment);
        }
        var fragment = new Fragment(activitiesView.newChildId(), "activityFrag", this);            
        fragment.add(new Label("time", DateUtils.formatTime(activity.getDate())));
        fragment.add(activity.render("description"));
        fragment.setOutputMarkupId(true);
        rows.add(fragment);
        return rows;
    }

    private List<UserActivity> getActivities(Date fromDate, Date toDate) {
        var activities = new ArrayList<UserActivity>();

        var user = getUser();

        var commitInfoService = OneDev.getInstance(CommitInfoService.class);
        for (var entry: commitInfoService.getUserCommits(user, fromDate, toDate).entrySet()) {
            for (var entry2: entry.getValue().entrySet())
                activities.add(new CommitCode(new Date(entry2.getValue()), entry.getKey(), entry2.getKey()));
        }
        
        var issueService = OneDev.getInstance(IssueService.class);
        for (var issue: issueService.query(user, fromDate, toDate)) {
            activities.add(new OpenIssue(issue));
        }

        var issueCommentService = OneDev.getInstance(IssueCommentService.class);
        for (var comment: issueCommentService.query(user, fromDate, toDate)) {
            activities.add(new CommentIssue(comment));
        }

        var issueChangeService = OneDev.getInstance(IssueChangeService.class);
        for (var change: issueChangeService.query(user, fromDate, toDate)) {
            if (change.getData() instanceof IssueStateChangeData) {
                IssueStateChangeData stateChangeData = (IssueStateChangeData) change.getData();
                activities.add(new TransitIssue(change.getDate(), change.getIssue(), stateChangeData.getNewState()));
            } else if (change.getData() instanceof IssueBatchUpdateData) {
                IssueBatchUpdateData batchUpdateData = (IssueBatchUpdateData) change.getData();
                if (!batchUpdateData.getOldState().equals(batchUpdateData.getNewState())) 
                    activities.add(new TransitIssue(change.getDate(), change.getIssue(), batchUpdateData.getNewState()));
            }
        }

        var pullRequestService = OneDev.getInstance(PullRequestService.class);
        for (var pullRequest: pullRequestService.query(user, fromDate, toDate)) {
            activities.add(new OpenPullRequest(pullRequest));
        }

        var pullRequestCommentService = OneDev.getInstance(PullRequestCommentService.class);
        for (var comment: pullRequestCommentService.query(user, fromDate, toDate)) {
            activities.add(new CommentPullRequest(comment));
        }

        var pullRequestChangeService = OneDev.getInstance(PullRequestChangeService.class);
        for (var change: pullRequestChangeService.query(user, fromDate, toDate)) {
            if (change.getData() instanceof PullRequestApproveData) {
                activities.add(new ApprovePullRequest(change.getDate(), change.getRequest()));
            } else if (change.getData() instanceof PullRequestRequestedForChangesData) {
                activities.add(new RequestChangesToPullRequest(change.getDate(), change.getRequest()));
            } else if (change.getData() instanceof PullRequestMergeData) {
                activities.add(new MergePullRequest(change.getDate(), change.getRequest()));
            } else if (change.getData() instanceof PullRequestDiscardData) {
                activities.add(new DiscardPullRequest(change.getDate(), change.getRequest()));
            } else if (change.getData() instanceof PullRequestReopenData) {
                activities.add(new ReopenPullRequest(change.getDate(), change.getRequest()));
            }
        }

        var codeCommentService = OneDev.getInstance(CodeCommentService.class);
        for (var comment: codeCommentService.query(user, fromDate, toDate)) {
            activities.add(new CreateCodeComment(comment));
        }

        var codeCommentReplyService = OneDev.getInstance(CodeCommentReplyService.class);
        for (var reply: codeCommentReplyService.query(user, fromDate, toDate)) {
            activities.add(new ReplyCodeComment(reply));
        }

        var codeCommentStatusChangeService = OneDev.getInstance(CodeCommentStatusChangeService.class);
        for (var change: codeCommentStatusChangeService.query(user, fromDate, toDate)) {
            if (change.isResolved()) {
                activities.add(new ResolveCodeComment(change.getDate(), change.getComment()));
            } else {
                activities.add(new UnresolveCodeComment(change.getDate(), change.getComment()));
            }
        }

        activities.sort(Comparator.comparing(UserActivity::getDate).reversed());    

        return activities;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new UserProfileResourceReference())); 

        var mapper = OneDev.getInstance(ObjectMapper.class);        
        try {
            response.render(OnDomReadyHeaderItem.forScript(String.format(
                "onedev.server.userProfile.onDomReady(%s, %d, %d, %s, %s, %s);", 
                    mapper.writeValueAsString(activityStatsByDay), 
                    fromDate.toEpochDay(), 
                    toDate.toEpochDay(),
                    mapper.writeValueAsString(activityStatsByType), 
                    mapper.writeValueAsString(Arrays.stream(UserActivity.Type.values()).map(it->translations.get(it.name())).toArray()),
                    mapper.writeValueAsString(translations))));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected abstract void onDateRangeChanged(AjaxRequestTarget target, DateRange dateRange);

}
