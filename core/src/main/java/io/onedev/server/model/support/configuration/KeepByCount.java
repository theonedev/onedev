package io.onedev.server.model.support.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;

import org.hibernate.Session;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.manager.BuildInfoManager;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=200, name="Keep builds by count")
public class KeepByCount implements BuildCleanupRule {

	private static final long serialVersionUID = 1L;

	private int count = 1000;
	
	@Editable(name="Number of Builds to Keep")
	@Min(value=1, message="Should keep at least one build")
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public void cleanup(Configuration configuration, Session session) {
		List<Long> buildIds = new ArrayList<>(OneDev.getInstance(CacheManager.class).getBuildIdsByConfiguration(configuration.getId()));
		Collections.sort(buildIds);
		BuildManager buildManager = OneDev.getInstance(BuildManager.class);
		BuildInfoManager buildInfoManager = OneDev.getInstance(BuildInfoManager.class);
		for (int i=0; i<buildIds.size()-count; i++) {
			Long buildId = buildIds.get(i);
			Build build = buildManager.load(buildId);
			buildManager.delete(build);
			buildInfoManager.delete(configuration.getProject(), build.getId());
		}
	}

}
