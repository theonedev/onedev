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
import io.onedev.server.service.CommitQueryPersonalizationService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.user.UserPage;
import io.onedev.server.web.util.ConfirmClickModifier;

class CommitQueryWatchesPanel extends GenericPanel<User> {

    private static final long serialVersionUID = 1L;
    
    private SelectionColumn<QueryInfo, Void> selectionColumn;
    
    private final IModel<List<QueryInfo>> queryInfosModel = new LoadableDetachableModel<>() {

        @Override
        protected List<QueryInfo> load() {
            List<QueryInfo> queryInfos = new ArrayList<>();
            for (var personalization: getUser().getCommitQueryPersonalizations()) {
                for (var name: personalization.getQuerySubscriptions()) {            
                    NamedQuery query;            
                    if (name.startsWith(COMMON_NAME_PREFIX))
                        query = personalization.getProject().getNamedCommitQuery(name.substring(COMMON_NAME_PREFIX.length()));
                    else                   
                        query = personalization.getQuery(name.substring(PERSONAL_NAME_PREFIX.length()));
                    if (query != null) 
                        queryInfos.add(new QueryInfo(name, query.getQuery(), personalization.getProject().getId()));
                }
            }
            return queryInfos;
        }
    };

    public CommitQueryWatchesPanel(String id, IModel<User> model) {
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
                         for (var personalization: getUser().getCommitQueryPersonalizations()) {
                             if (personalization.getProject().getId().equals(queryInfo.projectId)) {
                                 personalization.getQuerySubscriptions().remove(queryInfo.name);
                                 getCommitQueryPersonalizationService().createOrUpdate(personalization);
                                 if (getPage() instanceof UserPage)
                                    auditService.audit(null, "unsubscribed from commit query \"" + queryInfo.name + "\" in account \"" + getUser().getName() + "\" in project \"" + personalization.getProject().getPath() + "\"", null, null);
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
                                        
                var fragment = new Fragment(componentId, "linkFrag", CommitQueryWatchesPanel.this);
                var project = getProjectService().load(rowModel.getObject().projectId);
                var link = new BookmarkablePageLink<Void>("link", ProjectCommitsPage.class, ProjectCommitsPage.paramsOf(project, rowModel.getObject().query, null));
                link.add(new Label("label", name));
                fragment.add(link);
                cellItem.add(fragment);
            }
        });

        columns.add(new AbstractColumn<>(Model.of(_T("Project"))) {

            @Override
            public void populateItem(Item<ICellPopulator<QueryInfo>> cellItem, String componentId,
                                     IModel<QueryInfo> rowModel) {
                var project = getProjectService().load(rowModel.getObject().projectId);
                var fragment = new Fragment(componentId, "linkFrag", CommitQueryWatchesPanel.this);
                var link = new BookmarkablePageLink<Void>("link", ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project));
                link.add(new Label("label", project.getPath()));
                fragment.add(link);
                cellItem.add(fragment);
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

    private CommitQueryPersonalizationService getCommitQueryPersonalizationService() {
        return OneDev.getInstance(CommitQueryPersonalizationService.class);
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
