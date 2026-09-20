package io.onedev.server.persistence;

import org.hibernate.SharedSessionContract;
import org.hibernate.Transaction;

public interface IdService {

	void init();

	/** Allocate an ID and persist its high-water mark with the session's transaction. */
	long nextId(SharedSessionContract session, Class<?> entityClass);

	/** Account for an assigned ID in the same transaction as its entity. */
	void useId(SharedSessionContract session, Class<?> entityClass, long id);

	/** Discard pending counter updates after a transaction ends, even if completion callbacks failed. */
	void clearTransaction(Transaction transaction);

}
