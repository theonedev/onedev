package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=200, name="nt:Claude Code", description="""
        Create a workspace spec running Claude Code directly with server shell environment. 
        To use spec created by this template, please ensure that: 
        <ul>
            <li><a href='https://code.claude.com/docs/en/overview' target='_blank'>Claude Code</a> is installed on OneDev server</li>
            <li>A shell provisioner is added in 'Administration / Workspace Provisioners' with 'applicable projects' configured properly</li>
        </ul>""")
public class ClaudeCode extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();

        workspaceSpec.setName(getName());
        
        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("Claude Code");
        shortcutConfig.setCommand("claude");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        return workspaceSpec;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}