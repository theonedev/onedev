package com.pmease.commons.wicket.editable.list;

import java.io.Serializable;

import com.pmease.commons.editable.AbstractListEditSupport;
import com.pmease.commons.editable.AbstractPolymorphicListPropertyEditContext;
import com.pmease.commons.editable.AbstractTableListPropertyEditContext;
import com.pmease.commons.wicket.editable.list.polymorphic.PolymorphicListPropertyEditConext;
import com.pmease.commons.wicket.editable.list.table.TableListPropertyEditContext;

public class ListEditSupport extends AbstractListEditSupport {

	@Override
	protected AbstractPolymorphicListPropertyEditContext newPolymorphicListEditContext(
			Serializable bean, String propertyName) {
		return new PolymorphicListPropertyEditConext(bean, propertyName);
	}

	@Override
	protected AbstractTableListPropertyEditContext newTableListEditContext(Serializable bean, String propertyName) {
		return new TableListPropertyEditContext(bean, propertyName);
	}

}
