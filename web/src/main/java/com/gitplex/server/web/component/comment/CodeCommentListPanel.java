package com.gitplex.server.web.component.comment;

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

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.CodeCommentRelationManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentRelation;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.User;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.link.UserLink;
import com.gitplex.server.web.component.markdown.ContentVersionSupport;
import com.gitplex.server.web.component.markdown.MarkdownViewer;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.page.project.ProjectPage;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.compare.RevisionComparePage;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.changes.RequestChangesPage;
import com.gitplex.server.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public abstract class CodeCommentListPanel extends Panel {

	private final IModel<CodeCommentFilter> filterOptionModel;
	
	public CodeCommentListPanel(String id, IModel<CodeCommentFilter> filterOptionModel) {
		super(id);
		
		this.filterOptionModel = filterOptionModel;
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
				
				ContentVersionSupport contentVersionSupport;
				if (SecurityUtils.canModify(comment)) {
					contentVersionSupport = new ContentVersionSupport() {

						@Override
						public long getVersion() {
							return rowModel.getObject().getVersion();
						}
						
					};
				} else {
					contentVersionSupport = null;
				}
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
						GitPlex.getInstance(CodeCommentManager.class).save(comment, getPullRequest());				
					}
					
				}, contentVersionSupport));
				
				WebMarkupContainer lastEventContainer = new WebMarkupContainer("lastEvent");
				if (comment.getLastEvent() != null) {
					String description = comment.getLastEvent().getType();
					lastEventContainer.add(new Label("description", description));
					
					userForDisplay = User.getForDisplay(comment.getLastEvent().getUser(), 
							comment.getLastEvent().getUserName());
					lastEventContainer.add(new UserLink("user", userForDisplay));
					lastEventContainer.add(new Label("date", DateUtils.formatAge(comment.getLastEvent().getDate())));
				} else {
					lastEventContainer.add(new WebMarkupContainer("description"));
					lastEventContainer.add(new WebMarkupContainer("user"));
					lastEventContainer.add(new WebMarkupContainer("date"));
					lastEventContainer.setVisible(false);
				}
				fragment.add(lastEventContainer);
				
				cellItem.add(fragment);
				
				Date lastUpdateDate;
				if (comment.getLastEvent() != null)
					lastUpdateDate = comment.getLastEvent().getDate();
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
					return GitPlex.getInstance(CodeCommentRelationManager.class)
							.findRange(criteria, (int)first, (int)count)
							.stream()
							.map(CodeCommentRelation::getComment)
							.iterator();
				} else {
					EntityCriteria<CodeComment> criteria = EntityCriteria.of(CodeComment.class);
					criteria.add(Restrictions.eq("project", getProject()));
					filterOptionModel.getObject().fillCriteria(criteria);
					criteria.addOrder(Order.desc("id"));
					return GitPlex.getInstance(Dao.class).findRange(criteria, (int)first, (int)count).iterator();
				}
			}

			@Override
			public long size() {
				ProjectPage page = (ProjectPage) getPage();

				if (getPullRequest() != null) {
					EntityCriteria<CodeCommentRelation> criteria = EntityCriteria.of(CodeCommentRelation.class);
					criteria.add(Restrictions.eq("request", getPullRequest()));
					filterOptionModel.getObject().fillRelationCriteria(criteria);
					return GitPlex.getInstance(CodeCommentRelationManager.class).count(criteria);
				} else {
					EntityCriteria<CodeComment> criteria = EntityCriteria.of(CodeComment.class);
					criteria.add(Restrictions.eq("project", page.getProject()));
					filterOptionModel.getObject().fillCriteria(criteria);
					return GitPlex.getInstance(Dao.class).count(criteria);
				}
			}

			@Override
			public IModel<CodeComment> model(CodeComment object) {
				Long commentId = object.getId();
				return new LoadableDetachableModel<CodeComment>() {

					@Override
					protected CodeComment load() {
						return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
					}
					
				};
			}
			
		};
		DataTable<CodeComment, Void> dataTable = new DataTable<>("comments", columns, 
				dataProvider, WebConstants.PAGE_SIZE);
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.addBottomToolbar(new NavigationToolbar(dataTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new BootstrapPagingNavigator(navigatorId, dataTable);
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
