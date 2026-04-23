package io.onedev.server.buildspec.step.branchnameprovider;

import java.io.Serializable;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.Build;

@Editable
public interface BranchNameProvider extends Serializable {

	String getBranchName(Build build);

}
