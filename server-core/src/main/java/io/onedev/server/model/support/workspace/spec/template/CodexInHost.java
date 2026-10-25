package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.agent.workspace.ProvisionMode;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=1300, name="nt:Codex in Host", description="""
        Create a workspace spec running Codex directly on the workspace host. 
        Workspaces using created spec will share same environments and settings. 
        To use this template, please ensure that: 
        <ul>
            <li><a href='https://openai.com/codex/' target='_blank'>Codex</a> is installed on the workspace host</li>
            <li><a href='https://code.onedev.io/onedev/tod/~files/main/readme.md' target='_blank'>TOD</a> is installed on the workspace host and companion skills are installed for Codex</li>
            <li>A host provisioner is added in 'Administration / Workspace Provisioners' with 'applicable projects' configured properly</li>
        </ul>""")
public class CodexInHost extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();

        workspaceSpec.setName(getName());
        workspaceSpec.setProvisionMode(ProvisionMode.HOST);
        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("Codex");
        shortcutConfig.setCommand("codex");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        configureTaskAutomation(workspaceSpec, "codex exec --dangerously-bypass-approvals-and-sandbox \"$TASK_PROMPT\"");

        return workspaceSpec;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}
