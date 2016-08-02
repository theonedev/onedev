package com.pmease.gitplex.web.websocket;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.request.component.IRequestablePage;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.wicket.WicketUtils;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.event.codecomment.CodeCommentEvent;

@Singleton
public class CodeCommentChangeBroadcaster {
	
	private final Dao dao;
	
	@Inject
	public CodeCommentChangeBroadcaster(Dao dao) {
		this.dao = dao;
	}
	
	@Listen
	public void on(CodeCommentEvent event) {
		/*
		 * Make sure that code comment and associated objects are committed before
		 * sending render request; otherwise rendering request may not reflect
		 * expected status as rendering happens in another thread which may get
		 * executed before code comment modification is committed.
		 */
		CodeCommentChangeTrait trait = new CodeCommentChangeTrait();
		trait.commentId = event.getComment().getId();
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				// Send web socket message in a thread in order not to blocking UI operations
				IRequestablePage page = WicketUtils.getPage();
				GitPlex.getInstance(ExecutorService.class).execute(new Runnable() {

					@Override
					public void run() {
						WebSocketRenderBehavior.requestToRender(trait, page);
					}
					
				});
			}
			
		});
	}
		
}