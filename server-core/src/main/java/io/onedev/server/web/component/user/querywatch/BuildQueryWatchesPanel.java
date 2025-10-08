package io.onedev.server.web.component.user.querywatch;

import static io.onedev.server.model.support.NamedQuery.COMMON_NAME_PREFIX;
import static io.onedev.server.model.support.NamedQuery.PERSONAL_NAME_PREFIX;
import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.BuildQueryPersonalizationService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.page.builds.BuildListPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.user.UserPage;
import io.onedev.server.web.util.ConfirmClickModifier;

class BuildQueryWatchesPanel extends GenericPanel<User> {

    private static final long serialVersionUID = 1L;
    
    private SelectionColumn<QueryInfo, Void> selectionColumn;
    
    private final IModel<List<QueryInfo>> queryInfosModel = new LoadableDetachableModel<>() {

        @Override
        protected List<QueryInfo> load() {
            List<QueryInfo> queryInfos = new ArrayList<>();
            var buildSetting = getSettingService().getBuildSetting();
            for (var name: getUser().getBuildQuerySubscriptions()) {
                NamedQuery query;
                if (name.startsWith(COMMON_NAME_PREFIX))
                    query = buildSetting.getNamedQuery(name.substring(COMMON_NAME_PREFIX.length()));
                else
                    query = getUser().getBuildQuery(name.substring(PERSONAL_NAME_PREFIX.length()));

                if (query != null) 
                    queryInfos.add(new QueryInfo(name, query.getQuery(), null));
            }
            for (var personalization: getUser().getBuildQueryPersonalizations()) {
                for (var name: personalization.getQuerySubscriptions()) {            
                    NamedQuery query;            
                    if (name.startsWith(COMMON_NAME_PREFIX))
                        query = personalization.getProject().getNamedBuildQuery(name.substring(COMMON_NAME_PREFIX.length()));
                    else                   
                        query = personalization.getQuery(name.substring(PERSONAL_NAME_PREFIX.length()));
                    if (query != null) 
                        queryInfos.add(new QueryInfo(name, query.getQuery(), personalization.getProject().getId()));
                }
            }
            return queryInfos;
        }
    };

