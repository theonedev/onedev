package io.onedev.server.web.component.codecomment;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CodeCommentManager;
import io.onedev.server.manager.CodeCommentRelationManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentRelation;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.changes.RequestChangesPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public abstract class CodeCommentListPanel extends Panel {

	private final IModel<CodeCommentFilter> filterOptionModel;
	
	private final PagingHistorySupport pagingHistorySupport;
	
	public CodeCommentListPanel(String id, IModel<CodeCommentFilter> filterOptionModel, 
			PagingHistorySupport pagingHistorySupport) {
		super(id);
		
		this.filterOptionModel = filterOptionModel;
		this.pagingHistorySupport = pagingHistorySupport;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("myComments") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getUser() != null);
			}

			@Override
			public void onClick() {
				CodeCommentFilter filterOption = new CodeCommentFilter();
				filterOption.setUserName(SecurityUtils.getUser().getName());
				filterOptionModel.setObject(filterOption);
			}
			
		});
		add(new Link<Void>("allComments") {

			@Override
			public void onClick() {
				filterOptionModel.setObject(new CodeCommentFilter());
			}
			
		});
		Form<?> filterForm = new Form<Void>("filterForm") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
			}
			
		};
		filterForm.add(BeanContext.editModel("editor", filterOptionModel));
		add(filterForm);
		
		List<IColumn<CodeComment, Void>> columns = new ArrayList<>();
		columns.add(new AbstractColumn<CodeComment, Void>(Model.of("Code Comment")) {

			private void openComment(CodeComment comment) {
				if (!comment.isValid()) {
					CodeCommentListPanel.this.replaceWith(new InvalidCodeCommentPanel(CodeCommentListPanel.this, 
							comment.getId()));
				} else {
					PullRequest request = getPullRequest();
					if (request != null) {
						setResponsePage(RequestChangesPage.class, RequestChangesPage.paramsOf(request, comment));
					} else {
						String compareCommit = comment.getCompareContext().getCompareCommit();
						if (!compareCommit.equals(comment.getMarkPos().getCommit())
								&& getProject().getRepository().hasObject(ObjectId.fromString(compareCommit))) {
							setResponsePage(RevisionComparePage.class, RevisionComparePage.paramsOf(comment));
						} else {
							setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(comment));
						}
					}				
				}
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem,
					String componentId, IModel<CodeComment> rowModel) {
				CodeComment comment = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "commentFrag", CodeCommentListPanel.this);
				User userForDisplay = User.getForDisplay(comment.getUser(), comment.getUserName());
				fragment.add(new AvatarLink("avatar", userForDisplay));
				fragment.add(new UserLink("user", userForDisplay));
				
				Link<Void> fileLink = new Link<Void>("file") {

					@Override
					public void onClick() {
						openComment(rowModel.getObject());
					}
					
				};
				fileLink.add(new Label("label", comment.getMarkPos().getPath()));
				fragment.add(fileLink);
				
				fragment.add(new Label("date", DateUtils.formatAge(comment.getDate())));
				
				fragment.add(new Link<Void>("detail") {

					@Override
					public void onClick() {
						openComment(rowModel.getObject());
					}
					
				});
				
				fragment.add(new MarkdownViewer("content", new IModel<String>() {

					@Override
					public String getObject() {
						return rowModel.getObject().getContent();
					}

					@Override
					public void detach() {
					}

					@Override
					public void setObject(String object) {
						CodeComment comment = rowModel.getObject();
						comment.setContent(object);
						OneDev.getInstance(CodeCommentManager.class).save(comment, getPullRequest());				
					}
					
				}, null));
				
				WebMarkupContainer lastActivityContainer = new WebMarkupContainer("lastActivity");
				if (comment.getLastActivity() != null) {
					String action = comment.getLastActivity().getDescription();
					lastActivityContainer.add(new Label("action", action));
					
					userForDisplay = User.getForDisplay(comment.getLastActivity().getUser(), 
							comment.getLastActivity().getUserName());
					lastActivityContainer.add(new UserLink("user", userForDisplay));
					lastActivityContainer.add(new Label("date", DateUtils.formatAge(comment.getLastActivity().getDate())));
				} else {
					lastActivityContainer.add(new WebMarkupContainer("action"));
					lastActivityContainer.add(new WebMarkupContainer("user"));
					lastActivityContainer.add(new WebMarkupContainer("date"));
					lastActivityContainer.setVisible(false);
				}
				fragment.add(lastActivityContainer);
				
				cellItem.add(fragment);
				
				Date lastUpdateDate;
				if (comment.getLastActivity() != null)
					lastUpdateDate = comment.getLastActivity().getDate();
				else
					lastUpdateDate = comment.getDate();
				cellItem.add(AttributeAppender.append("class", 
						comment.isVisitedAfter(lastUpdateDate)?"comment":"comment new"));
			}
			
		});

		IDataProvider<CodeComment> dataProvider = new IDataProvider<CodeComment>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends CodeComment> iterator(long first, long count) {
				if (getPullRequest() != null) {
					EntityCriteria<CodeCommentRelation> criteria = EntityCriteria.of(CodeCommentRelation.class);
					criteria.setFetchMode("comment", FetchMode.JOIN);
					criteria.add(Restrictions.eq("request", getPullRequest()));
					filterOptionModel.getObject().fillRelationCriteria(criteria);
					criteria.addOrder(Order.desc("id"));
					return OneDev.getInstance(CodeCommentRelationManager.class)
							.findRange(criteria, (int)first, (int)count)
							.stream()
							.map(CodeCommentRelation::getComment)
							.iterator();
				} else {
					EntityCriteria<CodeComment> criteria = EntityCriteria.of(CodeComment.class);
					criteria.add(Restrictions.eq("project", getProject()));
					filterOptionModel.getObject().fillCriteria(criteria);
					criteria.addOrder(Order.desc("id"));
					return OneDev.getInstance(Dao.class).findRange(criteria, (int)first, (int)count).iterator();
				}
			}

			@Override
			public long size() {
				ProjectPage page = (ProjectPage) getPage();

				if (getPullRequest() != null) {
					EntityCriteria<CodeCommentRelation> criteria = EntityCriteria.of(CodeCommentRelation.class);
					criteria.add(Restrictions.eq("request", getPullRequest()));
					filterOptionModel.getObject().fillRelationCriteria(criteria);
					return OneDev.getInstance(CodeCommentRelationManager.class).count(criteria);
				} else {
					EntityCriteria<CodeComment> criteria = EntityCriteria.of(CodeComment.class);
					criteria.add(Restrictions.eq("project", page.getProject()));
					filterOptionModel.getObject().fillCriteria(criteria);
					return OneDev.getInstance(Dao.class).count(criteria);
				}
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
		DataTable<CodeComment, Void> dataTable = new DataTable<>("comments", columns, 
				dataProvider, WebConstants.PAGE_SIZE);
		dataTable.setCurrentPage(pagingHistorySupport.getCurrentPage());
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.addBottomToolbar(new NavigationToolbar(dataTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new HistoryAwarePagingNavigator(navigatorId, table, pagingHistorySupport);
			}
			
		});
		add(dataTable);		
	}

	private Project getProject() {
		return ((ProjectPage) getPage()).getProject();
	}
	
	@Override
	protected void onDetach() {
		filterOptionModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CodeCommentResourceReference()));
	}

	@Nullable
	protected abstract PullRequest getPullRequest();
	
}
