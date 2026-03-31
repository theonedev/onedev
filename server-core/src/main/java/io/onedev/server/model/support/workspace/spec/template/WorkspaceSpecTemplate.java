package io.onedev.server.model.support.workspace.spec.template;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;

@Editable
public abstract class WorkspaceSpecTemplate implements Serializable {

    private String name;

	@Editable(order=10, description="Specify name of the workspace spec")
    @NotEmpty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public abstract WorkspaceSpec createWorkspaceSpec();

}
