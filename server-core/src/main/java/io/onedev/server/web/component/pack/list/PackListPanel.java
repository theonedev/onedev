package io.onedev.server.web.component.pack.list;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.pack.FuzzyCriteria;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.search.entity.pack.PackQueryLexer;
import io.onedev.server.search.entity.pack.TypeCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.PackQueryBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.entity.labels.EntityLabelsPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.orderedit.OrderEditPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.packs.ProjectPacksPage;
import io.onedev.server.web.page.project.packs.detail.PackDetailPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("serial")
public abstract class PackListPanel extends Panel {
	
	private final IModel<String> queryStringModel;
	
	private final boolean showType;
	
	private final IModel<PackQuery> queryModel = new LoadableDetachableModel<>() {

		@Override
		protected PackQuery load() {
			return parse(queryStringModel.getObject(), getBaseQuery(), getPackType());
		}

	};
	
	private Component countLabel;
	
	private DataTable<Pack, Void> packsTable;
	
	private SelectionColumn<Pack, Void> selectionColumn;
	
	private LoadableDetachableDataProvider<Pack, Void> dataProvider;	
	
	private TextField<String> queryInput;
	
	private WebMarkupContainer body;
	
	private Component saveQueryLink;
	
	private boolean querySubmitted = true;
	
	public PackListPanel(String id, IModel<String> queryModel, boolean showType) {
		super(id);
		this.queryStringModel = queryModel;
		this.showType = showType;
	}
	
	private PackManager getPackManager() {
		return OneDev.getInstance(PackManager.class);
	}
	
	@Nullable
	private PackQuery parse(@Nullable String queryString, PackQuery baseQuery, @Nullable String packType) {
		PackQuery parsedQuery;
		try {
			parsedQuery = PackQuery.parse(getProject(), queryString, true);
		} catch (Exception e) {
			getFeedbackMessages().clear();
			if (e instanceof ExplicitException) {
				error(e.getMessage());
				return null;
			} else {
				info("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"Type\" is \"NPM\"");
				parsedQuery = new PackQuery(new FuzzyCriteria(queryString));
			}
		}
		var query = PackQuery.merge(baseQuery, parsedQuery);
		if (packType != null)
			query = PackQuery.merge(query, new PackQuery(new TypeCriteria(packType, PackQueryLexer.Is)));
		return query;			
	}
	
