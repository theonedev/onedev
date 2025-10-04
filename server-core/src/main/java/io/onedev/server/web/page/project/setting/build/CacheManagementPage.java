package io.onedev.server.web.page.project.setting.build;

import static io.onedev.server.model.JobCache.PROP_PROJECT;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.JobCacheService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.JobCache;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;

public class CacheManagementPage extends ProjectBuildSettingPage {
	
	private static final String PARAM_PAGE = "page";
	
	private DataTable<JobCache, Void> cachesTable;
	
	public CacheManagementPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var bean = new CacheSettingBean();
		bean.setPreserveDays(getProject().getBuildSetting().getCachePreserveDays());
		var oldAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
		var form = new Form<Void>("cacheSetting") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
				getProject().getBuildSetting().setCachePreserveDays(bean.getPreserveDays());
				OneDev.getInstance(ProjectService.class).update(getProject());
				auditService.audit(getProject(), "changed job cache preserve days", oldAuditContent, newAuditContent);
			}
			
		};
		form.add(BeanContext.edit("editor", bean));
		add(form);

		List<IColumn<JobCache, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<>(Model.of(_T("Key"))) {

			@Override
			public void populateItem(Item<ICellPopulator<JobCache>> cellItem, String componentId, IModel<JobCache> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getKey()));
			}
		});
		
		columns.add(new AbstractColumn<>(Model.of(_T("Size"))) {

			@Override
			public void populateItem(Item<ICellPopulator<JobCache>> cellItem, String componentId,
									 IModel<JobCache> rowModel) {
				var cache = rowModel.getObject();
				var cacheSize = getCacheService().getCacheSize(cache.getProject().getId(), cache.getId());
				if (cacheSize != null)
					cellItem.add(new Label(componentId, FileUtils.byteCountToDisplaySize(cacheSize)));
				else 
					cellItem.add(new Label(componentId, "<i class='text-danger'>" + _T("File missing or obsolete") + "</i>").setEscapeModelStrings(false));
			}

		});

		columns.add(new AbstractColumn<>(Model.of(_T("Last Accessed"))) {

			@Override
			public void populateItem(Item<ICellPopulator<JobCache>> cellItem, String componentId,
									 IModel<JobCache> rowModel) {
				var cache = rowModel.getObject();
				cellItem.add(new Label(componentId, DateUtils.formatDate(cache.getAccessDate())));
			}

		});

		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobCache>> cellItem, String componentId, IModel<JobCache> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", CacheManagementPage.this);

				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						var cache = rowModel.getObject();
						getCacheService().delete(cache);
						Session.get().success(MessageFormat.format(_T("Job cache \"{0}\" deleted"), cache.getKey()));
						target.add(cachesTable);
					}
					
				});

				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}

		});

		SortableDataProvider<JobCache, Void> dataProvider = new SortableDataProvider<>() {

			private EntityCriteria<JobCache> newCriteria() {
				var criteria = EntityCriteria.of(JobCache.class);
				criteria.add(Restrictions.eq(PROP_PROJECT, getProject()));
				return criteria;
			}
			
			@Override
			public Iterator<? extends JobCache> iterator(long first, long count) {
				var criteria = newCriteria();
				criteria.addOrder(Order.desc(JobCache.PROP_ACCESS_DATE));
				return getCacheService().query(criteria, (int) first, (int) count).iterator();
			}

			@Override
			public long size() {
				return getCacheService().count(newCriteria());
			}

			@Override
			public IModel<JobCache> model(JobCache object) {
				Long id = object.getId();
				return new LoadableDetachableModel<>() {

					@Override
					protected JobCache load() {
						return getCacheService().load(id);
					}

				};
			}

		};

		PagingHistorySupport pagingHistorySupport = new ParamPagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = new PageParameters();
				params.add(PARAM_PAGE, currentPage+1);
				return params;
			}

			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}

		};

		add(cachesTable = new DefaultDataTable<>("caches", columns, dataProvider,
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Job Cache Management"));
	}
	
	private JobCacheService getCacheService() {
		return OneDev.getInstance(JobCacheService.class);
	}
	
}
