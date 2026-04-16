package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=100, name="nt:Open Code", description="""
        Create a workspace spec running Open Code directly with server shell environment. 
        To use spec created by this template, please ensure that: 
        <ul>
            <li><a href='https://github.com/tmux/tmux' target='_blank'>tmux</a> is installed on OneDev server</li>
            <li><a href='https://opencode.ai/' target='_blank'>Open Code</a> is installed on OneDev server</li>
            <li>A shell provisioner is added in 'Administration / Workspace Provisioners' with 'applicable projects' configured properly</li>
        </ul>""")
public class OpenCode extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();

        workspaceSpec.setName(getName());
        
        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("Open Code");
        shortcutConfig.setCommand("opencode");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        return workspaceSpec;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}