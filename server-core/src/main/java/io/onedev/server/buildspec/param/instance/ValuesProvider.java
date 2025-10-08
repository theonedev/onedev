package io.onedev.server.buildspec.param.instance;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.annotation.Editable;

@Editable
public interface ValuesProvider extends Serializable {
	
	List<List<String>> getValues(@Nullable Build build, @Nullable ParamCombination paramCombination);
	
}
