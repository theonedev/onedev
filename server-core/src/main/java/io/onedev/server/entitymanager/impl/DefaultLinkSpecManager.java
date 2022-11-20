package io.onedev.server.entitymanager.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.util.facade.LinkSpecFacade;
import io.onedev.server.util.usage.Usage;

@Singleton
public class DefaultLinkSpecManager extends BaseEntityManager<LinkSpec> implements LinkSpecManager {

	private final SettingManager settingManager;
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private volatile Map<String, Long> ids;
	
	private volatile Map<Long, LinkSpecFacade> cache;
	
	@Inject
	public DefaultLinkSpecManager(Dao dao, SettingManager settingManager, TransactionManager transactionManager, 
			ClusterManager clusterManager) {
		super(dao);
		this.settingManager = settingManager;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		ids = clusterManager.getHazelcastInstance().getReplicatedMap("linkSpecIds");
		cache = clusterManager.getHazelcastInstance().getReplicatedMap("linkSpecCache");
		
		for (LinkSpec link: query(true)) {
			ids.put(link.getName(), link.getId());
			
			String oppositeName = link.getOpposite()!=null?link.getOpposite().getName():null;
			if (oppositeName != null)
				ids.put(oppositeName, link.getId());
			cache.put(link.getId(), link.getFacade());
		}
	}
	
	@Sessional
	@Override
	public LinkSpec find(String name) {
		Long linkId = ids.get(name);
		if (linkId != null) {
			LinkSpecFacade facade = cache.get(linkId);
			if (facade != null && (name.equals(facade.getName()) || name.equals(facade.getOppositeName())))
				return load(linkId);
		}
		return null;
	}

	@Override
	public List<LinkSpec> queryAndSort() {
		List<LinkSpec> links = query(true);
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
			    	for (LinkSpec link: query(true)) {
			    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
			    			usage.add(updater.onDeleteLink(oldOppositeName));
			    	}
			    	usage.checkInUse("Opposite side of issue link '" + oldName + "'");
				} else if (!oldOppositeName.equals(spec.getOpposite().getName())){
					settingManager.onRenameLink(oldOppositeName, spec.getOpposite().getName());
			    	for (LinkSpec link: query(true)) {
			    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
			    			updater.onRenameLink(oldOppositeName, spec.getOpposite().getName());
			    	}
				}
			}
			if (!oldName.equals(spec.getName())) {
				settingManager.onRenameLink(oldName, spec.getName());
		    	for (LinkSpec link: query(true)) {
		    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
		    			updater.onRenameLink(oldName, spec.getName());
		    	}
			}
		}
		super.save(spec);
		
		LinkSpecFacade facade = new LinkSpecFacade(spec);
		
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				cache.put(facade.getId(), facade);
				ids.put(facade.getName(), facade.getId());
				if (facade.getOppositeName() != null)
					ids.put(facade.getOppositeName(), facade.getId());
			}
			
		});
	}

	@Transactional
	public void delete(LinkSpec spec) {
		if (spec.getOpposite() != null) {
	    	Usage usage = new Usage();
	    	usage.add(settingManager.onDeleteLink(spec.getOpposite().getName()));
	    	for (LinkSpec link: query(true)) {
	    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
	    			usage.add(updater.onDeleteLink(spec.getOpposite().getName()));
	    	}
	    	usage.checkInUse("Opposite side of issue link '" + spec.getName() + "'");
		}
		
    	Usage usage = new Usage();
    	usage.add(settingManager.onDeleteLink(spec.getName()));
    	for (LinkSpec link: query(true)) {
    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
    			usage.add(updater.onDeleteLink(spec.getName()));
    	}
    	usage.checkInUse("Issue link '" + spec.getName() + "'");
    	
    	super.delete(spec);
    	
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				cache.remove(spec.getId());
				ids.remove(spec.getName());
				if (spec.getOpposite() != null)
					ids.remove(spec.getOpposite().getName());
			}
			
		});
	}
	
}
