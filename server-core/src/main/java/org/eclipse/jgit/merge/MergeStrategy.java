/*
 * Copyright (C) 2008-2009, Google Inc.
 * Copyright (C) 2009, Matthias Sohn <matthias.sohn@sap.com>
 * Copyright (C) 2012, Research In Motion Limited and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.merge;

import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;

/**
 * A method of combining two or more trees together to form an output tree.
 * <p>
 * Different strategies may employ different techniques for deciding which paths
 * (and ObjectIds) to carry from the input trees into the final output tree.
 */
public abstract class MergeStrategy {
	/** Simple strategy that sets the output tree to the first input tree. */
	public static final MergeStrategy OURS = new StrategyOneSided("ours", 0); //$NON-NLS-1$

	/** Simple strategy that sets the output tree to the second input tree. */
	public static final MergeStrategy THEIRS = new StrategyOneSided("theirs", 1); //$NON-NLS-1$

	/** Simple strategy to merge paths, without simultaneous edits. */
	public static final ThreeWayMergeStrategy SIMPLE_TWO_WAY_IN_CORE = new StrategySimpleTwoWayInCore();

	/**
	 * Simple strategy to merge paths. It tries to merge also contents. Multiple
	 * merge bases are not supported
	 */
	public static final ThreeWayMergeStrategy RESOLVE = new StrategyResolve();

	/**
	 * Recursive strategy to merge paths. It tries to merge also contents.
	 * Multiple merge bases are supported
	 * @since 3.0
	 */
	public static final ThreeWayMergeStrategy RECURSIVE = new StrategyRecursive();

	private static final HashMap<String, MergeStrategy> STRATEGIES = new HashMap<>();

	static {
		register(OURS);
		register(THEIRS);
		register(SIMPLE_TWO_WAY_IN_CORE);
		register(RESOLVE);
		register(RECURSIVE);
	}

	/**
	 * Register a merge strategy so it can later be obtained by name.
	 *
	 * @param imp
	 *            the strategy to register.
	 * @throws java.lang.IllegalArgumentException
	 *             a strategy by the same name has already been registered.
	 */
	public static void register(MergeStrategy imp) {
		register(imp.getName(), imp);
	}

	/**
	 * Register a merge strategy so it can later be obtained by name.
	 *
	 * @param name
	 *            name the strategy can be looked up under.
	 * @param imp
	 *            the strategy to register.
	 * @throws java.lang.IllegalArgumentException
	 *             a strategy by the same name has already been registered.
	 */
	public static synchronized void register(final String name,
			final MergeStrategy imp) {
		if (STRATEGIES.containsKey(name))
			throw new IllegalArgumentException(MessageFormat.format(
					JGitText.get().mergeStrategyAlreadyExistsAsDefault, name));
		STRATEGIES.put(name, imp);
	}

	/**
	 * Locate a strategy by name.
	 *
	 * @param name
	 *            name of the strategy to locate.
	 * @return the strategy instance; null if no strategy matches the name.
	 */
	public static synchronized MergeStrategy get(String name) {
		return STRATEGIES.get(name);
	}

	/**
	 * Get all registered strategies.
	 *
	 * @return the registered strategy instances. No inherit order is returned;
	 *         the caller may modify (and/or sort) the returned array if
	 *         necessary to obtain a reasonable ordering.
	 */
	public static synchronized MergeStrategy[] get() {
		final MergeStrategy[] r = new MergeStrategy[STRATEGIES.size()];
		STRATEGIES.values().toArray(r);
		return r;
	}

	/**
	 * Get default name of this strategy implementation.
	 *
	 * @return default name of this strategy implementation.
	 */
	public abstract String getName();

	/**
	 * Create a new merge instance.
	 *
	 * @param db
	 *            repository database the merger will read from, and eventually
	 *            write results back to.
	 * @return the new merge instance which implements this strategy.
	 */
	public abstract Merger newMerger(Repository db);

	/**
	 * Create a new merge instance.
	 *
	 * @param db
	 *            repository database the merger will read from, and eventually
	 *            write results back to.
	 * @param inCore
	 *            the merge will happen in memory, working folder will not be
	 *            modified, in case of a non-trivial merge that requires manual
	 *            resolution, the merger will fail.
	 * @return the new merge instance which implements this strategy.
	 */
	public abstract Merger newMerger(Repository db, boolean inCore);

	/**
	 * Create a new merge instance.
	 * <p>
	 * The merge will happen in memory, working folder will not be modified, in
	 * case of a non-trivial merge that requires manual resolution, the merger
	 * will fail.
	 *
	 * @param inserter
	 *            inserter to write results back to.
	 * @param config
	 *            repo config for reading diff algorithm settings.
	 * @return the new merge instance which implements this strategy.
	 * @since 4.8
	 */
	public abstract Merger newMerger(ObjectInserter inserter, Config config);
}
