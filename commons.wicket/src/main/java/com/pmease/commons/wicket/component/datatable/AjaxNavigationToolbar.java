package com.pmease.commons.wicket.component.datatable;

import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationIncrementLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

import com.pmease.commons.wicket.ajaxlistener.AjaxLoadingOverlay;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class AjaxNavigationToolbar extends org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar {

	public AjaxNavigationToolbar(DataTable<?, ?> dataTable) {
		super(dataTable);
	}

	@Override
	protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
		
		return new BootstrapAjaxPagingNavigator(navigatorId, table) {

			@Override
			protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int increment) {
				return new AjaxPagingNavigationLink(id, pageable, increment) {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);

						attributes.getAjaxCallListeners().add(new AjaxLoadingOverlay());
					}
					
				};
			}

			@Override
			protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable, int increment) {
				return new AjaxPagingNavigationIncrementLink(id, pageable, increment) {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);

						attributes.getAjaxCallListeners().add(new AjaxLoadingOverlay());
					}
					
				};
			}

			@Override
			protected PagingNavigation newNavigation(String id, 
					IPageable pageable, IPagingLabelProvider labelProvider) {
				PagingNavigation navigation = new AjaxPagingNavigation(id, pageable, labelProvider) {

					@Override
					protected Link<?> newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
						return new AjaxPagingNavigationLink(id, pageable, pageIndex) {

							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								attributes.getAjaxCallListeners().add(new AjaxLoadingOverlay());
							}
							
						};
					}
					
				};
				navigation.setViewSize(5);
				return navigation;
			}
			
		};
	}

}
