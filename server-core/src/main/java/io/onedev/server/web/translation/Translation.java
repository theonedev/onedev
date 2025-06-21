package io.onedev.server.web.translation;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.wicket.Application;
import org.apache.wicket.Localizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;

public class Translation extends TranslationResourceBundle {

	private static final Logger logger = LoggerFactory.getLogger(Translation.class);

	private static final Map<String, String> m = new HashMap<>();

	public static String _T(String key) {
		if (Application.exists()) {
			return Localizer.get().getString("t: " + key, null);
		} else {
			return key;
		}
	}
	
	public static void watchUpdate(Class<? extends TranslationResourceBundle> translationClass, Runnable onUpdate) {
		if (Bootstrap.sandboxMode && !Bootstrap.prodMode) {
			var classResource = translationClass.getResource(translationClass.getSimpleName() + ".class");
			if (classResource != null) {
				try {
					var classFile = new File(classResource.toURI());
					var lastModifiedRef = new AtomicLong(classFile.lastModified());
					var stop = new AtomicBoolean(false);
					new Thread(() -> {
						while (!stop.get()) {
							try {
								Thread.sleep(100);
								var lastModified = classFile.lastModified();
								if (lastModified != lastModifiedRef.get()) {
									lastModifiedRef.set(lastModified);
									Thread.sleep(5000);
									onUpdate.run();
								}
							} catch (InterruptedException e) {
							}
						}
					}).start();
					
					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						stop.set(true);
					}));
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			} else {
				logger.error("Unable to locate class file for: {}", translationClass.getName());
			}
		}
	}

	static {
		init(m);
		Translation.watchUpdate(Translation.class, () -> {
			init(m);
		});
	}

	public static Set<String> getExtraKeys() {
		var extraKeys = new TreeSet<String>();

		extraKeys.add("Create Administrator Account");
		extraKeys.add("Server Setup");
		extraKeys.add("Specify System Settings");
		extraKeys.add("adding .onedev-buildspec.yml");
		extraKeys.add("WAITING");
		extraKeys.add("PENDING");
		extraKeys.add("FAILED");
		extraKeys.add("Create Administrator Account");
		extraKeys.add("Server Setup");	
		extraKeys.add("Specify System Settings");
		extraKeys.add("adding .onedev-buildspec.yml");
		extraKeys.add("WAITING");
		extraKeys.add("PENDING");
		extraKeys.add("FAILED");
		extraKeys.add("CANCELLED");
		extraKeys.add("TIMED_OUT");
		extraKeys.add("SUCCESSFUL");		
		extraKeys.add("OPEN");
		extraKeys.add("MERGED");
		extraKeys.add("DISCARDED");
		extraKeys.add("Container Image(s)");
		extraKeys.add("Container Image");
		extraKeys.add("RubyGems(s)");
		extraKeys.add("NPM(s)");
		extraKeys.add("Maven(s)");
		extraKeys.add("NuGet(s)");
		extraKeys.add("PyPI(s)");
		extraKeys.add("Helm(s)");
		extraKeys.add("job");
		extraKeys.add("service");
		extraKeys.add("step template");
		extraKeys.add("property");		
		extraKeys.add("Successful");
		extraKeys.add("Always");
		extraKeys.add("Never");
		extraKeys.add("Yes");
		extraKeys.add("Security & Compliance");
		extraKeys.add("Dependency Management");
		extraKeys.add("Publish");
		extraKeys.add("Repository Sync");
		extraKeys.add("Utilities");
		extraKeys.add("Docker Image");
		extraKeys.add("Unified view");
		extraKeys.add("Split view");
		extraKeys.add("Ignore all whitespace");
		extraKeys.add("Ignore change whitespace");
		extraKeys.add("Ignore leading whitespace");
		extraKeys.add("Ignore trailing whitespace");
		extraKeys.add("Do not ignore whitespace");
		extraKeys.add("Internal Database");
		extraKeys.add("External System");
		extraKeys.add("Tell user to reset password");
		extraKeys.add("Commits are taken from default branch of non-forked repositories");
		extraKeys.add("issue");
		extraKeys.add("pull request");
		extraKeys.add("build");
		extraKeys.add("pack");
		extraKeys.add("code commit");
		extraKeys.add("pull request and code review");
		extraKeys.add("Filter pull requests");
		extraKeys.add("Filter issues");
		extraKeys.add("Add all commits from source branch to target branch with a merge commit");
		extraKeys.add("Only create merge commit if target branch can not be fast-forwarded to source branch");
		extraKeys.add("Squash all commits from source branch into a single commit in target branch");
		extraKeys.add("Rebase all commits from source branch onto target branch");
		extraKeys.add("Closed");
		extraKeys.add("Create Merge Commit");
		extraKeys.add("Create Merge Commit If Necessary");
		extraKeys.add("Squash Source Branch Commits");
		extraKeys.add("Rebase Source Branch Commits");
		extraKeys.add("Test");
		extraKeys.add("{0} seconds");
		extraKeys.add("{0} minutes");
		extraKeys.add("{0} hours");
		extraKeys.add("{0} days");
		extraKeys.add("{0} second");
		extraKeys.add("{0} minute");
		extraKeys.add("{0} hour");
		extraKeys.add("{0} day");		
		extraKeys.add("Server is Starting...");
		
		return extraKeys;
	}
	
	public static void init(Map<String, String> m) {
		m.clear();

		m.put("markdown:heading", "Heading");
		m.put("markdown:image", "Image");
		m.put("container:image", "Image");
		m.put("month:Apr", "Apr");
		m.put("month:Aug", "Aug");
		m.put("month:Dec", "Dec");
		m.put("month:Feb", "Feb");
		m.put("month:Jan", "Jan");
		m.put("month:Jul", "Jul");
		m.put("month:Jun", "Jun");
		m.put("month:Mar", "Mar");
		m.put("month:May", "May");
		m.put("month:Nov", "Nov");
		m.put("month:Oct", "Oct");
		m.put("month:Sep", "Sep");
		m.put("week:Fri", "Fri");
		m.put("week:Mon", "Mon");
		m.put("week:Sat", "Sat");
		m.put("week:Sun", "Sun");
		m.put("week:Thu", "Thu");
		m.put("week:Tue", "Tue");
		m.put("week:Wed", "Wed");
		m.put("issue:Number", "Number");
		m.put("link:Multiple", "Multiple");
		m.put("cluster:lead", "lead");
		m.put("widget:Tabs", "Tabs");
		m.put("severity:CRITICAL", "Critical");
		m.put("severity:HIGH", "High");
		m.put("severity:MEDIUM", "Medium");
		m.put("severity:LOW", "Low");
	}

	@Override
	protected Map<String, String> getContents() {
		return m;
	}

}
