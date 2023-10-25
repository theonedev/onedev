package io.onedev.server.web.page.project.packs;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.PackSupport;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.PagingHistorySupport;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.*;

public class PackListPage extends ProjectPage {
	
	private static final String PARAM_PAGE = "page";

	private static final String PARAM_TYPE = "type";
	
	private static final String PARAM_QUERY = "query";
	
	private TextField<String> searchField;

	private DataTable<Pack, Void> packsTable;

	private State state = new State();

	private boolean typing;

	public PackListPage(PageParameters params) {
		super(params);
		state.type = params.get(PARAM_TYPE).toString();
		state.query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onBeforeRender() {
		typing = false;
		super.onBeforeRender();
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		state = (State) data;
		getPageParameters().set(PARAM_QUERY, state.query);
		target.add(searchField);
		target.add(packsTable);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PackCssResourceReference()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(searchField = new TextField<>("filterPacks", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.query;
			}

			@Override
			public void setObject(String object) {
				state.query = object;
				PageParameters params = paramsOf(getProject(), state);
				params.remove(PARAM_PAGE);

				String url = urlFor(PackListPage.class, params).toString();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (typing)
					replaceState(target, url, state);
				else
					pushState(target, url, state);

				packsTable.setCurrentPage(0);
				target.add(packsTable);

				typing = true;
			}

		}));

		searchField.add(new OnTypingDoneBehavior(250) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
			}

		});
		
		add(new StringSingleChoice("type", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.type;
			}

			@Override
			public void setObject(String object) {
				state.type = object;
			}
		}, new LoadableDetachableModel<>() {
			@Override
			protected Map<String, String> load() {
				var map = new LinkedHashMap<String, String>();
				ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
				var implementations = new ArrayList<Class<?>>();
				implementations.addAll(registry.getImplementations(PackSupport.class));
				EditableUtils.sortAnnotatedElements(implementations);
				for (var implementation: implementations) {
					var type = EditableUtils.getDisplayName(implementation);
					map.put(type, type);
				}
				return map;
			}
		}, false).add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				var url = urlFor(PackListPage.class, paramsOf(getProject(), state));
				pushState(target, url.toString(), state);
				packsTable.setCurrentPage(0);
				target.add(packsTable);
			}

		}));

		add(new Link<Void>("addNew") {

			@Override
			public void onClick() {
				setResponsePage(NewPackPage.class, NewPackPage.paramsOf(getProject()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}

		});

		List<IColumn<Pack, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Pack>> cellItem, String componentId, IModel<Pack> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", PackListPage.this);
				Pack pack = rowModel.getObject();
				WebMarkupContainer link = new ActionablePageLink("link",
						PackDetailPage.class, PackDetailPage.paramsOf(pack)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								PackListPage.class, getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Pack.class, redirectUrlAfterDelete);
					}

				};
				link.add(new Label("label", pack.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});

		columns.add(new AbstractColumn<>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<Pack>> cellItem, String componentId, IModel<Pack> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getType()));
			}
		});
		
		SortableDataProvider<Pack, Void> dataProvider = new SortableDataProvider<>() {

			@Override
			public Iterator<? extends Pack> iterator(long first, long count) {
				return OneDev.getInstance(PackManager.class).query(
						getProject(), state.type, state.query, (int) first, (int) count).iterator();
			}

			@Override
			public long size() {
				return OneDev.getInstance(PackManager.class).count(getProject(), state.type, state.query);
			}

			@Override
			public IModel<Pack> model(Pack object) {
				Long id = object.getId();
				return new LoadableDetachableModel<>() {

					@Override
					protected Pack load() {
						return OneDev.getInstance(PackManager.class).load(id);
					}

				};
			}
		};

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), state);
				params.add(PARAM_PAGE, currentPage+1);
				return params;
			}

			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}

		};

		add(packsTable = new DefaultDataTable<>("packs", columns, dataProvider,
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	public static PageParameters paramsOf(Project project, State state) {
		var params = paramsOf(project);
		if (state.type != null)
			params.add(PARAM_TYPE, state.type);
		if (state.query != null)
			params.add(PARAM_QUERY, state.query);
		return params;
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isPackManagement())
			return new BookmarkablePageLink<>(componentId, PackListPage.class, PackListPage.paramsOf(project));
		else
			return new BookmarkablePageLink<>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project));			
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Packages");
	}
	
	public static class State implements Serializable {
		
		String type;
		
		String query;
	}
}
