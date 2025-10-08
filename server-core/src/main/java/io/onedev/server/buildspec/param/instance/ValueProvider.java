package io.onedev.server.buildspec.param.instance;

import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.List;

@Editable
public interface ValueProvider extends Serializable {
	
	List<String> getValue(@Nullable Build build, @Nullable ParamCombination paramCombination);
	
}
