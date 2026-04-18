package io.onedev.server.web.page.project.setting.cache;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RunCacheService;
import io.onedev.server.service.support.RunCacheInfo;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.indexedpath.IndexedPathsPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.page.project.setting.build.CacheSettingBean;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;

public class CacheManagementPage extends ProjectSettingPage {
	
	private static final String PARAM_PAGE = "page";
	
	private DataTable<RunCacheInfo, Void> cachesTable;
	
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
				auditService.audit(getProject(), "changed cache preserve days", oldAuditContent, newAuditContent);
			}
			
		};
		form.add(BeanContext.edit("editor", bean));
		add(form);

		List<IColumn<RunCacheInfo, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<>(Model.of(_T("Key"))) {

			@Override
			public void populateItem(Item<ICellPopulator<RunCacheInfo>> cellItem, String componentId, IModel<RunCacheInfo> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getKey()));
			}
		});

		columns.add(new AbstractColumn<>(Model.of(_T("Checksum"))) {

			@Override
			public void populateItem(Item<ICellPopulator<RunCacheInfo>> cellItem, String componentId, IModel<RunCacheInfo> rowModel) {
				var checksum = rowModel.getObject().getChecksum();
				if (checksum != null)
					cellItem.add(new Label(componentId, checksum));
				else
					cellItem.add(new Label(componentId, "<i>N/A</i>").setEscapeModelStrings(false));
			}
		});

		columns.add(new AbstractColumn<>(Model.of(_T("#Paths"))) {

			@Override
			public void populateItem(Item<ICellPopulator<RunCacheInfo>> cellItem, String componentId, IModel<RunCacheInfo> rowModel) {
				cellItem.add(new IndexedPathsPanel(componentId, Model.ofList(rowModel.getObject().getIndexedPaths())));
			}

		});

		columns.add(new AbstractColumn<>(Model.of(_T("Last Accessed"))) {

			@Override
			public void populateItem(Item<ICellPopulator<RunCacheInfo>> cellItem, String componentId,
									 IModel<RunCacheInfo> rowModel) {
				var info = rowModel.getObject();
				if (info.getLastAccessDate() != null)
					cellItem.add(new Label(componentId, DateUtils.formatDate(info.getLastAccessDate())));
				else
					cellItem.add(new Label(componentId, "<i>N/A</i>").setEscapeModelStrings(false));
			}

		});

		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<RunCacheInfo>> cellItem, String componentId, IModel<RunCacheInfo> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", CacheManagementPage.this);

				var cacheInfo = rowModel.getObject();
				String cacheName;
				var cacheChecksum = cacheInfo.getChecksum();
				if (cacheChecksum != null)
					cacheName = cacheInfo.getKey() + " (checksum: " + cacheChecksum + ")";
				else
					cacheName = cacheInfo.getKey();
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						getCacheService().deleteCache(getProject().getId(), cacheName, cacheChecksum);
						Session.get().success(MessageFormat.format(_T("Cache \"{0}\" deleted"), cacheName));

						target.add(cachesTable);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = MessageFormat.format(_T("Do you really want to delete cache \"{0}\"?"), cacheName);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

				});

				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}

		});

		SortableDataProvider<RunCacheInfo, Void> dataProvider = new SortableDataProvider<>() {

			private List<RunCacheInfo> listCaches() {
				return getCacheService().listCaches(getProject().getId());
			}

			@Override
			public Iterator<? extends RunCacheInfo> iterator(long first, long count) {
				var all = listCaches();
				int from = (int) Math.min(first, all.size());
				int to = (int) Math.min(first + count, all.size());
				return all.subList(from, to).iterator();
			}

			@Override
			public long size() {
				return listCaches().size();
			}

			@Override
			public IModel<RunCacheInfo> model(RunCacheInfo object) {
				return Model.of(object);
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
		return new Label(componentId, _T("Cache Management"));
	}
	
	private RunCacheService getCacheService() {
		return OneDev.getInstance(RunCacheService.class);
	}
	
}
