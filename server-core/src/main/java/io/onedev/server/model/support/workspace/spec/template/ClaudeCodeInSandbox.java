package io.onedev.server.model.support.workspace.spec.template;

import io.onedev.agent.workspace.ProvisionMode;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;

@Editable(order=600, name="nt:Claude Code in Sandbox", description="""
        Create a workspace spec running Claude Code inside a sandbox for isolation.
        You may customize the <a href='https://code.onedev.io/onedev/docker/claudecode' target='_blank'>sandbox template image</a>
        later to suit your needs if desired""")
public class ClaudeCodeInSandbox extends ClaudeCodeInContainer {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = super.createWorkspaceSpec();
        workspaceSpec.setProvisionMode(ProvisionMode.SANDBOX);
        workspaceSpec.setTemplate(workspaceSpec.getImage());
        return workspaceSpec;
    }

}