	@Override
	protected void onDetach() {
		queryStringModel.detach();
		queryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected abstract Project getProject();
	
	@Nullable
	protected String getPackType() {
		return null;
	}

	protected PackQuery getBaseQuery() {
		return new PackQuery();
	}

	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}
	
	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}
	
	private void doQuery(AjaxRequestTarget target) {
		packsTable.setCurrentPage(0);
		target.add(countLabel);
		target.add(body);
		if (selectionColumn != null)
			selectionColumn.getSelections().clear();
		querySubmitted = true;
		if (SecurityUtils.getAuthUser() != null && getQuerySaveSupport() != null)
			target.add(saveQueryLink);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("showSavedQueries") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SavedQueriesClosed) {
					((SavedQueriesClosed) event.getPayload()).getHandler().add(this);
				}
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuerySaveSupport() != null && !getQuerySaveSupport().isSavedQueriesVisible());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				send(getPage(), Broadcast.BREADTH, new SavedQueriesOpened(target));
				target.add(this);
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(saveQueryLink = new AjaxLink<Void>("saveQuery") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(querySubmitted && queryModel.getObject() != null);
				setVisible(SecurityUtils.getAuthUser() != null && getQuerySaveSupport() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) 
					tag.append("class", "disabled", " ");
				if (!querySubmitted)
					tag.put("title", "Query not submitted");
				else if (queryModel.getObject() == null)
					tag.put("title", "Can not save malformed query");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getQuerySaveSupport().onSaveQuery(target, queryModel.getObject().toString());
			}		
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(new MenuLink("operations") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Delete Selected Packages";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								new ConfirmModalPanel(target) {
									
									@Override
									protected void onConfirm(AjaxRequestTarget target) {
										Collection<Pack> packs = new ArrayList<>();
										for (IModel<Pack> each: selectionColumn.getSelections())
											packs.add(each.getObject());
										OneDev.getInstance(PackManager.class).delete(packs);
										target.add(countLabel);
										target.add(body);
										selectionColumn.getSelections().clear();
									}
									
									@Override
									protected String getConfirmMessage() {
										return "Type <code>yes</code> below to delete selected packages";
									}
									
									@Override
									protected String getConfirmInput() {
										return "yes";
									}
									
								};
								
							}
							
							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(!selectionColumn.getSelections().isEmpty());
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "Please select packages to delete");
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Delete All Queried Packages";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@SuppressWarnings("unchecked")
							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								new ConfirmModalPanel(target) {
									
									@Override
									protected void onConfirm(AjaxRequestTarget target) {
										Collection<Pack> packs = new ArrayList<>();
										for (Iterator<Pack> it = (Iterator<Pack>) dataProvider.iterator(0, packsTable.getItemCount()); it.hasNext();) {
											packs.add(it.next());
										}
										OneDev.getInstance(PackManager.class).delete(packs);
										dataProvider.detach();
										target.add(countLabel);
										target.add(body);
										selectionColumn.getSelections().clear();
									}
									
									@Override
									protected String getConfirmMessage() {
										return "Type <code>yes</code> below to delete all queried packages";
									}
									
									@Override
									protected String getConfirmInput() {
										return "yes";
									}
									
								};
								
							}
							
							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(packsTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "No packages to delete");
								}
							}
							
						};
					}
					
				});
				
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject() != null && SecurityUtils.canWritePack(getProject()));
			}
			
		});
		
		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				List<String> orderFields = new ArrayList<>(Pack.ORDER_FIELDS.keySet());
				if (getProject() != null)
					orderFields.remove(Pack.NAME_PROJECT);
				
				return new OrderEditPanel<Pack>(id, orderFields, new IModel<>() {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						PackQuery query = parse(queryStringModel.getObject(), new PackQuery(), null);
						PackListPanel.this.getFeedbackMessages().clear();
						if (query != null)
							return query.getSorts();
						else
							return new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						PackQuery query = parse(queryStringModel.getObject(), new PackQuery(), null);
						PackListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new PackQuery();
						query.getSorts().clear();
						query.getSorts().addAll(object);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
						target.add(queryInput);
						doQuery(target);
					}

				});
			}
			
		});	
		
		queryInput = new TextField<>("input", queryStringModel);
		queryInput.add(new PackQueryBehavior(new AbstractReadOnlyModel<>() {

			@Override
			public Project getObject() {
				return getProject();
			}

		}, getPackType(), true, true, true) {
			
			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				PackListPanel.this.getFeedbackMessages().clear();
				querySubmitted = StringUtils.trimToEmpty(queryStringModel.getObject())
						.equals(StringUtils.trimToEmpty(inputContent));
				target.add(saveQueryLink);
			}
			
		});
		
		queryInput.add(new AjaxFormComponentUpdatingBehavior("clear") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				doQuery(target);
			}
			
		});
		
		Form<?> queryForm = new Form<Void>("query");
		queryForm.add(queryInput);
		queryForm.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				PackListPanel.this.getFeedbackMessages().clear();
				doQuery(target);
			}
			
		});
		add(queryForm);

		add(countLabel = new Label("count", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (dataProvider.size() > 1)
					return "found " + dataProvider.size() + " packages";
				else
					return "found 1 package";
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(dataProvider.size() != 0);
			}
		}.setOutputMarkupPlaceholderTag(true));
		
		dataProvider = new LoadableDetachableDataProvider<>() {

			@Override
			public Iterator<? extends Pack> iterator(long first, long count) {
				try {
					return getPackManager().query(getProject(), queryModel.getObject(), 
							true, (int) first, (int) count).iterator();
				} catch (ExplicitException e) {
					error(e.getMessage());
					return new ArrayList<Pack>().iterator();
				}
			}

			@Override
			public long calcSize() {
				PackQuery query = queryModel.getObject();
				if (query != null) {
					try {
						return getPackManager().count(getProject(), query.getCriteria());
					} catch (ExplicitException e) {
						error(e.getMessage());
					}
				}
				return 0;
			}

			@Override
			public IModel<Pack> model(Pack object) {
				Long packId = object.getId();
				return new LoadableDetachableModel<>() {

					@Override
					protected Pack load() {
						return getPackManager().load(packId);
					}

				};
			}

		};
		
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<Pack, Void>> columns = new ArrayList<>();
		
		if (getProject() != null && SecurityUtils.canWritePack(getProject())) 
			columns.add(selectionColumn = new SelectionColumn<>());

		columns.add(new AbstractColumn<>(Model.of("Package")) {

			@Override
			public String getCssClass() {
				return "pack";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Pack>> cellItem, String componentId, IModel<Pack> rowModel) {
				Fragment fragment = new Fragment(componentId, "packFrag", PackListPanel.this);
				Pack pack = rowModel.getObject();

				WebMarkupContainer link = new ActionablePageLink("link",
						PackDetailPage.class, PackDetailPage.paramsOf(pack)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						OddEvenItem<?> row = cellItem.findParent(OddEvenItem.class);
						Cursor cursor = new Cursor(queryModel.getObject().toString(), (int) packsTable.getItemCount(),
								(int) packsTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex(), getProject());
						WebSession.get().setPackCursor(cursor);

						String directUrlAfterDelete = RequestCycle.get().urlFor(
								getPage().getClass(), getPage().getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Pack.class, directUrlAfterDelete);
					}

				};
				
				link.add(new SpriteImage("icon", pack.getSupport().getPackIcon()));
				if (getProject() == null)
					link.add(new Label("label", pack.getReference(true)));
				else
					link.add(new Label("label", pack.getReference(false)));

				fragment.add(new EntityLabelsPanel<>("labels", rowModel));
				
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		if (showType) {
			columns.add(new AbstractColumn<>(Model.of("Type")) {

				@Override
				public String getCssClass() {
					return "type d-none d-lg-table-cell";
				}

				@Override
				public void populateItem(Item<ICellPopulator<Pack>> cellItem, String componentId, IModel<Pack> rowModel) {
					cellItem.add(new Label(componentId, rowModel.getObject().getType()));
				}
			});
		}

		columns.add(new AbstractColumn<>(Model.of("Last Published")) {

			@Override
			public String getCssClass() {
				return "date d-none d-lg-table-cell";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Pack>> cellItem, String componentId, IModel<Pack> rowModel) {
				cellItem.add(new Label(componentId, DateUtils.formatAge(rowModel.getObject().getPublishDate())));
			}
		});

		columns.add(new AbstractColumn<>(Model.of("Total Size")) {

			@Override
			public String getCssClass() {
				return "size d-none d-lg-table-cell";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Pack>> cellItem, String componentId, IModel<Pack> rowModel) {
				cellItem.add(new Label(componentId, FileUtils.byteCountToDisplaySize(rowModel.getObject().getSize())));
			}
			
		});

		body.add(packsTable = new DefaultDataTable<>("packs", columns, dataProvider,
				WebConstants.PAGE_SIZE, getPagingHistorySupport()) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!shouldShowHelp());
			}
		});
		
		body.add(new WebMarkupContainer("help") {
			@Override
			protected void onBeforeRender() {
				var packSupports = new ArrayList<>(OneDev.getExtensions(PackSupport.class));
				packSupports.sort(Comparator.comparing(PackSupport::getOrder));
				List<Tab> tabs = new ArrayList<>();
				for (var packSupport: packSupports) {
					tabs.add(new AjaxActionTab(Model.of(packSupport.getPackType()), Model.of(packSupport.getPackIcon())) {
						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							var helpContent = packSupport.renderHelp("content", getProject())	;
							helpContent.setOutputMarkupId(true);
							replace(helpContent);
							target.add(helpContent);
						}

					});
				}
				addOrReplace(new Tabbable("tabs", tabs));
				
				var helpContent = packSupports.iterator().next().renderHelp("content", getProject());
				helpContent.setOutputMarkupId(true);
				addOrReplace(helpContent);

				super.onBeforeRender();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(shouldShowHelp());
			}
		});
		
		setOutputMarkupId(true);
	}
	
	private boolean shouldShowHelp() {
		return dataProvider.size() == 0 && getPage() instanceof ProjectPacksPage
				&& queryModel.getObject() != null && queryModel.getObject().getCriteria() == null;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PackListCssResourceReference()));
	}
}
