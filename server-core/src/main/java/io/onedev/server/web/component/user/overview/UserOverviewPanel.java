package io.onedev.server.web.component.user.overview;

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

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.entitymanager.CodeCommentStatusChangeManager;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.util.DateRange;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.datepicker.DateRangePicker;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.component.user.overview.activity.ApprovePullRequest;
import io.onedev.server.web.component.user.overview.activity.CommentIssue;
import io.onedev.server.web.component.user.overview.activity.CommentPullRequest;
import io.onedev.server.web.component.user.overview.activity.CommitCode;
import io.onedev.server.web.component.user.overview.activity.CreateCodeComment;
import io.onedev.server.web.component.user.overview.activity.DiscardPullRequest;
import io.onedev.server.web.component.user.overview.activity.MergePullRequest;
import io.onedev.server.web.component.user.overview.activity.OpenIssue;
import io.onedev.server.web.component.user.overview.activity.OpenPullRequest;
import io.onedev.server.web.component.user.overview.activity.ReopenPullRequest;
import io.onedev.server.web.component.user.overview.activity.ReplyCodeComment;
import io.onedev.server.web.component.user.overview.activity.RequestChangesToPullRequest;
import io.onedev.server.web.component.user.overview.activity.ResolveCodeComment;
import io.onedev.server.web.component.user.overview.activity.TransitIssue;
import io.onedev.server.web.component.user.overview.activity.UnresolveCodeComment;
import io.onedev.server.web.component.user.overview.activity.UserActivity;
import io.onedev.server.web.util.TextUtils;
import io.onedev.server.xodus.CommitInfoManager;

public abstract class UserOverviewPanel extends GenericPanel<User> {

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

    public UserOverviewPanel(String id, IModel<User> model, @Nullable DateRange dateRange) {
        super(id, model);

        translations.put("user-activities-commits-note", _T("Commits are taken from default branch of non-forked repositories"));
    
        translations.put("week.mon", _T("week.Mon"));
        translations.put("week.tue", _T("week.Tue"));
        translations.put("week.wed", _T("week.Wed"));
        translations.put("week.thu", _T("week.Thu"));
        translations.put("week.fri", _T("week.Fri"));
        translations.put("week.sat", _T("week.Sat"));
        translations.put("week.sun", _T("week.Sun"));
        translations.put("month.jan", _T("month.Jan"));
        translations.put("month.feb", _T("month.Feb"));
        translations.put("month.mar", _T("month.Mar"));
        translations.put("month.apr", _T("month.Apr"));
        translations.put("month.may", _T("month.May"));
        translations.put("month.jun", _T("month.Jun"));
        translations.put("month.jul", _T("month.Jul"));
        translations.put("month.aug", _T("month.Aug"));
        translations.put("month.sep", _T("month.Sep"));
        translations.put("month.oct", _T("month.Oct"));
        translations.put("month.nov", _T("month.Nov"));
        translations.put("month.dec", _T("month.Dec"));
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

    @Override
    protected void onInitialize() {
        super.onInitialize();

        var user = getModelObject();
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

        var dateRangePicker = new DateRangePicker("dateRange", Model.of(new DateRange(fromDate, toDate)));
        dateRangePicker.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                var newPanel = new UserOverviewPanel("overview", getModel(), dateRangePicker.getModelObject()) {

                    @Override
                    protected void onDateRangeChanged(AjaxRequestTarget target, DateRange dateRange) {
                        UserOverviewPanel.this.onDateRangeChanged(target, dateRange);
                    }

                };
                replaceWith(newPanel);
                target.add(newPanel);
                onDateRangeChanged(target, dateRangePicker.getModelObject());
            }
        });
        dateRangePicker.setRequired(true);
        add(dateRangePicker);

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

        var user = getModelObject();

        var commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
        for (var entry: commitInfoManager.getUserCommits(user, fromDate, toDate).entrySet()) {
            for (var entry2: entry.getValue().entrySet())
                activities.add(new CommitCode(new Date(entry2.getValue()), entry.getKey(), entry2.getKey()));
        }
        
        var issueManager = OneDev.getInstance(IssueManager.class);
        for (var issue: issueManager.query(user, fromDate, toDate)) {
            activities.add(new OpenIssue(issue));
        }

        var issueCommentManager = OneDev.getInstance(IssueCommentManager.class);
        for (var comment: issueCommentManager.query(user, fromDate, toDate)) {
            activities.add(new CommentIssue(comment));
        }

        var issueChangeManager = OneDev.getInstance(IssueChangeManager.class);
        for (var change: issueChangeManager.query(user, fromDate, toDate)) {
            if (change.getData() instanceof IssueStateChangeData) {
                IssueStateChangeData stateChangeData = (IssueStateChangeData) change.getData();
                activities.add(new TransitIssue(change.getDate(), change.getIssue(), stateChangeData.getNewState()));
            } else if (change.getData() instanceof IssueBatchUpdateData) {
                IssueBatchUpdateData batchUpdateData = (IssueBatchUpdateData) change.getData();
                if (!batchUpdateData.getOldState().equals(batchUpdateData.getNewState())) 
                    activities.add(new TransitIssue(change.getDate(), change.getIssue(), batchUpdateData.getNewState()));
            }
        }

        var pullRequestManager = OneDev.getInstance(PullRequestManager.class);
        for (var pullRequest: pullRequestManager.query(user, fromDate, toDate)) {
            activities.add(new OpenPullRequest(pullRequest));
        }

        var pullRequestCommentManager = OneDev.getInstance(PullRequestCommentManager.class);
        for (var comment: pullRequestCommentManager.query(user, fromDate, toDate)) {
            activities.add(new CommentPullRequest(comment));
        }

        var pullRequestChangeManager = OneDev.getInstance(PullRequestChangeManager.class);
        for (var change: pullRequestChangeManager.query(user, fromDate, toDate)) {
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

        var codeCommentManager = OneDev.getInstance(CodeCommentManager.class);
        for (var comment: codeCommentManager.query(user, fromDate, toDate)) {
            activities.add(new CreateCodeComment(comment));
        }

        var codeCommentReplyManager = OneDev.getInstance(CodeCommentReplyManager.class);
        for (var reply: codeCommentReplyManager.query(user, fromDate, toDate)) {
            activities.add(new ReplyCodeComment(reply));
        }

        var codeCommentStatusChangeManager = OneDev.getInstance(CodeCommentStatusChangeManager.class);
        for (var change: codeCommentStatusChangeManager.query(user, fromDate, toDate)) {
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
        response.render(JavaScriptHeaderItem.forReference(new UserOverviewResourceReference())); 

        var mapper = OneDev.getInstance(ObjectMapper.class);        
        try {
            response.render(OnDomReadyHeaderItem.forScript(String.format(
                "onedev.server.userOverview.onDomReady(%s, %d, %d, %s, %s, %s);", 
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
