package com.pmease.gitplex.web.websocket;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.request.component.IRequestablePage;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.wicket.WicketUtils;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.event.pullrequest.PullRequestChangeEvent;

@Singleton
public class PullRequestChangeBroadcaster {
	
	private final Dao dao;
	
	private final ExecutorService executorService;
	
	@Inject
	public PullRequestChangeBroadcaster(Dao dao, ExecutorService executorService) {
		this.dao = dao;
		this.executorService = executorService;
	}

	private void requestToRender(PullRequestChangeTrait trait) {
		// Send web socket message in a thread in order not to blocking UI operations
		IRequestablePage page = WicketUtils.getPage();
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				WebSocketRenderBehavior.requestToRender(trait, page);
			}
			
		});
	}
	
	@Listen
	public void on(PullRequestChangeEvent event) {
		PullRequestChangeTrait trait = new PullRequestChangeTrait();
		trait.requestId = event.getRequest().getId();
		
		if (dao.getSession().getTransaction().getStatus() == TransactionStatus.ACTIVE) {
			/*
			 * Make sure that pull request and associated objects are committed before
			 * sending render request; otherwise rendering request may not reflect
			 * expected status as rendering happens in another thread which may get
			 * executed before pull request modification is committed.
			 */
			dao.doAfterCommit(new Runnable() {
	
				@Override
				public void run() {
					requestToRender(trait);
				}
				
			});
		} else {
			requestToRender(trait);
		}
	}

}