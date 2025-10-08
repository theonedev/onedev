package io.onedev.server.web.component.codecomment;

import static io.onedev.server.model.CodeComment.SORT_FIELDS;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

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

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.service.CodeCommentStatusChangeService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.search.entity.codecomment.FuzzyCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Provider;
import io.onedev.server.web.UrlService;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.CodeCommentQueryBehavior;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.sortedit.SortEditPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.xodus.VisitInfoService;

public abstract class CodeCommentListPanel extends Panel {

	private static final int MAX_DESCRIPTION_LEN = 200;
	
	private final IModel<String> queryStringModel;
	
	private final IModel<CodeCommentQuery> queryModel = new LoadableDetachableModel<>() {

		@Override
		protected CodeCommentQuery load() {
			String queryString = queryStringModel.getObject();
			try {
				return CodeCommentQuery.parse(getProject(), queryString, true);
			} catch (Exception e) {
				getFeedbackMessages().clear();
				if (e instanceof ExplicitException) {
					error(e.getMessage());
					return null;
				} else {
					info(_T("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and unresolved"));
					return new CodeCommentQuery(new FuzzyCriteria(queryString));
				}
			}
		}
		
	};
	
	private Component countLabel;
	
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

	private CodeCommentService getCodeCommentService() {
		return OneDev.getInstance(CodeCommentService.class);
	}
	
