package com.gitplex.server.web.component.datatable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DisabledAttributeLinkBehavior;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

import com.gitplex.server.web.util.PagingHistorySupport;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class HistoryAwareNavToolbar extends NavigationToolbar {

	private final PagingHistorySupport pagingHistorySupport;
	
	public HistoryAwareNavToolbar(DataTable<?, ?> table, PagingHistorySupport pagingHistorySupport) {
		super(table);
		this.pagingHistorySupport = pagingHistorySupport;
	}
	
	@Override
	protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
		return new BootstrapPagingNavigator(navigatorId, table) {

			@Override
			protected PagingNavigation newNavigation(String id, IPageable pageable, 
					IPagingLabelProvider labelProvider) {

				return new PagingNavigation(id, pageable, labelProvider) {
		            private final AttributeModifier activeAttribute = AttributeModifier.append("class", "active");

		            @Override
					protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
		            	if (pagingHistorySupport != null) {
							return new BookmarkablePageLink<Void>(id, getPage().getClass(), 
									pagingHistorySupport.newPageParameters((int)pageIndex));
		            	} else {
		            		return super.newPagingNavigationLink(id, pageable, pageIndex);
		            	}
					}

					@Override
		            protected void populateItem(final LoopItem loopItem) {
		                super.populateItem(loopItem);
		                if ((getStartIndex() + loopItem.getIndex()) == pageable.getCurrentPage()) {
		                    loopItem.add(activeAttribute);
		                }
		            }
		        };
			}

			@Override
			protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable, int increment) {
				AbstractLink link;
				if (pagingHistorySupport != null) {
					int pageNumber = (int) pageable.getCurrentPage() + increment;
					link = new BookmarkablePageLink<Void>(id, getPage().getClass(), 
							pagingHistorySupport.newPageParameters(pageNumber));
					link.setEnabled(pageNumber>=0 && pageNumber<getPageable().getPageCount());
					link.add(new DisabledAttributeLinkBehavior());
				} else {
					link = new PagingNavigationIncrementLink<Void>(id, pageable, increment);
				}
				return link;
			}

			@Override
			protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int pageNumber) {
				AbstractLink link;
				if (pagingHistorySupport != null) {
					if (pageNumber == -1)
						pageNumber = (int) (getPageable().getPageCount()) - 1;
					link = new BookmarkablePageLink<Void>(id, getPage().getClass(), 
							pagingHistorySupport.newPageParameters(pageNumber));
					link.setEnabled(pageNumber != pageable.getCurrentPage());
					link.add(new DisabledAttributeLinkBehavior());
				} else {
					link = new PagingNavigationLink<Void>(id, pageable, pageNumber);
				}
				return link;
			}
			
		};
	}

}
