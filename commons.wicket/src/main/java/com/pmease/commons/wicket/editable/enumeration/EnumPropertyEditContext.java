package com.pmease.commons.wicket.editable.enumeration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class EnumPropertyEditContext extends PropertyEditContext {

    public EnumPropertyEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

    @Override
    public Component renderForEdit(String componentId) {
        List<String> choices = new ArrayList<>();

        @SuppressWarnings({"unchecked", "rawtypes"})
        final EnumSet<?> values = EnumSet.allOf((Class<Enum>) getPropertyGetter().getReturnType());

        for (Iterator<?> it = values.iterator(); it.hasNext();) {
            Enum<?> value = (Enum<?>) it.next();
            choices.add(value.toString());
        }

        DropDownChoice<String> dropDownChoice =
                new DropDownChoice<String>(componentId, new IModel<String>() {

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

        	};

        dropDownChoice.setNullValid(!EditableUtils.isPropertyRequired(getPropertyGetter()));

        return dropDownChoice;
    }

    @Override
    public Component renderForView(String componentId) {
        Enum<?> propertyValue = (Enum<?>) getPropertyValue();
        if (propertyValue != null) {
            return new Label(componentId, propertyValue.toString());
        } else {
            return new Label(componentId, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
