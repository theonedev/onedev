package io.onedev.server.web.component.codecomment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.collect.Sets;

import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.CodeCommentStatusChangeManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.search.entitytext.CodeCommentTextManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Provider;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.CodeCommentQueryBehavior;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
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
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.codecomments.InvalidCodeCommentPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public abstract class CodeCommentListPanel extends Panel {

	private final IModel<String> queryStringModel;
	
	private final IModel<Object> queryModel = new LoadableDetachableModel<Object>() {

		@Override
		protected Object load() {
			String queryString = queryStringModel.getObject();
			try {
				return CodeCommentQuery.parse(getProject(), queryString, true);
			} catch (ExplicitException e) {
				error(e.getMessage());
				return null;
			} catch (Exception e) {
				if (getPullRequest() != null) {
					error("Malformed code comment query");
					return null;
				} else {
					info("Performing fuzzy query");
					return queryString;
				}
			}
		}
		
	};
	
	private DataTable<CodeComment, Void> commentsTable;
	
	private SortableDataProvider<CodeComment, Void> dataProvider;
	
	private SelectionColumn<CodeComment, Void> selectionColumn;
	
	private TextField<String> queryInput;
	
	private Component saveQueryLink;
	
	private WebMarkupContainer body;
	
	private boolean querySubmitted = true;
	
	public CodeCommentListPanel(String id, IModel<String> queryModel) {
		super(id);
		this.queryStringModel = queryModel;
	}

	private CodeCommentManager getCodeCommentManager() {
		return OneDev.getInstance(CodeCommentManager.class);
	}
	
	private CodeCommentTextManager getCodeCommentTextManager() {
		return OneDev.getInstance(CodeCommentTextManager.class);
	}
	
	private void doQuery(AjaxRequestTarget target) {
		commentsTable.setCurrentPage(0);
		target.add(body);
		if (selectionColumn != null)
			selectionColumn.getSelections().clear();
		querySubmitted = true;
		if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
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
				setVisible(SecurityUtils.getUser() != null && getQuerySaveSupport() != null);
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

			private void changeStatus(AjaxRequestTarget target, 
					Provider<Collection<CodeComment>> commentsProvider, boolean resolved) {
				new BeanEditModalPanel<StatusChangeOptionBean>(target, new StatusChangeOptionBean()) {
					
					@Override
					protected String getCssClass() {
						return "code-comment-status-change-option";
					}

					@Override
					protected void onSave(AjaxRequestTarget target, StatusChangeOptionBean bean) {
						Collection<CodeCommentStatusChange> changes = new ArrayList<>();
						
						for (CodeComment comment: commentsProvider.get()) {
							CodeCommentStatusChange change = new CodeCommentStatusChange();
							change.setComment(comment);
							change.setCompareContext(comment.getCompareContext());
							change.setDate(new Date());
							change.setResolved(resolved);
							change.setUser(SecurityUtils.getUser());
							changes.add(change);
						}
						
						String note = bean.getNote();
						
						OneDev.getInstance(CodeCommentStatusChangeManager.class).save(changes, note);
						selectionColumn.getSelections().clear();
						target.add(body);
						
						close();
					}
					
				};
			}
			
			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Resolve Selected Comments";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								changeStatus(target, new Provider<Collection<CodeComment>>() {

									@Override
									public Collection<CodeComment> get() {
										return selectionColumn.getSelections().stream()
												.map(it->it.getObject())
												.collect(Collectors.toList());
									}
									
								}, true);
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
									tag.put("title", "Please select comments to resolve");
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Unresolve Selected Comments";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								changeStatus(target, new Provider<Collection<CodeComment>>() {

									@Override
									public Collection<CodeComment> get() {
										return selectionColumn.getSelections().stream()
												.map(it->it.getObject())
												.collect(Collectors.toList());
									}
									
								}, false);
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
									tag.put("title", "Please select comments to unresolve");
								}
							}
							
						};
					}
					
				});
				
				if (SecurityUtils.canManageCodeComments(getProject())) {
					menuItems.add(new MenuItem() {
	
						@Override
						public String getLabel() {
							return "Delete Selected Comments";
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
											Collection<CodeComment> comments = new ArrayList<>();
											for (IModel<CodeComment> each: selectionColumn.getSelections())
												comments.add(each.getObject());
											OneDev.getInstance(CodeCommentManager.class).delete(comments);
											selectionColumn.getSelections().clear();
											target.add(body);
										}
										
										@Override
										protected String getConfirmMessage() {
											return "Type <code>yes</code> below to delete selected issues";
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
										tag.put("title", "Please select issues to delete");
									}
								}
								
							};
						}
						
					});
				}
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Resolve All Queried Comments";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								changeStatus(target, new Provider<Collection<CodeComment>>() {

									@SuppressWarnings("unchecked")
									@Override
									public Collection<CodeComment> get() {
										Collection<CodeComment> comments = new ArrayList<>();
										for (Iterator<CodeComment> it = (Iterator<CodeComment>) dataProvider.iterator(0, commentsTable.getItemCount()); it.hasNext();) 
											comments.add(it.next());
										return comments;
									}
									
								}, true);
							}
							
							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(commentsTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "No comments to resolve");
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Unresolve All Queried Comments";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								changeStatus(target, new Provider<Collection<CodeComment>>() {

									@SuppressWarnings("unchecked")
									@Override
									public Collection<CodeComment> get() {
										Collection<CodeComment> comments = new ArrayList<>();
										for (Iterator<CodeComment> it = (Iterator<CodeComment>) dataProvider.iterator(0, commentsTable.getItemCount()); it.hasNext();) 
											comments.add(it.next());
										return comments;
									}
									
								}, false);
							}
							
							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(commentsTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "No comments to unresolve");
								}
							}
							
						};
					}
					
				});
				
				if (SecurityUtils.canManageCodeComments(getProject())) {
					menuItems.add(new MenuItem() {
	
						@Override
						public String getLabel() {
							return "Delete All Queried Comments";
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
											Collection<CodeComment> comments = new ArrayList<>();
											for (Iterator<CodeComment> it = (Iterator<CodeComment>) dataProvider.iterator(0, commentsTable.getItemCount()); it.hasNext();) 
												comments.add(it.next());
											OneDev.getInstance(CodeCommentManager.class).delete(comments);
											selectionColumn.getSelections().clear();
											target.add(body);
										}
										
										@Override
										protected String getConfirmMessage() {
											return "Type <code>yes</code> below to delete all queried comments";
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
									setEnabled(commentsTable.getItemCount() != 0);
								}
								
								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("title", "No comments to delete");
									}
								}
								
							};
						}
						
					});
				}				
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(selectionColumn != null);
			}

		});
		
		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				List<String> orderFields = new ArrayList<>(CodeComment.ORDER_FIELDS.keySet());
				
				return new OrderEditPanel<CodeComment>(id, orderFields, new IModel<List<EntitySort>> () {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						Object query = queryModel.getObject();
						CodeCommentListPanel.this.getFeedbackMessages().clear();
						if (query instanceof CodeCommentQuery) 
							return ((CodeCommentQuery)query).getSorts();
						else
							return new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						Object query = queryModel.getObject();
						CodeCommentListPanel.this.getFeedbackMessages().clear();
						if (!(query instanceof CodeCommentQuery))
							query = new CodeCommentQuery();
						((CodeCommentQuery)query).getSorts().clear();
						((CodeCommentQuery)query).getSorts().addAll(object);
						queryModel.setObject(query);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class); 
						target.add(queryInput);
						doQuery(target);
					}
					
				});
			}
			
		});	
		
		queryInput = new TextField<String>("input", queryStringModel);
		queryInput.add(new CodeCommentQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}, true, true) {
			
			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				CodeCommentListPanel.this.getFeedbackMessages().clear();
				querySubmitted = StringUtils.trimToEmpty(queryStringModel.getObject())
						.equals(StringUtils.trimToEmpty(inputContent));
				target.add(saveQueryLink);
			}
			
			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				List<String> hints = super.getHints(terminalExpect);
				if (getPullRequest() == null)
					hints.add("Free input for fuzzy query on path/comment");
				return hints;
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
				CodeCommentListPanel.this.getFeedbackMessages().clear();
				doQuery(target);
			}
			
		});
		add(queryForm);
		
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		body.add(new FencedFeedbackPanel("feedback", this));

		dataProvider = new LoadableDetachableDataProvider<CodeComment, Void>() {

			@Override
			public Iterator<? extends CodeComment> iterator(long first, long count) {
				Object query = queryModel.getObject();
				if (query instanceof CodeCommentQuery) {
					return getCodeCommentManager().query(getProject(), getPullRequest(), 
							(CodeCommentQuery)query, (int)first, (int)count).iterator();
				} else if (query instanceof String) {
					return getCodeCommentTextManager()
							.query(getProject(), (String)query, (int)first, (int)count).iterator();
				} else {
					return new ArrayList<CodeComment>().iterator();
				}
			}

			@Override
			public long calcSize() {
				try {
					Object query = queryModel.getObject();
					if (query instanceof CodeCommentQuery) {
						return getCodeCommentManager().count(getProject(), getPullRequest(), 
								((CodeCommentQuery)query).getCriteria());
					} else if (query instanceof String) {
						return getCodeCommentTextManager().count(getProject(), (String)query);
					}
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return 0;
			}

			@Override
			public IModel<CodeComment> model(CodeComment object) {
				Long commentId = object.getId();
				return new LoadableDetachableModel<CodeComment>() {

					@Override
					protected CodeComment load() {
						return OneDev.getInstance(CodeCommentManager.class).load(commentId);
					}
					
				};
			}
			
		};
		
		List<IColumn<CodeComment, Void>> columns = new ArrayList<>();
		
		if (SecurityUtils.canManageCodeComments(getProject())) {
			columns.add(selectionColumn = new SelectionColumn<CodeComment, Void>());
		} else if (getPullRequest() != null) {
			Collection<User> keyUsers = Sets.newHashSet(getPullRequest().getSubmitter());
			for (PullRequestReview review: getPullRequest().getReviews())
				keyUsers.add(review.getUser());
			for (PullRequestAssignment assignment: getPullRequest().getAssignments())
				keyUsers.add(assignment.getUser());
			if (keyUsers.contains(SecurityUtils.getUser()))
				columns.add(selectionColumn = new SelectionColumn<CodeComment, Void>());				
		} 
		
		columns.add(new AbstractColumn<CodeComment, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem, String componentId, IModel<CodeComment> rowModel) {
				cellItem.add(new Label(componentId, ""));
			}

			@Override
			public String getCssClass() {
				return "new-indicator";
			}
			
		});
		
		columns.add(new AbstractColumn<CodeComment, Void>(Model.of("File")) {

			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem, String componentId, IModel<CodeComment> rowModel) {
				Fragment fragment = new Fragment(componentId, "fileFrag", CodeCommentListPanel.this);
				CodeComment comment = rowModel.getObject();
				WebMarkupContainer link;
				if (!comment.isValid()) {
					link = new ActionablePageLink("link", InvalidCodeCommentPage.class, 
							InvalidCodeCommentPage.paramsOf(comment)) {

						@Override
						protected void doBeforeNav(AjaxRequestTarget target) {
							String redirectUrlAfterDelete = RequestCycle.get().urlFor(
									getPage().getClass(), getPage().getPageParameters()).toString();
							WebSession.get().setRedirectUrlAfterDelete(CodeComment.class, redirectUrlAfterDelete);
						}
						
					};
				} else {
					String url = OneDev.getInstance(UrlManager.class).urlFor(comment);
					link = new ExternalLink("link", UrlUtils.makeRelative(url));
				}
				link.add(new Label("label", comment.getMark().getPath()));
				fragment.add(link);
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "text-break";
			}
			
		});
		
		columns.add(new AbstractColumn<CodeComment, Void>(Model.of("Status")) {

			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem, String componentId, IModel<CodeComment> rowModel) {
				CodeComment comment = rowModel.getObject();
				String label;
				if (comment.isResolved()) {
					label = String.format(
							"<svg class='icon text-success mr-1'><use xlink:href='%s'/></svg> %s", 
							SpriteImage.getVersionedHref("tick-circle-o"), "Resolved");
				} else {
					label = String.format(
							"<svg class='icon text-warning mr-1'><use xlink:href='%s'/></svg> %s", 
							SpriteImage.getVersionedHref("dot"), "Unresolved");
				}
				cellItem.add(new Label(componentId, label).setEscapeModelStrings(false));
			}

		});
		
		columns.add(new AbstractColumn<CodeComment, Void>(Model.of("Last Update")) {

			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem, String componentId, IModel<CodeComment> rowModel) {
				CodeComment comment = rowModel.getObject();
				
				Fragment fragment = new Fragment(componentId, "lastUpdateFrag", CodeCommentListPanel.this);
				
				LastUpdate lastUpdate = comment.getLastUpdate();
				if (lastUpdate.getUser() != null) {
					fragment.add(new UserIdentPanel("user", lastUpdate.getUser(), Mode.NAME));
				} else {
					fragment.add(new WebMarkupContainer("user").setVisible(false));
				}
				fragment.add(new Label("activity", lastUpdate.getActivity()));
				fragment.add(new Label("date", DateUtils.formatAge(lastUpdate.getDate()))
					.add(new AttributeAppender("title", DateUtils.formatDateTime(lastUpdate.getDate()))));
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "d-none d-md-table-cell";
			}

		});
		
		body.add(commentsTable = new DefaultDataTable<CodeComment, Void>("comments", columns, dataProvider, 
				WebConstants.PAGE_SIZE, getPagingHistorySupport()) {

			@Override
			protected Item<CodeComment> newRowItem(String id, int index, IModel<CodeComment> model) {
				Item<CodeComment> item = super.newRowItem(id, index, model);
				CodeComment comment = model.getObject();
				item.add(AttributeAppender.append("class", 
						comment.isVisitedAfter(comment.getLastUpdate().getDate())?"comment":"comment new"));
				return item;
			}
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CodeCommentCssResourceReference()));
	}

	@Override
	protected void onDetach() {
		queryStringModel.detach();
		queryModel.detach();
		super.onDetach();
	}

	protected abstract Project getProject();
	
	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}

	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
}
