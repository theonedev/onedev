package io.onedev.server.entitymanager.impl;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.util.usage.Usage;

@Singleton
public class DefaultLinkSpecManager extends BaseEntityManager<LinkSpec> implements LinkSpecManager {

	private final SettingManager settingManager;
	
	@Inject
	public DefaultLinkSpecManager(Dao dao, SettingManager settingManager) {
		super(dao);
		this.settingManager = settingManager;
	}

	@Sessional
	@Override
	public LinkSpec find(String name) {
		for (LinkSpec spec: query()) {
			if (spec.getName().equals(name) 
					|| spec.getOpposite() != null && spec.getOpposite().getName().equalsIgnoreCase(name)) {
				return spec;
			}
		}
		return null;
	}

	@Override
	public List<LinkSpec> queryAndSort() {
		List<LinkSpec> links = query();
		links.sort(Comparator.comparing(LinkSpec::getOrder));
		return links;
	}

	@Transactional
	@Override
	public void updateOrders(List<LinkSpec> links) {
		for (int i=0; i<links.size(); i++) {
			LinkSpec link = links.get(i);
			link.setOrder(i+1);
			save(link);
		}
	}

	@Transactional
	@Override
	public void save(LinkSpec spec, String oldName, String oldOppositeName) {
		if (oldName != null) {
			if (oldOppositeName != null) {
				if (spec.getOpposite() == null) {
			    	Usage usage = new Usage();
			    	usage.add(settingManager.onDeleteLink(oldOppositeName));
			    	for (LinkSpec link: query()) {
			    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
			    			usage.add(updater.onDeleteLink(oldOppositeName));
			    	}
			    	usage.checkInUse("Opposite side of issue link '" + oldName + "'");
				} else if (!oldOppositeName.equals(spec.getOpposite().getName())){
					settingManager.onRenameLink(oldOppositeName, spec.getOpposite().getName());
			    	for (LinkSpec link: query()) {
			    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
			    			updater.onRenameLink(oldOppositeName, spec.getOpposite().getName());
			    	}
				}
			}
			if (!oldName.equals(spec.getName())) {
				settingManager.onRenameLink(oldName, spec.getName());
		    	for (LinkSpec link: query()) {
		    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
		    			updater.onRenameLink(oldName, spec.getName());
		    	}
			}
		}
		super.save(spec);
	}

	@Transactional
	public void delete(LinkSpec spec) {
		if (spec.getOpposite() != null) {
	    	Usage usage = new Usage();
	    	usage.add(settingManager.onDeleteLink(spec.getOpposite().getName()));
	    	for (LinkSpec link: query()) {
	    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
	    			usage.add(updater.onDeleteLink(spec.getOpposite().getName()));
	    	}
	    	usage.checkInUse("Opposite side of issue link '" + spec.getName() + "'");
		}
		
    	Usage usage = new Usage();
    	usage.add(settingManager.onDeleteLink(spec.getName()));
    	for (LinkSpec link: query()) {
    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
    			usage.add(updater.onDeleteLink(spec.getName()));
    	}
    	usage.checkInUse("Issue link '" + spec.getName() + "'");
    	
    	super.delete(spec);
	}
	
}
