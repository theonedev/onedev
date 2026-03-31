package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.UploadStrategy;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.CacheConfig;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.model.support.workspace.spec.shell.CustomLinuxShell;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=1000, name="nt:Open Code in Container", description="""
        Create a workspace spec running Open Code inside container for isolation and security purpose. 
        <b class='text-info'>NOTE: </b> This requires an enterprise subscription. 
        <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days""")
public class OpenCodeInContainer extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();
        
        workspaceSpec.setName(getName());
        workspaceSpec.setRunInContainer(true);
        workspaceSpec.setImage("1dev/opencode:1.0.0");
        workspaceSpec.setRunAs("1001:1001");
        workspaceSpec.setShell(new CustomLinuxShell());

        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("OpenCode");
        shortcutConfig.setCommand("opencode");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        var cacheConfig = new CacheConfig();
        cacheConfig.setKey("opencode");
        cacheConfig.getPaths().add("/home/opencode/.cache");
        cacheConfig.getPaths().add("/home/opencode/.npm");
        cacheConfig.setUploadStrategy(UploadStrategy.UPLOAD_IF_CHANGED);        
        workspaceSpec.getCacheConfigs().add(cacheConfig);

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