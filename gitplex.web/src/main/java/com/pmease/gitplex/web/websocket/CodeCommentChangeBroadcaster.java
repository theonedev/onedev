package com.pmease.gitplex.web.websocket;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.listener.CodeCommentListener;
import com.pmease.gitplex.core.listener.CodeCommentReplyListener;

@Singleton
public class CodeCommentChangeBroadcaster implements CodeCommentListener, CodeCommentReplyListener {
	
	private final Dao dao;
	
	@Inject
	public CodeCommentChangeBroadcaster(Dao dao) {
		this.dao = dao;
	}
	
	private void onChange(CodeComment comment) {
		/*
		 * Make sure that code comment and associated objects are committed before
		 * sending render request; otherwise rendering request may not reflect
		 * expected status as rendering happens in another thread which may get
		 * executed before code comment modification is committed.
		 */
		CodeCommentChangeTrait trait = new CodeCommentChangeTrait();
		trait.commentId = comment.getId();
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				// Send web socket message in a thread in order not to blocking UI operations
				IRequestablePage page;
				if (RequestCycle.get() != null && RequestCycle.get().getActiveRequestHandler() instanceof IPageRequestHandler) {
					IPageRequestHandler handler = (IPageRequestHandler) RequestCycle.get().getActiveRequestHandler();					
					page = handler.getPage();
				} else {
					page = null;
				}
				
				GitPlex.getInstance(ExecutorService.class).execute(new Runnable() {

					@Override
					public void run() {
						WebSocketRenderBehavior.requestToRender(trait, page);
					}
					
				});
			}
			
		});
	}

	@Override
	public void onSaveComment(CodeComment comment) {
		onChange(comment);
	}

	@Override
	public void onDeleteComment(CodeComment comment) {
	}

	@Override
	public void onSaveReply(CodeCommentReply reply) {
		onChange(reply.getComment());
	}

	@Override
	public void onDeleteReply(CodeCommentReply reply) {
	}
		
}