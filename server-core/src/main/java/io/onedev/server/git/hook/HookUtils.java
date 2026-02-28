package io.onedev.server.git.hook;

import com.google.common.base.Preconditions;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ServerConfig;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.CryptoUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HookUtils {

	public static final String RECEIVE_HOOK_TOKEN = CryptoUtils.generateSecret(); 
	
	private static final String gitReceiveHook;

	private static final String gitWorkspacePostCommitHook;
	
	static {
        try (InputStream is = HookUtils.class.getClassLoader().getResourceAsStream("git-receive-hook")) {
        	Preconditions.checkNotNull(is);
            gitReceiveHook = StringUtils.join(IOUtils.readLines(is, Charset.defaultCharset()), "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		try (InputStream is = HookUtils.class.getClassLoader().getResourceAsStream("git-workspace-postcommit-hook")) {
			Preconditions.checkNotNull(is);
			gitWorkspacePostCommitHook = StringUtils.join(IOUtils.readLines(is, Charset.defaultCharset()), "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Map<String, String> getCommonHookEnvs() {
		ServerConfig serverConfig = OneDev.getInstance(ServerConfig.class);
		SettingService settingService = OneDev.getInstance(SettingService.class);
		String hookUrl = "http://localhost:" + serverConfig.getHttpPort();
		String curl = settingService.getSystemSetting().getCurlLocation().getExecutable();
		
		Map<String, String> envs = new HashMap<>();
		
        envs.put("ONEDEV_CURL", curl);
		envs.put("ONEDEV_URL", hookUrl);
				
		return envs;
	}

	public static Map<String, String> getReceiveHookEnvs(Long projectId, String principal) {		
		var envs = getCommonHookEnvs();
		envs.put("ONEDEV_HOOK_TOKEN", RECEIVE_HOOK_TOKEN);
		envs.put("ONEDEV_USER_ID", principal);
		envs.put("ONEDEV_REPOSITORY_ID", projectId.toString());				
		return envs;
	}
	
	public static boolean isReceiveHookValid(File gitDir, String hookName) {
        File hookFile = new File(gitDir, "hooks/" + hookName);
        if (!hookFile.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(hookFile, Charset.defaultCharset());
			if (!content.contains("ONEDEV_HOOK_TOKEN"))
				return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        if (!hookFile.canExecute())
        	return false;
        
        return true;
	}
	
	public static Map<String, String> getWorkspacePostCommitHookEnvs(String workspaceToken) {
		var envs = getCommonHookEnvs();
		envs.put("ONEDEV_HOOK_TOKEN", workspaceToken);
		return envs;
	}

	public static void setupWorkspacePostCommitHook(File gitDir, boolean lfsEnabled) {
		File hooksDir = new File(gitDir, "hooks");
		FileUtils.createDir(hooksDir);
		File postCommitHookFile = new File(hooksDir, "post-commit");

		String lfsAwareHook;
		if (lfsEnabled) {
			lfsAwareHook = String.format(gitWorkspacePostCommitHook, "git lfs post-commit \"$@\"");
		} else {
			lfsAwareHook = String.format(gitWorkspacePostCommitHook, "");
		}

		FileUtils.writeFile(postCommitHookFile, lfsAwareHook);
		postCommitHookFile.setExecutable(true);
	}

	public static void checkReceiveHooks(File gitDir) {
		if (!isReceiveHookValid(gitDir, "pre-receive") || !isReceiveHookValid(gitDir, "post-receive")) {
            File hooksDir = new File(gitDir, "hooks");

            File gitPreReceiveHookFile = new File(hooksDir, "pre-receive");
            FileUtils.writeFile(gitPreReceiveHookFile, String.format(gitReceiveHook, "git-prereceive-callback"));
            gitPreReceiveHookFile.setExecutable(true);
            
            File gitPostReceiveHookFile = new File(hooksDir, "post-receive");
            FileUtils.writeFile(gitPostReceiveHookFile, String.format(gitReceiveHook, "git-postreceive-callback"));
            gitPostReceiveHookFile.setExecutable(true);
        }
	}
	
}
