package io.onedev.server.model.support.widget;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;

public interface TabStateSupport<T extends TabState> extends Serializable {

    @Nullable
    T get();

    void set(@Nullable T state);

}
