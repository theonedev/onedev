package io.onedev.server.web.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jspecify.annotations.Nullable;

import com.google.common.io.Resources;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.web.img.ImageScope;

public class BrandingUtils {

	private static final String DEFAULT_LOGO_VERSION;

	private static Logo lightLogo;

	private static Logo darkLogo;

	static {
		try {
			DEFAULT_LOGO_VERSION = "default-" + Resources.getResource(ImageScope.class, "logo.png")
					.openConnection().getLastModified();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public record Logo(String version, byte[] bytes) {
	}

	public static File getCustomLogoFile(boolean darkMode) {
		return new File(OneDev.getAssetsDir(), darkMode ? "logo-dark.png" : "logo.png");
	}

	private static File getOemLogoFile(boolean darkMode) {
		return new File(Bootstrap.installDir, darkMode ? "oem/logo-dark.png" : "oem/logo-light.png");
	}

	private static File getLogoFile(boolean darkMode) {
		var file = getCustomLogoFile(darkMode);
		return file.exists() ? file : getOemLogoFile(darkMode);
	}

	public static URL getLogoResource(boolean darkMode) {
		return getLogoResource(getLogoFile(darkMode));
	}

	public static URL getDefaultLogoResource(boolean darkMode) {
		return getLogoResource(getOemLogoFile(darkMode));
	}

	private static URL getLogoResource(File file) {
		if (file.exists()) {
			try {
				return file.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		} else {
			return Resources.getResource(ImageScope.class, "logo.png");
		}
	}

	private static String getLogoVersion(File file) {
		return file.exists() ? file.getParentFile().getName() + "-" + file.lastModified() : DEFAULT_LOGO_VERSION;
	}

	public static synchronized String getLogoVersion(boolean darkMode) {
		return getLogoVersion(getLogoFile(darkMode));
	}

	public static synchronized Logo getLogo(boolean darkMode) throws IOException {
		var file = getLogoFile(darkMode);
		var version = getLogoVersion(file);
		var cached = darkMode ? darkLogo : lightLogo;
		if (cached == null || !cached.version().equals(version)) {
			cached = new Logo(version, Resources.toByteArray(getLogoResource(file)));
			if (darkMode)
				darkLogo = cached;
			else
				lightLogo = cached;
		}
		return cached;
	}

	public static synchronized void updateLogo(@Nullable byte[] bytes, boolean darkMode) throws IOException {
		// Keep each response's version and bytes consistent with branding updates.
		if (bytes != null)
			FileUtils.writeByteArrayToFile(getCustomLogoFile(darkMode), bytes);
		else
			FileUtils.deletePath(getCustomLogoFile(darkMode));
		if (darkMode)
			darkLogo = null;
		else
			lightLogo = null;
	}

	public static String getLogoUrl(boolean darkMode) {
		return (darkMode ? "/logo-dark.png" : "/logo.png") + "?v=" + getLogoVersion(darkMode);
	}

}
