package io.onedev.server.model.support.workspace.spec.template;

import io.onedev.agent.workspace.ProvisionMode;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;

@Editable(order=700, name="nt:Codex in Sandbox", description="""
        Create a workspace spec running Codex inside a sandbox for isolation.
        You may customize the <a href='https://code.onedev.io/onedev/docker/codex' target='_blank'>sandbox template image</a>
        later to suit your needs if desired""")
public class CodexInSandbox extends CodexInContainer {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = super.createWorkspaceSpec();
        workspaceSpec.setProvisionMode(ProvisionMode.SANDBOX);
        workspaceSpec.setTemplate(workspaceSpec.getImage());
        return workspaceSpec;
    }

}
