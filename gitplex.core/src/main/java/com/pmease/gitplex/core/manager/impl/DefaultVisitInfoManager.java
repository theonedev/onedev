package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.loader.Listen;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.Verification;
import com.pmease.gitplex.core.event.PullRequestListener;
import com.pmease.gitplex.core.event.codecomment.CodeCommentCreated;
import com.pmease.gitplex.core.event.codecomment.CodeCommentReplied;
import com.pmease.gitplex.core.event.codecomment.CodeCommentResolved;
import com.pmease.gitplex.core.event.codecomment.CodeCommentUnresolved;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.VisitInfoManager;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.EnvironmentConfig;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultVisitInfoManager implements VisitInfoManager, PullRequestListener {

	private static final String INFO_DIR = "visit";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String PULL_REQUEST_STORE = "pullRequest";
	
	private static final String CODE_COMMENT_STORE = "codeComment";

	private final Map<Long, Environment> envs = new HashMap<>();
	
	private final StorageManager storageManager;
	
	@Inject
	public DefaultVisitInfoManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}
	
	private synchronized Environment getEnv(Depot depot) {
		Environment env = envs.get(depot.getId());
		if (env == null) {
			EnvironmentConfig config = new EnvironmentConfig();
			config.setLogCacheShared(false);
			config.setMemoryUsage(1024*1024*16);
			env = Environments.newInstance(getInfoDir(depot), config);
			envs.put(depot.getId(), env);
		}
		return env;
	}
	
	private File getInfoDir(Depot depot) {
		File infoDir = new File(storageManager.getInfoDir(depot), INFO_DIR);
		if (!infoDir.exists()) 
			FileUtils.createDir(infoDir);
		return infoDir;
	}
	
	private Store getStore(Environment env, String storeName) {
		return env.computeInTransaction(new TransactionalComputable<Store>() {
		    @Override
		    public Store compute(Transaction txn) {
		        return env.openStore(storeName, StoreConfig.WITHOUT_DUPLICATES, txn);
		    }
		});		
	}

	private byte[] getBytes(@Nullable ByteIterable byteIterable) {
		if (byteIterable != null)
			return Arrays.copyOf(byteIterable.getBytesUnsafe(), byteIterable.getLength());
		else
			return null;
	}
	
	@Override
	public void visit(Account user, Depot depot) {
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				store.put(txn, new StringByteIterable(user.getUUID()), 
						new ArrayByteIterable(longToBytes(System.currentTimeMillis()+1000L)));
			}
			
		});
	}

	@Override
	public void visit(Account user, PullRequest request) {
		Environment env = getEnv(request.getTargetDepot());
		Store store = getStore(env, PULL_REQUEST_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				store.put(txn, new StringPairByteIterable(user.getUUID(), request.getUUID()), 
						new ArrayByteIterable(longToBytes(System.currentTimeMillis()+1000L)));
			}
			
		});
	}

	@Override
	public void visit(Account user, CodeComment comment) {
		Environment env = getEnv(comment.getDepot());
		Store store = getStore(env, CODE_COMMENT_STORE);
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				store.put(txn, new StringPairByteIterable(user.getUUID(), comment.getUUID()), 
						new ArrayByteIterable(longToBytes(System.currentTimeMillis()+1000L)));
			}
			
		});
	}

	@Override
	public Date getVisitDate(Account user, Depot depot) {
		Environment env = getEnv(depot);
		Store store = getStore(env, DEFAULT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				byte[] bytes = getBytes(store.get(txn, new StringByteIterable(user.getUUID())));
				if (bytes != null)
					return new Date(bytesToLong(bytes));
				else
					return null;
			}
			
		});
	}

	@Override
	public Date getVisitDate(Account user, PullRequest request) {
		Environment env = getEnv(request.getTargetDepot());
		Store store = getStore(env, PULL_REQUEST_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				byte[] bytes = getBytes(store.get(txn, new StringPairByteIterable(user.getUUID(), request.getUUID())));
				if (bytes != null)
					return new Date(bytesToLong(bytes));
				else
					return null;
			}
			
		});
	}

	@Override
	public Date getVisitDate(Account user, CodeComment comment) {
		Environment env = getEnv(comment.getDepot());
		Store store = getStore(env, CODE_COMMENT_STORE);
		return env.computeInTransaction(new TransactionalComputable<Date>() {
			
			@Override
			public Date compute(Transaction txn) {
				byte[] bytes = getBytes(store.get(txn, new StringPairByteIterable(user.getUUID(), comment.getUUID())));
				if (bytes != null)
					return new Date(bytesToLong(bytes));
				else
					return null;
			}
			
		});
	}

	@Listen
	public void on(CodeCommentReplied event) {
		CodeCommentReply reply = event.getReply();
		visit(reply.getUser(), reply.getComment());
		for (CodeCommentRelation relation: reply.getComment().getRelations()) {
			visit(reply.getUser(), relation.getRequest());
		}
	}

	@Listen
	public void on(CodeCommentCreated event) {
		visit(event.getComment().getUser(), event.getComment());
	}

	@Listen
	public void on(CodeCommentResolved event) {
		visit(event.getUser(), event.getComment());
		for (CodeCommentRelation relation: event.getComment().getRelations()) {
			visit(event.getUser(), relation.getRequest());
		}
	}
	
	@Listen
	public void on(CodeCommentUnresolved event) {
		visit(event.getUser(), event.getComment());
		for (CodeCommentRelation relation: event.getComment().getRelations()) {
			visit(event.getUser(), relation.getRequest());
		}
	}
	
	private byte[] longToBytes(long value) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(value);
	    return buffer.array();
	}

	private long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip(); 
	    return buffer.getLong();
	}
	
	@Override
	public void onOpenRequest(PullRequest request) {
	}

	@Override
	public void onReopenRequest(PullRequest request, Account user) {
		visit(user, request);
	}

	@Override
	public void onUpdateRequest(PullRequestUpdate update) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReviewRequest(Review review) {
		visit(review.getUser(), review.getUpdate().getRequest());
	}

	@Override
	public void onVerifyRequest(Verification verification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteVerification(Verification verification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteReview(Review review) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMentionAccount(PullRequest request, Account account) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMentionAccount(PullRequestComment comment, Account account) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCommentRequest(PullRequestComment comment) {
		visit(comment.getUser(), comment.getRequest());
	}

	@Override
	public void onAssignRequest(PullRequest request, Account user) {
		visit(user, request);
	}

	@Override
	public void onRestoreSourceBranch(PullRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteSourceBranch(PullRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onIntegrateRequest(PullRequest request, Account user) {
		visit(user, request);
	}

	@Override
	public void onDiscardRequest(PullRequest request, Account user) {
		visit(user, request);
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInvitingReview(ReviewInvitation invitation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pendingIntegration(PullRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pendingUpdate(PullRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pendingApproval(PullRequest request) {
		// TODO Auto-generated method stub
		
	}

	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes());
		}
	}

	static class StringPairByteIterable extends ArrayByteIterable {
		StringPairByteIterable(String uuid1, String uuid2) {
			super(getBytes(uuid1, uuid2));
		}
		
		private static byte[] getBytes(String uuid1, String uuid2) {
			byte[] bytes1 = uuid1.getBytes();
			byte[] bytes2 = uuid2.getBytes();
			byte[] bytes = new byte[bytes1.length+bytes2.length];
			System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
			System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
			return bytes;
		}
	}

}
