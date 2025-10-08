package io.onedev.server.web.component.pagenavigator;

import io.onedev.server.web.util.paginghistory.AjaxPagingHistorySupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationIncrementLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DisabledAttributeLinkBehavior;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;

import org.jspecify.annotations.Nullable;

public class OnePagingNavigator extends AjaxPagingNavigator {

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

		return new AjaxPagingNavigation(id, pageable, labelProvider) {
			
			private final AttributeModifier activeAttribute = AttributeModifier.append("class", "active");

			@Override
			protected Link<?> newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
				if (pagingHistorySupport instanceof ParamPagingHistorySupport) {
					return new BookmarkablePageLink<Void>(id, getPage().getClass(),
							((ParamPagingHistorySupport)pagingHistorySupport).newPageParameters((int) pageIndex));
				} else {
					return new AjaxPagingNavigationLink(id, pageable, pageIndex) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							super.onClick(target);
							if (pagingHistorySupport instanceof AjaxPagingHistorySupport)
								((AjaxPagingHistorySupport)pagingHistorySupport).onPageNavigated(target, (int) pageIndex);
						}

					};
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
		int pageNumber = (int) pageable.getCurrentPage() + increment;
		if (pagingHistorySupport instanceof ParamPagingHistorySupport) {
			link = new BookmarkablePageLink<Void>(id, getPage().getClass(),
					((ParamPagingHistorySupport)pagingHistorySupport).newPageParameters(pageNumber)) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setEnabled(pageNumber >= 0 && pageNumber < getPageable().getPageCount());
				}

			};
			link.add(new DisabledAttributeLinkBehavior());
		} else {
			link = new AjaxPagingNavigationIncrementLink(id, pageable, increment) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					super.onClick(target);
					if (pagingHistorySupport instanceof AjaxPagingHistorySupport)
						((AjaxPagingHistorySupport)pagingHistorySupport).onPageNavigated(target, pageNumber);
				}
			};
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
		if (pagingHistorySupport instanceof ParamPagingHistorySupport) {
			link = new BookmarkablePageLink<Void>(id, getPage().getClass(),
					((ParamPagingHistorySupport)pagingHistorySupport).newPageParameters(absolutePageNumber)) {
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setEnabled(absolutePageNumber != pageable.getCurrentPage());
				}
				
			};
			link.add(new DisabledAttributeLinkBehavior());
		} else {
			link = new AjaxPagingNavigationLink(id, pageable, pageNumber) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					super.onClick(target);
					if (pagingHistorySupport instanceof AjaxPagingHistorySupport)
						((AjaxPagingHistorySupport)pagingHistorySupport).onPageNavigated(target, pageNumber);
				}
			};
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
