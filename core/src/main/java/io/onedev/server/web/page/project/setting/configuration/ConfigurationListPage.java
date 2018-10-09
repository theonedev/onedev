package io.onedev.server.web.page.project.setting.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.ConfigurationManager;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.support.configuration.DoNotCleanup;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;

@SuppressWarnings("serial")
public class ConfigurationListPage extends ProjectSettingPage {

	public ConfigurationListPage(PageParameters params) {
		super(params);
	}

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private DataTable<Configuration, Void> configurationsTable;
	
	private String searchInput;
	
	private EntityCriteria<Configuration> getCriteria() {
		EntityCriteria<Configuration> criteria = EntityCriteria.of(Configuration.class);
		if (searchInput != null) 
			criteria.add(Restrictions.ilike("name", searchInput, MatchMode.ANYWHERE));
		return criteria;
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("filterConfigurations", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(configurationsTable);
			}

		});
		
		add(new Link<Void>("addNew") {

			@Override
			public void onClick() {
				setResponsePage(NewConfigurationPage.class, NewConfigurationPage.paramsOf(getProject()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		List<IColumn<Configuration, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Configuration, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Configuration>> cellItem, String componentId,
					IModel<Configuration> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", ConfigurationListPage.this);
				Configuration configuration = rowModel.getObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", ConfigurationEditPage.class, 
						ConfigurationEditPage.paramsOf(configuration));
				link.add(new Label("label", configuration.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
			
		});
		
		columns.add(new AbstractColumn<Configuration, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<Configuration>> cellItem, String componentId, IModel<Configuration> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", ConfigurationListPage.this);
				fragment.add(AttributeAppender.append("class", "actions"));
				
				Configuration configuration = rowModel.getObject();
				
				fragment.add(new Link<Void>("delete") {

					@Override
					public void onClick() {
						OneDev.getInstance(ConfigurationManager.class).delete(rowModel.getObject());
						setResponsePage(ConfigurationListPage.class, ConfigurationListPage.paramsOf(getProject()));
					}

				}.add(new ConfirmOnClick("Do you really want to delete configuration '" + configuration.getName() + "'?")));
				
				fragment.add(new AjaxLink<Void>("cleanup") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = "Do you really want to clean up builds of configuration '" + configuration.getName() + "'?";
						attributes.getAjaxCallListeners().add(new ConfirmListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						OneDev.getInstance(BuildManager.class).cleanupBuilds(rowModel.getObject());
						Session.get().success("Builds cleaned up");
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!(rowModel.getObject().getBuildCleanupRule() instanceof DoNotCleanup));
					}

				});
				
				cellItem.add(fragment);
			}
		});
		
		SortableDataProvider<Configuration, Void> dataProvider = new LoadableDetachableDataProvider<Configuration, Void>() {

			@Override
			public Iterator<? extends Configuration> iterator(long first, long count) {
				EntityCriteria<Configuration> criteria = getCriteria();
				criteria.addOrder(Order.asc("name"));
				return OneDev.getInstance(ConfigurationManager.class).query(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long calcSize() {
				return OneDev.getInstance(ConfigurationManager.class).count(getCriteria());
			}

			@Override
			public IModel<Configuration> model(Configuration object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Configuration>() {

					@Override
					protected Configuration load() {
						return OneDev.getInstance(ConfigurationManager.class).load(id);
					}
					
				};
			}
		};
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = new PageParameters();
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(configurationsTable = new HistoryAwareDataTable<Configuration, Void>("configurations", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ConfigurationCssResourceReference()));
	}

}