	private void doQuery(AjaxRequestTarget target) {
		commentsTable.setCurrentPage(0);
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
					tag.put("data-tippy-content", _T("Query not submitted"));
				else if (queryModel.getObject() == null)
					tag.put("data-tippy-content", _T("Can not save malformed query"));
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
					protected String onSave(AjaxRequestTarget target, StatusChangeOptionBean bean) {
						Collection<CodeCommentStatusChange> changes = new ArrayList<>();
						
						for (CodeComment comment: commentsProvider.get()) {
							CodeCommentStatusChange change = new CodeCommentStatusChange();
							change.setComment(comment);
							change.setCompareContext(comment.getCompareContext());
							change.setDate(new Date());
							change.setResolved(resolved);
							change.setUser(SecurityUtils.getAuthUser());
							changes.add(change);
						}
						
						String note = bean.getNote();
						
						OneDev.getInstance(CodeCommentStatusChangeService.class).create(changes, note);
						selectionColumn.getSelections().clear();
						dataProvider.detach();
						target.add(countLabel);
						target.add(body);
						
						close();
						
						return null;
					}
					
				};
			}
			
			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Set Selected Comments as Resolved");
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
									tag.put("data-tippy-content", _T("Please select comments to set resolved"));
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Set Selected Comments as Unresolved");
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
									tag.put("data-tippy-content", _T("Please select comments to set unresolved"));
								}
							}
							
						};
					}
					
				});
				
				if (SecurityUtils.canManageCodeComments(getProject())) {
					menuItems.add(new MenuItem() {
	
						@Override
						public String getLabel() {
							return _T("Delete Selected Comments");
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
											OneDev.getInstance(CodeCommentService.class).delete(comments, getProject());
											selectionColumn.getSelections().clear();
											target.add(countLabel);
											target.add(body);
										}
										
										@Override
										protected String getConfirmMessage() {
											return _T("Type <code>yes</code> below to delete selected comments");
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
										tag.put("data-tippy-content", _T("Please select comments to delete"));
									}
								}
								
							};
						}
						
					});
				}
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Set All Queried Comments as Resolved");
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								changeStatus(target, (Provider<Collection<CodeComment>>) () -> {
									Collection<CodeComment> comments = new ArrayList<>();
									for (@SuppressWarnings("unchecked")
									Iterator<CodeComment> it = (Iterator<CodeComment>) dataProvider.iterator(0, commentsTable.getItemCount()); it.hasNext();) 
										comments.add(it.next());
									return comments;
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
									tag.put("data-tippy-content", _T("No comments to set resolved"));
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Set All Queried Comments as Unresolved");
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								changeStatus(target, (Provider<Collection<CodeComment>>) () -> {
									Collection<CodeComment> comments = new ArrayList<>();
									for (@SuppressWarnings("unchecked")
									Iterator<CodeComment> it = (Iterator<CodeComment>) dataProvider.iterator(0, commentsTable.getItemCount()); it.hasNext();) 
										comments.add(it.next());
									return comments;
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
									tag.put("data-tippy-content", _T("No comments to set unresolved"));
								}
							}
							
						};
					}
					
				});
				
				if (SecurityUtils.canManageCodeComments(getProject())) {
					menuItems.add(new MenuItem() {
	
						@Override
						public String getLabel() {
							return _T("Delete All Queried Comments");
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
											OneDev.getInstance(CodeCommentService.class).delete(comments, getProject());
											dataProvider.detach();
											selectionColumn.getSelections().clear();
											target.add(countLabel);
											target.add(body);
										}
										
										@Override
										protected String getConfirmMessage() {
											return _T("Type <code>yes</code> below to delete all queried comments");
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
										tag.put("data-tippy-content", _T("No comments to delete"));
									}
								}
								
							};
						}
						
					});
				}

				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Set All Queried Comments as Read");
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

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
									tag.put("data-tippy-content", _T("No comments to set as read"));
								}
							}

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								var visitInfoService = OneDev.getInstance(VisitInfoService.class);
								for (@SuppressWarnings("unchecked")
								Iterator<CodeComment> it = (Iterator<CodeComment>) dataProvider.iterator(0, commentsTable.getItemCount()); it.hasNext(); )
									visitInfoService.visitCodeComment(SecurityUtils.getAuthUser(), it.next());
								target.add(body);
							}

						};
					}

				});
				
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(selectionColumn != null);
			}

		});
		
		add(new DropdownLink("filter") {
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new CodeCommentFilterPanel(id, new IModel<EntityQuery<CodeComment>>() {
					@Override
					public void detach() {
					}
					@Override
					public EntityQuery<CodeComment> getObject() {
						return queryModel.getObject()!=null? queryModel.getObject() : new CodeCommentQuery();
					}
					@Override
					public void setObject(EntityQuery<CodeComment> object) {
						CodeCommentListPanel.this.getFeedbackMessages().clear();
						queryModel.setObject((CodeCommentQuery) object);
						queryStringModel.setObject(object.toString());
						var target = RequestCycle.get().find(AjaxRequestTarget.class);
						target.add(queryInput);
						doQuery(target);	
					}
				});
			}
		});

		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Map<String, Direction> sortFields = new LinkedHashMap<>();
				for (var entry: SORT_FIELDS.entrySet())
					sortFields.put(entry.getKey(), entry.getValue().getDefaultDirection());
				
				return new SortEditPanel<CodeComment>(id, sortFields, new IModel<List<EntitySort>> () {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						var query = queryModel.getObject();
						return query!=null? query.getSorts() : new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						var query = queryModel.getObject();
						CodeCommentListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new CodeCommentQuery();
						query.setSorts(object);
						queryModel.setObject(query);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class); 
						target.add(queryInput);
						doQuery(target);
					}
					
				});
			}
			
		});	
				
		queryInput = new TextField<>("input", queryStringModel);
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

		add(countLabel = new Label("count", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (dataProvider.size() > 1)
					return MessageFormat.format(_T("found {0} comments"), dataProvider.size());
				else
					return _T("found 1 comment");
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
			public Iterator<? extends CodeComment> iterator(long first, long count) {
				var query = queryModel.getObject();
				if (query != null) {
					return getCodeCommentService().query(getProject(), getPullRequest(),
							query, (int) first, (int) count).iterator();
				} else {
					return new ArrayList<CodeComment>().iterator();
				}
			}

			@Override
			public long calcSize() {
				try {
					var query = queryModel.getObject();
					if (query != null)
						return getCodeCommentService().count(getProject(), getPullRequest(), query.getCriteria());
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return 0;
			}

			@Override
			public IModel<CodeComment> model(CodeComment object) {
				Long commentId = object.getId();
				return new LoadableDetachableModel<>() {

					@Override
					protected CodeComment load() {
						return OneDev.getInstance(CodeCommentService.class).load(commentId);
					}

				};
			}

		};
		
		List<IColumn<CodeComment, Void>> columns = new ArrayList<>();
		
		if (SecurityUtils.canManageCodeComments(getProject())) {
			columns.add(selectionColumn = new SelectionColumn<>());
		} else if (getPullRequest() != null) {
			Collection<User> keyUsers = Sets.newHashSet(getPullRequest().getSubmitter());
			for (PullRequestReview review: getPullRequest().getReviews())
				keyUsers.add(review.getUser());
			for (PullRequestAssignment assignment: getPullRequest().getAssignments())
				keyUsers.add(assignment.getUser());
			if (keyUsers.contains(SecurityUtils.getAuthUser()))
				columns.add(selectionColumn = new SelectionColumn<>());				
		} 
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem, String componentId, IModel<CodeComment> rowModel) {
				cellItem.add(new Label(componentId, ""));
			}

			@Override
			public String getCssClass() {
				return "new-indicator";
			}

		});
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem, String componentId, IModel<CodeComment> rowModel) {
				CodeComment comment = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "contentFrag", CodeCommentListPanel.this);
				var commentId = comment.getId();
				fragment.add(new Label("status", new LoadableDetachableModel<String>() {
					@Override
					protected String load() {
						if (rowModel.getObject().isResolved()) {
							return String.format(
									"<span data-tippy-content=\"" + _T("Resolved") + "\"><svg class='icon text-success mr-1'><use xlink:href='%s'/></svg></span>",
									SpriteImage.getVersionedHref("tick-circle-o"));
						} else {
							return String.format(
									"<span data-tippy-content=\"" + _T("Unresolved") + "\"><svg class='icon text-warning mr-1'><use xlink:href='%s'/></svg></span>",
									SpriteImage.getVersionedHref("dot"));
						}
					}
				}) {
					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new ChangeObserver() {
							@Override
							public Collection<String> findObservables() {
								return Sets.newHashSet(CodeComment.getChangeObservable(commentId));
							}

						});
						setOutputMarkupId(true);
						setEscapeModelStrings(false);
					}
				});

				String url = OneDev.getInstance(UrlService.class).urlFor(comment, false);
				var link = new ExternalLink("description", url);
				link.add(new Label("label", StringUtils.abbreviate(comment.getContent(), MAX_DESCRIPTION_LEN)));
				fragment.add(link);

				fragment.add(new Label("file", MessageFormat.format(_T("on file {0}"), comment.getMark().getPath())));

				LastActivity lastActivity = comment.getLastActivity();
				if (lastActivity.getUser() != null) {
					fragment.add(new UserIdentPanel("user", lastActivity.getUser(), Mode.NAME));
				} else {
					fragment.add(new WebMarkupContainer("user").setVisible(false));
				}
				fragment.add(new Label("activity", lastActivity.getDescription()));
				fragment.add(new Label("date", DateUtils.formatAge(lastActivity.getDate()))
						.add(new AttributeAppender("title", DateUtils.formatDateTime(lastActivity.getDate()))));

				cellItem.add(fragment);
			}

		});  
		
		body.add(commentsTable = new DefaultDataTable<>("comments", columns, dataProvider,
				WebConstants.PAGE_SIZE, getPagingHistorySupport()) {

			@Override
			protected Item<CodeComment> newRowItem(String id, int index, IModel<CodeComment> model) {
				Item<CodeComment> item = super.newRowItem(id, index, model);
				CodeComment comment = model.getObject();
				item.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
					@Override
					protected String load() {
						var comment = item.getModelObject();
						return comment.isVisitedAfter(comment.getLastActivity().getDate()) ? "comment" : "comment new";
					}
				}));

				var commentId = comment.getId();
				item.add(new ChangeObserver() {
					@Override
					public Collection<String> findObservables() {
						return Sets.newHashSet(CodeComment.getChangeObservable(commentId));
					}

				});
				item.setOutputMarkupId(true);
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
