package io.onedev.server.git.hook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ServerConfig;
import io.onedev.server.entitymanager.SettingManager;

public class HookUtils {

	public static final String HOOK_TOKEN = RandomStringUtils.randomAlphanumeric(20); 
	
	private static final String gitReceiveHook;
	
	static {
        try (InputStream is = HookUtils.class.getClassLoader().getResourceAsStream("git-receive-hook")) {
        	Preconditions.checkNotNull(is);
            gitReceiveHook = StringUtils.join(IOUtils.readLines(is, Charset.defaultCharset()), "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	public static Map<String, String> getHookEnvs(Long projectId, Long userId) {
		ServerConfig serverConfig = OneDev.getInstance(ServerConfig.class);
		SettingManager settingManager = OneDev.getInstance(SettingManager.class);
		String hookUrl = "http://localhost:" + serverConfig.getHttpPort();
		String curl = settingManager.getSystemSetting().getCurlLocation().getExecutable();
		
		Map<String, String> envs = new HashMap<>();
		
        envs.put("ONEDEV_CURL", curl);
		envs.put("ONEDEV_URL", hookUrl);
		envs.put("ONEDEV_HOOK_TOKEN", HOOK_TOKEN);
		envs.put("ONEDEV_USER_ID", userId.toString());
		envs.put("ONEDEV_REPOSITORY_ID", projectId.toString());
		
        envs.put("GITPLEX_CURL", curl);
		envs.put("GITPLEX_URL", hookUrl);
		envs.put("GITPLEX_USER_ID", userId.toString());
		envs.put("GITPLEX_REPOSITORY_ID", projectId.toString());
		
		return envs;
	}
	
	public static boolean isHookValid(File gitDir, String hookName) {
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
	
	public static void checkHooks(File gitDir) {
		if (!isHookValid(gitDir, "pre-receive") 
				|| !isHookValid(gitDir, "post-receive")) {
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
