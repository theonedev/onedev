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

	public static final String PARAM_REF_UPDATES = "REF_UPDATES"; 
	
	private static final String gitReceiveHook;

	static {
        try (InputStream is = HookUtils.class.getClassLoader().getResourceAsStream("git-receive-hook")) {
        	Preconditions.checkNotNull(is);
            gitReceiveHook = StringUtils.join(IOUtils.readLines(is, Charset.defaultCharset()), "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	public static Map<String, String> getCommonHookEnvs(String host) {
		ServerConfig serverConfig = OneDev.getInstance(ServerConfig.class);
		SettingService settingService = OneDev.getInstance(SettingService.class);
		String hookUrl = "http://" + host + "/" + serverConfig.getHttpPort();
		String curl = settingService.getSystemSetting().getCurlLocation().getExecutable();
		
		Map<String, String> envs = new HashMap<>();
		
        envs.put("ONEDEV_CURL", curl);
		envs.put("ONEDEV_URL", hookUrl);
				
		return envs;
	}

	public static Map<String, String> getReceiveHookEnvs(Long projectId, String principal) {		
		var envs = new HashMap<String, String>();
		ServerConfig serverConfig = OneDev.getInstance(ServerConfig.class);
		SettingService settingService = OneDev.getInstance(SettingService.class);
		String hookUrl = "http://localhost:" + serverConfig.getHttpPort();
		String curl = settingService.getSystemSetting().getCurlLocation().getExecutable();

		envs.put("ONEDEV_CURL", curl);
		envs.put("ONEDEV_URL", hookUrl);

		envs.put("ONEDEV_HOOK_TOKEN", RECEIVE_HOOK_TOKEN);
		envs.put("ONEDEV_USER_ID", principal);
		envs.put("ONEDEV_REPOSITORY_ID", projectId.toString());				
		return envs;
	}
	
	public static void checkReceiveHooks(File gitDir) {
		File hooksDir = new File(gitDir, "hooks");
		installReceiveHook(new File(hooksDir, "pre-receive"), "git-prereceive-callback");
		installReceiveHook(new File(hooksDir, "post-receive"), "git-postreceive-callback");
	}

	private static void installReceiveHook(File hookFile, String callback) {
		String content = String.format(gitReceiveHook, callback);
		if (hookFile.exists() && hookFile.canExecute()) {
			try {
				if (content.equals(FileUtils.readFileToString(hookFile, Charset.defaultCharset())))
					return;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		FileUtils.writeFile(hookFile, content);
		hookFile.setExecutable(true);
	}
	
}
