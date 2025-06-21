package io.onedev.server.web.component.audit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Splitter;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AuditManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entitymanager.support.AuditQuery;
import io.onedev.server.model.Audit;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.DateRange;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.diff.DiffRenderer;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.asset.diff.DiffResourceReference;
import io.onedev.server.web.component.datepicker.DateRangePicker;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.user.choice.UserMultiChoice;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;

public abstract class AuditListPanel extends Panel {
    
    private static final int PAGE_SIZE = 100;

    private final IModel<List<Audit>> auditsModel = new LoadableDetachableModel<List<Audit>>() {

        @Override
        protected List<Audit> load() {
            return getAuditManager().query(getProject(), buildQuery(), currentPage * PAGE_SIZE, PAGE_SIZE);
        }
        
    };

    private final IModel<Integer> countModel = new LoadableDetachableModel<Integer>() {

        @Override
        protected Integer load() {
            return getAuditManager().count(getProject(), buildQuery());
        }

    };

    private RepeatingView auditsView;

    private int currentPage;

    private LocalDate currentDate;

    private DateRange dateRange;

    private List<String> userNames;

    private String action;

    public AuditListPanel(String id, List<String> userNames, @Nullable DateRange dateRange, @Nullable String action) {
        super(id);
        this.userNames = userNames;
        this.dateRange = dateRange;
        this.action = action;
    }

    private AuditQuery buildQuery() {
        Date sinceDate = null;
        Date untilDate = null;
        if (dateRange != null) {
            sinceDate = DateUtils.toDate(dateRange.getFrom().atStartOfDay());
            untilDate = DateUtils.toDate(dateRange.getTo().atTime(23, 59, 59));
        }        
        return new AuditQuery(getUsers(), sinceDate, untilDate, action);
    }

    private void updateAuditList(AjaxRequestTarget target) {
        var newPanel = new AuditListPanel(getId(), userNames, dateRange, action) {

            @Override
            protected void onQueryUpdated(AjaxRequestTarget target, @Nullable DateRange dateRange, List<String> userNames, @Nullable String action) {
                AuditListPanel.this.onQueryUpdated(target, dateRange, userNames, action);
            }

            @Override
            protected Project getProject() {
                return AuditListPanel.this.getProject();
            }

        };
        replaceWith(newPanel);
        target.add(newPanel);
    }

    private List<User> getUsers() {
        return userNames.stream().map(getUserManager()::findByName).collect(Collectors.toList());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        var dateRangePicker = new DateRangePicker("dateRange", Model.of(dateRange));
        dateRangePicker.add(new AjaxFormComponentUpdatingBehavior("change clear") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                dateRange = dateRangePicker.getModelObject();
                updateAuditList(target);
                onQueryUpdated(target, dateRange, userNames, action);
            }

        });
        add(dateRangePicker);

        var usersChoice = new UserMultiChoice("users", Model.of(getUsers()), Model.ofList(getUserManager().query()));
        usersChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                userNames = usersChoice.getModelObject().stream().map(User::getName).collect(Collectors.toList());
                updateAuditList(target);
                onQueryUpdated(target, dateRange, userNames, action);
            }
        });
        add(usersChoice);

        var actionInput = new TextField<String>("action", Model.of(action));
        actionInput.add(new AjaxFormComponentUpdatingBehavior("change clear") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                action = actionInput.getModelObject();
                updateAuditList(target);
                onQueryUpdated(target, dateRange, userNames, action);
            }
        });
        add(actionInput);

        auditsView = new RepeatingView("audits");
        for (var audit: auditsModel.getObject()) {
            for (var row: newAuditRows(auditsView, audit)) {
                auditsView.add(row);
            }
        }

        add(auditsView);

        var moreContainer = new WebMarkupContainer("more") {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(PAGE_SIZE * (currentPage + 1) < countModel.getObject());
            }

        };
        moreContainer.setOutputMarkupId(true);
        
        moreContainer.add(new AjaxLink<Void>("link") {

            @SuppressWarnings("deprecation")
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(moreContainer);
                currentPage++;
                for (var audit: auditsModel.getObject()) {
                    for (var row: newAuditRows(auditsView, audit)) {
                        var script = String.format("$('#%s').after('<li id=\"%s\"></li>');", auditsView.get(auditsView.size() - 1).getMarkupId(), row.getMarkupId());
                        target.prependJavaScript(script);
                        auditsView.add(row);
                        target.add(row);                        
                    }
                }
                target.focusComponent(null);
            }

        });
        add(moreContainer);

        add(new WebMarkupContainer("noAudits") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(countModel.getObject() == 0);
            }
        });

        setOutputMarkupId(true);
    }

    private UserManager getUserManager() {
        return OneDev.getInstance(UserManager.class);
    }

    private AuditManager getAuditManager() {
        return OneDev.getInstance(AuditManager.class);
    }

    @Override
    protected void onDetach() {
        auditsModel.detach();
        countModel.detach();
        super.onDetach();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new DiffResourceReference()));
        response.render(CssHeaderItem.forReference(new AuditLogCssResourceReference()));
    }

    private List<Component> newAuditRows(RepeatingView auditsView, Audit audit) {
        var rows = new ArrayList<Component>();
        var auditDate = DateUtils.toLocalDate(audit.getDate());
        if (currentDate == null || !currentDate.equals(auditDate)) {
            currentDate = auditDate;
            var fragment = new Fragment(auditsView.newChildId(), "dateFrag", this);            
            fragment.add(new Label("date", currentDate.format(DateUtils.DATE_FORMATTER)));
            fragment.setOutputMarkupId(true);
            rows.add(fragment);
        }
        var fragment = new Fragment(auditsView.newChildId(), "auditFrag", this);            
        fragment.add(new Label("time", DateUtils.formatTime(audit.getDate())));
        fragment.add(new UserIdentPanel("user", audit.getUser(), Mode.AVATAR_AND_NAME));
        fragment.add(new Label("action", audit.getAction()));
        var oldContent = audit.getOldContent();
        var newContent = audit.getNewContent();
        fragment.add(new DropdownLink("diff") {

            @Override
            protected Component newContent(String id, FloatingPanel dropdown) {
                List<String> oldLines = new ArrayList<>();
                if (oldContent != null)
                    oldLines = Splitter.on('\n').splitToList(oldContent);
                List<String> newLines = new ArrayList<>();
                if (newContent != null)
                    newLines = Splitter.on('\n').splitToList(newContent);
                var fragment = new Fragment(id, "diffFrag", AuditListPanel.this);
                fragment.add(new Label("content", new DiffRenderer(DiffUtils.diff(oldLines, newLines)).renderDiffs()).setEscapeModelStrings(false));
                return fragment;
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(oldContent != null || newContent != null);
            }

        });
        fragment.setOutputMarkupId(true);
        rows.add(fragment);
        return rows;
    }

    @Nullable
    protected abstract Project getProject();

    protected abstract void onQueryUpdated(AjaxRequestTarget target, @Nullable DateRange dateRange, List<String> userNames, @Nullable String action);
    
}
