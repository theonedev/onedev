package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.model.support.workspace.spec.shell.CustomLinuxShell;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=100, name="nt:Open Code in Container", description="""
        Create a workspace spec running Open Code inside container for isolation and security purpose. 
        You may customize the <a href='https://code.onedev.io/onedev/docker/opencode' target='_blank'>container image</a> 
        later to suit your needs if desired""")
public class OpenCodeInContainer extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();
        
        workspaceSpec.setName(getName());
        workspaceSpec.setRunInContainer(true);
        workspaceSpec.setImage("1dev/opencode");
        workspaceSpec.setRunAs("1001:1001");
        workspaceSpec.setShell(new CustomLinuxShell());

        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("OpenCode");
        shortcutConfig.setCommand("opencode");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        var userData = new UserData();
        userData.setKey("opencode");
        userData.getPaths().add("/home/opencode/.profile");
        userData.getPaths().add("/home/opencode/.bashrc");
        userData.getPaths().add("/home/opencode/.config/opencode");
        userData.getPaths().add("/home/opencode/.local/share");
        userData.getPaths().add("/home/opencode/.local/state");
        workspaceSpec.getUserDatas().add(userData);

        return workspaceSpec;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}