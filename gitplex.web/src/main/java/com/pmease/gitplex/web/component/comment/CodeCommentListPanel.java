package com.pmease.gitplex.web.component.comment;

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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.support.CompareContext;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.pmease.gitplex.web.util.DateUtils;

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
				setVisible(SecurityUtils.getAccount() != null);
			}

			@Override
			public void onClick() {
				CodeCommentFilter filterOption = new CodeCommentFilter();
				filterOption.setUserName(SecurityUtils.getAccount().getName());
				filterOptionModel.setObject(filterOption);
			}
			
		});
		add(new Link<Void>("myUnresolvedComments") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getAccount() != null);
			}

			@Override
			public void onClick() {
				CodeCommentFilter filterOption = new CodeCommentFilter();
				filterOption.setUserName(SecurityUtils.getAccount().getName());
				filterOption.setUnresolved(true);
				filterOptionModel.setObject(filterOption);
			}
			
		});
		add(new Link<Void>("unresolvedComments") {

			@Override
			public void onClick() {
				CodeCommentFilter filterOption = new CodeCommentFilter();
				filterOption.setUnresolved(true);
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
				Depot depot = ((DepotPage) getPage()).getDepot();
				
				PullRequest request = getPullRequest();
				if (request != null) {
					setResponsePage(RequestChangesPage.class, RequestChangesPage.paramsOf(request, comment, null));
				} else {
					CompareContext compareContext = comment.getLastCompareContext();
					if (!compareContext.getCompareCommit().equals(comment.getCommentPos().getCommit())) {
						setResponsePage(RevisionComparePage.class, RevisionComparePage.paramsOf(depot, comment, null));
					} else {
						setResponsePage(DepotFilePage.class, DepotFilePage.paramsOf(depot, comment, null));
					}
				}				
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem,
					String componentId, IModel<CodeComment> rowModel) {
				CodeComment comment = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "commentFrag", CodeCommentListPanel.this);
				Link<Void> titleLink = new Link<Void>("title") {

					@Override
					public void onClick() {
						openComment(rowModel.getObject());
					}
					
				};
				titleLink.add(new Label("label", comment.getTitle()));
				fragment.add(titleLink);
				fragment.add(new WebMarkupContainer("resolved").setVisible(comment.isResolved()));
				fragment.add(new AccountLink("user", comment.getUser()));
				
				Link<Void> fileLink = new Link<Void>("file") {

					@Override
					public void onClick() {
						openComment(rowModel.getObject());
					}
					
				};
				fileLink.add(new Label("label", comment.getCommentPos().getPath()));
				fragment.add(fileLink);
				
				fragment.add(new Label("date", DateUtils.formatAge(comment.getDate())));
				
				WebMarkupContainer lastEventContainer = new WebMarkupContainer("lastEvent");
				if (comment.getLastEvent() != null) {
					String description = comment.getLastEvent().getType();
					lastEventContainer.add(new Label("description", description));
					if (comment.getLastEvent().getUser() != null)
						lastEventContainer.add(new AccountLink("user", comment.getLastEvent().getUser()));
					else
						lastEventContainer.add(new WebMarkupContainer("user").setVisible(false));
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
				DepotPage page = (DepotPage) getPage();

				if (getPullRequest() != null) {
					List<CodeComment> comments = new ArrayList<>(getPullRequest().getCodeComments());
					filterOptionModel.getObject().filter(comments);
					return comments.iterator();
				} else {
					EntityCriteria<CodeComment> criteria = EntityCriteria.of(CodeComment.class);
					criteria.add(Restrictions.eq("depot", page.getDepot()));
					filterOptionModel.getObject().fill(criteria);
					criteria.addOrder(Order.desc("id"));
					return GitPlex.getInstance(Dao.class).findRange(criteria, (int)first, (int)count).iterator();
				}
			}

			@Override
			public long size() {
				DepotPage page = (DepotPage) getPage();

				if (getPullRequest() != null) {
					return getPullRequest().getCodeComments().size();
				} else {
					EntityCriteria<CodeComment> criteria = EntityCriteria.of(CodeComment.class);
					criteria.add(Restrictions.eq("depot", page.getDepot()));
					filterOptionModel.getObject().fill(criteria);
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
				dataProvider, getPullRequest()!=null?Integer.MAX_VALUE:Constants.DEFAULT_PAGE_SIZE);
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.addBottomToolbar(new NavigationToolbar(dataTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new BootstrapPagingNavigator(navigatorId, dataTable);
			}
			
		});
		add(dataTable);		
		
		if (getPullRequest() != null && dataTable.getItemCount() > PullRequest.MAX_CODE_COMMENTS) {
			add(new Label("alert", "Too many code comments, only displaying " + PullRequest.MAX_CODE_COMMENTS));
		} else {
			add(new WebMarkupContainer("alert").setVisible(false));
		}
	}

	@Override
	protected void onDetach() {
		filterOptionModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CodeCommentListResourceReference()));
	}

	@Nullable
	protected abstract PullRequest getPullRequest();
	
}
