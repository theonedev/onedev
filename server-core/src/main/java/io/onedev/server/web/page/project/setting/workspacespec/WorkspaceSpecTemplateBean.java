package io.onedev.server.web.page.project.setting.workspacespec;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.model.support.workspace.spec.template.WorkspaceSpecTemplate;

@Editable(name="Create from Template")
public class WorkspaceSpecTemplateBean implements Serializable {

    private WorkspaceSpecTemplate template;

    @Editable(order=100)
    @OmitName
    @NotNull
    public WorkspaceSpecTemplate getTemplate() {
        return template;
    }

    public void setTemplate(WorkspaceSpecTemplate template) {
        this.template = template;
    }
    
}
