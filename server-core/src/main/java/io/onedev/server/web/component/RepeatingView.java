package io.onedev.server.web.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;

/** A repeating view whose children can be reordered without detaching their feedback panels. */
public class RepeatingView extends org.apache.wicket.markup.repeater.RepeatingView {

    private static final long serialVersionUID = 1L;

    private List<String> childOrder = List.of();

    public RepeatingView(String id) {
        super(id);
    }

    private List<Component> orderedChildren() {
        List<Component> children = new ArrayList<>();
        super.iterator().forEachRemaining(children::add);
        if (!childOrder.isEmpty()) {
            children.sort(Comparator.comparingInt(component -> {
                int index = childOrder.indexOf(component.getId());
                return index >= 0 ? index : Integer.MAX_VALUE;
            }));
        }
        return children;
    }

    public Component get(int index) {
        return orderedChildren().get(index);
    }

    public void swap(int first, int second) {
        List<Component> children = orderedChildren();
        Collections.swap(children, first, second);
        childOrder = children.stream().map(Component::getId).toList();
    }

    @Override
    public Iterator<Component> iterator() {
        Iterator<Component> iterator = orderedChildren().iterator();
        return new Iterator<>() {
            private Component current;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Component next() {
                current = iterator.next();
                return current;
            }

            @Override
            public void remove() {
                iterator.remove();
                RepeatingView.this.remove(current);
            }
        };
    }
}
