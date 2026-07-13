package io.onedev.server.util;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;

public class ComponentHierarchical implements Hierarchical {

    private static final long serialVersionUID = 1L;

    private Component component;

    public ComponentHierarchical(Component component) {
        this.component = component;
    }

    @Override
    public Hierarchical getParent() {
        MarkupContainer parent = component.getParent();
        if (parent != null) {
            return new ComponentHierarchical(parent);
        }
        return null;
    }

    @Override
    public <T> T getData(Class<T> clazz) {
        if (clazz.isInstance(component)) {
            return clazz.cast(component);
        }
        return null;
    }

}
