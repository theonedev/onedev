package io.onedev.server.web.component.savedquery;

import java.util.ArrayList;

import javax.annotation.Nullable;

import io.onedev.server.model.support.NamedQuery;

public interface ProjectQuerySupport<T extends NamedQuery> {

	@Nullable
	ArrayList<T> getQueries();

	void onSaveQueries(ArrayList<T> queries);

}
