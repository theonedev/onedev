package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=1400, name="nt:Cursor CLI In Shell", description="""
        Create a workspace spec running Cursor CLI directly with server shell environment. 
        Workspaces using created spec will share same environments and settings. 
        To use this template, please ensure that: 
        <ul>
            <li><a href='https://cursor.com/docs/cli/overview' target='_blank'>Cursor CLI</a> is installed on OneDev server</li>
            <li><a href='https://code.onedev.io/onedev/tod/~files/main/readme.md' target='_blank'>TOD</a> is installed on OneDev server and companion skills are installed for Cursor CLI</li>
            <li>A shell provisioner is added in 'Administration / Workspace Provisioners' with 'applicable projects' configured properly</li>
        </ul>""")
public class CursorInShell extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();

        workspaceSpec.setName(getName());
        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("Cursor CLI");
        shortcutConfig.setCommand("agent");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        configureTaskAutomation(workspaceSpec, "agent -p --force --approve-mcps --trust \"$TASK_PROMPT\"");

        return workspaceSpec;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}