    public BuildQueryWatchesPanel(String id, IModel<User> model) {
        super(id, model);
    }
        
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        var deleteSelected = new Link<Void>("deleteSelected") {

            @Override
            public void onClick() {
                OneDev.getInstance(TransactionService.class).run(() -> {
                    var auditService = OneDev.getInstance(AuditService.class);
                    for (IModel<QueryInfo> each: selectionColumn.getSelections()) {
                        var queryInfo = each.getObject();
                        if (queryInfo.projectId == null) {
                             getUser().getBuildQuerySubscriptions().remove(queryInfo.name);
                             if (getPage() instanceof UserPage)
                                auditService.audit(null, "unsubscribed from build query \"" + queryInfo.name + "\" in account \"" + getUser().getName() + "\"", null, null);
                        } else {
                             for (var personalization: getUser().getBuildQueryPersonalizations()) {
                                 if (personalization.getProject().getId().equals(queryInfo.projectId)) {
                                     personalization.getQuerySubscriptions().remove(queryInfo.name);
                                     getBuildQueryPersonalizationService().createOrUpdate(personalization);
                                     if (getPage() instanceof UserPage)
                                        auditService.audit(null, "unsubscribed from build query \"" + queryInfo.name + "\" in account \"" + getUser().getName() + "\" in project \"" + personalization.getProject().getPath() + "\"", null, null);
                                 }
                             }
                        }
                     }
                     getUserService().update(getUser(), null);     
                });
            }
            
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(!selectionColumn.getSelections().isEmpty());
                setVisible(!queryInfosModel.getObject().isEmpty());
            }
            
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (!isEnabled()) {
                    tag.put("disabled", "disabled");
                    tag.put("data-tippy-content", _T("Please select query watches to delete"));
                }
            }
            
        }.add(new ConfirmClickModifier(_T("Do you really want to delete selected query watches?"))).setOutputMarkupId(true);

        add(deleteSelected);        

        List<IColumn<QueryInfo, Void>> columns = new ArrayList<>();
        
        selectionColumn = new SelectionColumn<QueryInfo, Void>() {

            @Override
            protected void onSelectionChange(AjaxRequestTarget target) {
                target.add(deleteSelected);
            }
            
        };
        columns.add(selectionColumn);
        
        columns.add(new AbstractColumn<>(Model.of(_T("Name"))) {

            @Override
            public void populateItem(Item<ICellPopulator<QueryInfo>> cellItem, String componentId,
                                     IModel<QueryInfo> rowModel) {
                var name = rowModel.getObject().name;
                if (name.startsWith(COMMON_NAME_PREFIX)) 
                    name = name.substring(COMMON_NAME_PREFIX.length()) + " (" + _T("common") + ")";
                else
                    name = name.substring(PERSONAL_NAME_PREFIX.length()) + " (" + _T("personal") + ")";
                                        
                var fragment = new Fragment(componentId, "linkFrag", BuildQueryWatchesPanel.this);
                var projectId = rowModel.getObject().projectId;
                if (projectId != null) {
                    var project = getProjectService().load(projectId);
                    var link = new BookmarkablePageLink<Void>("link", ProjectBuildsPage.class, ProjectBuildsPage.paramsOf(project, rowModel.getObject().query, 1));
                    link.add(new Label("label", name));
                    fragment.add(link);
                } else {
                    var link = new BookmarkablePageLink<Void>("link", BuildListPage.class, BuildListPage.paramsOf(rowModel.getObject().query, 1));
                    link.add(new Label("label", name));
                    fragment.add(link);
                }
                cellItem.add(fragment);
            }
        });

        columns.add(new AbstractColumn<>(Model.of(_T("Project"))) {

            @Override
            public void populateItem(Item<ICellPopulator<QueryInfo>> cellItem, String componentId,
                                     IModel<QueryInfo> rowModel) {
                var projectId = rowModel.getObject().projectId;
                if (projectId != null) {
                    var project = getProjectService().load(projectId);
                    var fragment = new Fragment(componentId, "linkFrag", BuildQueryWatchesPanel.this);
                    var link = new BookmarkablePageLink<Void>("link", ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project));
                    link.add(new Label("label", project.getPath()));
                    fragment.add(link);
                    cellItem.add(fragment);
                } else {
                    cellItem.add(new Label(componentId, "<i>" + _T("Global") + "</i>").setEscapeModelStrings(false));
                }
            }
        });
        
		var dataProvider = new SortableDataProvider<QueryInfo, Void>() {

			@Override
			public Iterator<? extends QueryInfo> iterator(long first, long count) {
                List<QueryInfo> queryInfos = queryInfosModel.getObject();
                if (first + count > queryInfos.size()) 
                    return queryInfos.subList((int) first, queryInfos.size()).iterator();
                else 
                    return queryInfos.subList((int) first, (int) (first + count)).iterator();
			}

			@Override
			public long size() {
				return queryInfosModel.getObject().size();
			}

			@Override
			public IModel<QueryInfo> model(QueryInfo object) {
				return Model.of(object);
			}

        };        
        
        add(new DefaultDataTable<>("queries", columns, dataProvider, Integer.MAX_VALUE, null));        
    }
    
    @Override
    protected void onDetach() {
        queryInfosModel.detach();
        super.onDetach();
    }

    private ProjectService getProjectService() {
        return OneDev.getInstance(ProjectService.class);
    }

    private SettingService getSettingService() {
        return OneDev.getInstance(SettingService.class);
    }

    private BuildQueryPersonalizationService getBuildQueryPersonalizationService() {
        return OneDev.getInstance(BuildQueryPersonalizationService.class);
    }
    
    private UserService getUserService() {
        return OneDev.getInstance(UserService.class);
    }

    private User getUser() {
        return getModelObject();
    }
    
    private static class QueryInfo implements Serializable {

        final String name;

        final String query;
        
        final Long projectId;

        QueryInfo(String name, String query, @Nullable Long projectId) {
            this.name = name;
            this.query = query;
            this.projectId = projectId;
        }

    }
} 
