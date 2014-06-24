package com.pmease.commons.wicket.jqueryui;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.jqueryui.JQueryUIMouseJavaScriptReference;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.jqueryui.JQueryUIWidgetJavaScriptReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

@SuppressWarnings("serial")
public class JQueryUISortableResourceReference extends WebjarsJavaScriptResourceReference {
    /**
     * Singleton instance of this reference
     */
    private static final JQueryUISortableResourceReference INSTANCE = new JQueryUISortableResourceReference();

    /**
     * @return the single instance of the resource reference
     */
    public static JQueryUISortableResourceReference get() {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private JQueryUISortableResourceReference() {
        super("jquery-ui/current/ui/minified/jquery.ui.sortable.min.js");
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        final List<HeaderItem> dependencies = Lists.newArrayList(super.getDependencies());
        dependencies.add(JQueryUIWidgetJavaScriptReference.asHeaderItem());
        dependencies.add(JQueryUIMouseJavaScriptReference.asHeaderItem());

        return dependencies;
    }

    /**
     * @return this resource reference singleton instance as header item
     */
    public static HeaderItem asHeaderItem() {
        return JavaScriptHeaderItem.forReference(get());
    }
}
