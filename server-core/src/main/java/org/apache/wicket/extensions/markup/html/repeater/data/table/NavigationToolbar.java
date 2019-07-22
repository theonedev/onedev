/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.markup.html.repeater.data.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * Toolbar that displays links used to navigate the pages of the datatable as well as a message
 * about which rows are being displayed and their total number in the data table.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class NavigationToolbar extends AbstractToolbar
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public NavigationToolbar(final DataTable<?, ?> table)
	{
		super(table);

		WebMarkupContainer span = new WebMarkupContainer("span") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeRender() {
				addOrReplace(newPagingNavigator("navigator", table));
				addOrReplace(newNavigatorLabel("navigatorLabel", table));
				super.onBeforeRender();
			}
			
		};
		add(span);
		span.add(AttributeModifier.replace("colspan", new AbstractReadOnlyModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				return String.valueOf(table.getColumns().size()).intern();
			}
		}));

	}

	/**
	 * Factory method used to create the paging navigator that will be used by the datatable
	 * 
	 * @param navigatorId
	 *            component id the navigator should be created with
	 * @param table
	 *            dataview used by datatable
	 * @return paging navigator that will be used to navigate the data table
	 */
	protected PagingNavigator newPagingNavigator(final String navigatorId,
		final DataTable<?, ?> table)
	{
		return new PagingNavigator(navigatorId, table);
	}

	/**
	 * Factory method used to create the navigator label that will be used by the datatable
	 * 
	 * @param navigatorId
	 *            component id navigator label should be created with
	 * @param table
	 *            dataview used by datatable
	 * @return navigator label that will be used to navigate the data table
	 * 
	 */
	protected WebComponent newNavigatorLabel(final String navigatorId, final DataTable<?, ?> table)
	{
		return new NavigatorLabel(navigatorId, table);
	}

	/** {@inheritDoc} */
	@Override
	protected void onConfigure()
	{
		super.onConfigure();
		setVisible(getTable().getPageCount() > 1);
	}
}
