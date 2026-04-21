package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=1300, name="nt:Codex In Shell", description="""
        Create a workspace spec running Codex directly with server shell environment. 
        Workspaces using created spec will share same environments and settings. 
        To use this template, please ensure that: 
        <ul>
            <li><a href='https://openai.com/codex/' target='_blank'>Codex</a> is installed on OneDev server</li>
            <li>A shell provisioner is added in 'Administration / Workspace Provisioners' with 'applicable projects' configured properly</li>
        </ul>""")
public class CodexInShell extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();

        workspaceSpec.setName(getName());
        
        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("Codex");
        shortcutConfig.setCommand("codex");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        return workspaceSpec;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}