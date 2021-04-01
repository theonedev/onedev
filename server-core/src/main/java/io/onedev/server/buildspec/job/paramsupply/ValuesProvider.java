package io.onedev.server.buildspec.job.paramsupply;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ValuesProvider extends Serializable {
	
	List<List<String>> getValues(@Nullable Build build, String paramName);
	
}
