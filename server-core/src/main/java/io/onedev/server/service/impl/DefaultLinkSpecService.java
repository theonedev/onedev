package io.onedev.server.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.hazelcast.cp.IAtomicLong;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.facade.LinkSpecFacade;
import io.onedev.server.util.usage.Usage;

@Singleton
public class DefaultLinkSpecService extends BaseEntityService<LinkSpec> implements LinkSpecService {

	@Inject
	private SettingService settingService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private ClusterService clusterService;
	
	private volatile Map<String, Long> idCache;
	
	private volatile Map<Long, LinkSpecFacade> cache;

	@Sessional
	@Listen
	public void on(SystemStarting event) {
		var hazelcastInstance = clusterService.getHazelcastInstance();
		idCache = hazelcastInstance.getMap("linkSpecIds");
		cache = hazelcastInstance.getMap("linkSpecCache");
		
		IAtomicLong cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("linkSpecCacheInited");
		clusterService.initWithLead(cacheInited, () -> {
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
			    	usage.add(settingService.onDeleteLink(oldOppositeName));
			    	for (LinkSpec link: query(true)) {
			    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
			    			usage.add(updater.onDeleteLink(oldOppositeName));
			    	}
			    	usage.checkInUse("Opposite side of issue link '" + oldName + "'");
				} else if (!oldOppositeName.equals(spec.getOpposite().getName())){
					settingService.onRenameLink(oldOppositeName, spec.getOpposite().getName());
			    	for (LinkSpec link: query(true)) {
			    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
			    			updater.onRenameLink(oldOppositeName, spec.getOpposite().getName());
			    	}
				}
			}
			if (!oldName.equals(spec.getName())) {
				settingService.onRenameLink(oldName, spec.getName());
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
	    	usage.add(settingService.onDeleteLink(spec.getOpposite().getName()));
	    	for (LinkSpec link: query(true)) {
	    		for (IssueQueryUpdater updater: link.getQueryUpdaters())
	    			usage.add(updater.onDeleteLink(spec.getOpposite().getName()));
	    	}
	    	usage.checkInUse("Opposite side of issue link '" + spec.getName() + "'");
		}
		
    	Usage usage = new Usage();
    	usage.add(settingService.onDeleteLink(spec.getName()));
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
			var facade = ((LinkSpec) event.getEntity()).getFacade();
			transactionService.runAfterCommit(() -> updateCache(facade));
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof LinkSpec) {
			var facade = ((LinkSpec) event.getEntity()).getFacade();
			transactionService.runAfterCommit(() -> {
				cache.remove(facade.getId());
				idCache.remove(facade.getName());
				if (facade.getOppositeName() != null)
					idCache.remove(facade.getOppositeName());
			});
		}
	}
	
}
