package io.onedev.server.model.support.configuration;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.joda.time.DateTime;

import io.onedev.server.OneDev;
import io.onedev.server.cache.BuildInfoManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=100, name="Keep builds by days")
public class KeepByDays implements BuildCleanupRule {

	private static final long serialVersionUID = 1L;

	private int count = 30;
	
	@Editable(name="Number of Days to Keep")
	@Min(value=1, message="Should keep at least one day")
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void cleanup(Configuration configuration, Session session) {
		Date keepDate = new DateTime().minusDays(count).toDate();
		Query<?> query = session.createQuery("from Build where configuration=:configuration and date<:date");
		query.setParameter("configuration", configuration);
		query.setParameter("date", keepDate);
		BuildManager buildManager = OneDev.getInstance(BuildManager.class);
		BuildInfoManager buildInfoManager = OneDev.getInstance(BuildInfoManager.class);
		for (Build build: (List<Build>)query.list()) {
			buildManager.delete(build);
			buildInfoManager.delete(configuration.getProject(), build.getId());
		}
	}

}
