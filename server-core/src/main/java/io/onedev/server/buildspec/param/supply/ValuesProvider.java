package io.onedev.server.buildspec.param.supply;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.annotation.Editable;

@Editable
public interface ValuesProvider extends Serializable {
	
	List<List<String>> getValues(@Nullable Build build, @Nullable ParamCombination paramCombination);
	
}
