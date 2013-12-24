package com.pmease.commons.wicket.editable.enumeration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.wicket.asset.CommonHeaderItem;

@SuppressWarnings("serial")
public class EnumPropertyEditContext extends PropertyEditContext {

    public EnumPropertyEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

    @Override
    public Object renderForEdit(Object renderParam) {
        List<String> choices = new ArrayList<>();

        @SuppressWarnings({"unchecked", "rawtypes"})
        final EnumSet<?> values = EnumSet.allOf((Class<Enum>) getPropertyGetter().getReturnType());

        for (Iterator<?> it = values.iterator(); it.hasNext();) {
            Enum<?> value = (Enum<?>) it.next();
            choices.add(value.toString());
        }

        DropDownChoice<String> dropDownChoice =
                new DropDownChoice<String>((String) renderParam, new IModel<String>() {

                    public void detach() {
                    }

                    public String getObject() {
                        Enum<?> propertyValue = (Enum<?>) getPropertyValue();
                        if (propertyValue != null) {
                            return propertyValue.toString();
                        } else {
                            return null;
                        }
                    }

                    public void setObject(String object) {
                        if (object != null) {
                            for (Iterator<?> it = values.iterator(); it.hasNext();) {
                                Enum<?> value = (Enum<?>) it.next();
                                if (value.toString().equals(object)) setPropertyValue(value);
                            }
                        } else {
                            setPropertyValue(null);
                        }
                    }

                }, choices) {

                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        tag.setName("select");
                        tag.put("class", "form-control");
                        super.onComponentTag(tag);
                    }

        			@Override
        			public void renderHead(IHeaderResponse response) {
        				super.renderHead(response);
        				response.render(CommonHeaderItem.get());
        			}

        	};

        dropDownChoice.setNullValid(!EditableUtils.isPropertyRequired(getPropertyGetter()));

        return dropDownChoice;
    }

    @Override
    public Object renderForView(Object renderParam) {
        Enum<?> propertyValue = (Enum<?>) getPropertyValue();
        if (propertyValue != null) {
            return new Label((String) renderParam, propertyValue.toString());
        } else {
            return new Label((String) renderParam, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
