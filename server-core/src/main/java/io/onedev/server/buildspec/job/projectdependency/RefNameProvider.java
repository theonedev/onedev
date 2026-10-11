package io.onedev.server.buildspec.job.projectdependency;

import java.io.Serializable;

import io.onedev.server.annotation.Editable;

@Editable
public interface RefNameProvider extends Serializable {

	String getRefName();

}
