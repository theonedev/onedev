package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import com.hazelcast.cp.IAtomicLong;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.util.facade.LinkSpecFacade;
import io.onedev.server.util.usage.Usage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Singleton
public class DefaultLinkSpecManager extends BaseEntityManager<LinkSpec> implements LinkSpecManager {

	private final SettingManager settingManager;
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private volatile Map<String, Long> idCache;
	
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
	public void on(SystemStarting event) {
		var hazelcastInstance = clusterManager.getHazelcastInstance();
		idCache = hazelcastInstance.getMap("linkSpecIds");
		cache = hazelcastInstance.getMap("linkSpecCache");
		
		IAtomicLong cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("linkSpecCacheInited");
		clusterManager.init(cacheInited, () -> {
			for (LinkSpec spec : query(true))
				updateCache(spec.getFacade());
			return 1L;
		});
	}
	
	private void updateCache(LinkSpecFacade facade) {
		cache.put(facade.getId(), facade);
		idCache.put(facade.getName(), facade.getId());
		if (facade.getOppositeName() != null)
			idCache.put(facade.getOppositeName(), facade.getId());
	}
	
	@Sessional
	@Override
	public LinkSpec find(String name) {
		Long linkId = idCache.get(name);
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
			dao.persist(link);
		}
	}

	@Transactional
	@Override
	public void update(LinkSpec spec, String oldName, String oldOppositeName) {
		Preconditions.checkState(!spec.isNew());
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
		dao.persist(spec);
	}

	@Transactional
	@Override
	public void create(LinkSpec spec) {
		Preconditions.checkState(spec.isNew());
		dao.persist(spec);
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
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof LinkSpec) {
			var facade = (LinkSpecFacade) event.getEntity().getFacade();
			transactionManager.runAfterCommit(() -> updateCache(facade));
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof LinkSpec) {
			var facade = (LinkSpecFacade) event.getEntity().getFacade();
			transactionManager.runAfterCommit(() -> {
				cache.remove(facade.getId());
				idCache.remove(facade.getName());
				if (facade.getOppositeName() != null)
					idCache.remove(facade.getOppositeName());
			});
		}
	}
	
}
