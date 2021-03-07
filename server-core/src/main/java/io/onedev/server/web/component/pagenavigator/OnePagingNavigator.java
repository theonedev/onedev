package io.onedev.server.web.component.pagenavigator;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
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

import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class OnePagingNavigator extends PagingNavigator {

	private final PagingHistorySupport pagingHistorySupport;

	public OnePagingNavigator(String markupId, IPageable pageable, 
			@Nullable PagingHistorySupport pagingHistorySupport) {
		super(markupId, pageable);
		this.pagingHistorySupport = pagingHistorySupport;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		get("first").remove();
		get("last").remove();
		
		add(AttributeAppender.append("class", "pagination justify-content-center align-items-center"));
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getPageable().getPageCount() > 1);
	}

	@Override
	protected PagingNavigation newNavigation(String id, IPageable pageable, IPagingLabelProvider labelProvider) {

		return new PagingNavigation(id, pageable, labelProvider) {
			private final AttributeModifier activeAttribute = AttributeModifier.append("class", "active");

			@Override
			protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
				if (pagingHistorySupport != null) {
					return new BookmarkablePageLink<Void>(id, getPage().getClass(),
							pagingHistorySupport.newPageParameters((int) pageIndex));
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
					pagingHistorySupport.newPageParameters(pageNumber)) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setEnabled(pageNumber >= 0 && pageNumber < getPageable().getPageCount());
				}

			};
			link.add(new DisabledAttributeLinkBehavior());
		} else {
			link = new PagingNavigationIncrementLink<Void>(id, pageable, increment);
		}
		return link;
	}

	@Override
	protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int pageNumber) {
		AbstractLink link;
		int absolutePageNumber;
		if (pageNumber == -1)
			absolutePageNumber = (int) (getPageable().getPageCount()-1);
		else
			absolutePageNumber = pageNumber;
		if (pagingHistorySupport != null) {
			link = new BookmarkablePageLink<Void>(id, getPage().getClass(),
					pagingHistorySupport.newPageParameters(absolutePageNumber)) {
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setEnabled(absolutePageNumber != pageable.getCurrentPage());
				}
				
			};
			link.add(new DisabledAttributeLinkBehavior());
		} else {
			link = new PagingNavigationLink<Void>(id, pageable, pageNumber);
		}
		return link;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new PagingNavigatorResourceReference()));
		
		String script = String.format("onedev.server.pagingNavigator.onDomReady('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
