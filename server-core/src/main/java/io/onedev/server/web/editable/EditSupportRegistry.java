package io.onedev.server.web.editable;

import java.io.Serializable;

public interface EditSupportRegistry {

	PropertyContext<Serializable> getPropertyEditContext(PropertyDescriptor descriptor);

}
