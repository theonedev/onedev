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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DisabledAttributeLinkBehavior;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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

		getPagingNavigation().setViewSize(7);

		var first = (AbstractLink) get("first");
		first.add(new Label("pageNumber", "1"));
		first.add(AttributeModifier.replace("data-page-index", "0"));
		first.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPageable().getCurrentPage() == 0 ? "active" : "";
			}

		}));

		var last = (AbstractLink) get("last");
		last.add(new Label("pageNumber", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return String.valueOf(getPageable().getPageCount());
			}

		}));
		last.add(AttributeModifier.replace("data-page-index", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return String.valueOf(getPageable().getPageCount() - 1);
			}

		}));
		last.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPageable().getCurrentPage() == getPageable().getPageCount() - 1 ? "active" : "";
			}

		}));
		
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
					return newParamPagingLink(id, (int) pageIndex, false, false);
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
				long pageIndex = getStartIndex() + loopItem.getIndex();
				loopItem.get("pageLink").add(AttributeModifier.replace("data-page-index", String.valueOf(pageIndex)));
				loopItem.setVisible(pageIndex != 0 && pageIndex != pageable.getPageCount() - 1);
				long distance = Math.abs(pageIndex - pageable.getCurrentPage());
				if (distance >= 1)
					loopItem.add(AttributeAppender.append("class", "page-distance-1-plus"));
				if (distance >= 2)
					loopItem.add(AttributeAppender.append("class", "page-distance-2-plus"));
				if (distance >= 3)
					loopItem.add(AttributeAppender.append("class", "page-distance-3-plus"));
				if (pageIndex == pageable.getCurrentPage()) {
					loopItem.add(activeAttribute);
				}
			}
		};
	}

	@Override
	protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable, int increment) {
		AbstractLink link;
		if (pagingHistorySupport instanceof ParamPagingHistorySupport) {
			link = newParamPagingLink(id, increment, true, false);
		} else {
			link = new AjaxPagingNavigationIncrementLink(id, pageable, increment) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					super.onClick(target);
					int pageNumber = (int) getPageable().getCurrentPage();
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
		if (pagingHistorySupport instanceof ParamPagingHistorySupport) {
			link = newParamPagingLink(id, pageNumber, false, true);
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

	private BookmarkablePageLink<Void> newParamPagingLink(String id, int pageNumber,
			boolean relativeToCurrent, boolean disableWhenCurrent) {
		var link = new BookmarkablePageLink<Void>(id, getPage().getClass()) {

			private int getAbsolutePageNumber() {
				if (relativeToCurrent)
					return (int) getPageable().getCurrentPage() + pageNumber;
				else if (pageNumber == -1)
					return (int) getPageable().getPageCount() - 1;
				else
					return pageNumber;
			}

			@Override
			public PageParameters getPageParameters() {
				return ((ParamPagingHistorySupport) pagingHistorySupport)
						.newPageParameters(getAbsolutePageNumber());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				int absolutePageNumber = getAbsolutePageNumber();
				if (relativeToCurrent) {
					setEnabled(absolutePageNumber >= 0
							&& absolutePageNumber < getPageable().getPageCount());
				} else if (disableWhenCurrent) {
					setEnabled(absolutePageNumber != getPageable().getCurrentPage());
				}
			}

		};
		link.add(new DisabledAttributeLinkBehavior());
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
