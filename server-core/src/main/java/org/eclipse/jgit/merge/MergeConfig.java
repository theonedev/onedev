/*******************************************************************************
 * Copyright (c) 2014 Konrad Kügler and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *******************************************************************************/
package org.eclipse.jgit.merge;

import java.io.IOException;

import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Config.SectionParser;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;

/**
 * Holds configuration for merging into a given branch
 *
 * @since 3.3
 */
public class MergeConfig {

	/**
	 * Get merge configuration for the current branch of the repository
	 *
	 * @param repo
	 *            a {@link org.eclipse.jgit.lib.Repository} object.
	 * @return merge configuration for the current branch of the repository
	 */
	public static MergeConfig getConfigForCurrentBranch(Repository repo) {
		try {
			String branch = repo.getBranch();
			if (branch != null)
				return repo.getConfig().get(getParser(branch));
		} catch (IOException e) {
			// ignore
		}
		// use defaults if branch can't be determined
		return new MergeConfig();
	}

	/**
	 * Get a parser for use with
	 * {@link org.eclipse.jgit.lib.Config#get(SectionParser)}
	 *
	 * @param branch
	 *            short branch name to get the configuration for, as returned
	 *            e.g. by {@link org.eclipse.jgit.lib.Repository#getBranch()}
	 * @return a parser for use with
	 *         {@link org.eclipse.jgit.lib.Config#get(SectionParser)}
	 */
	public static final SectionParser<MergeConfig> getParser(
			final String branch) {
		return new MergeConfigSectionParser(branch);
	}

	private final FastForwardMode fastForwardMode;

	private final boolean squash;

	private final boolean commit;

	private MergeConfig(String branch, Config config) {
		String[] mergeOptions = getMergeOptions(branch, config);
		fastForwardMode = getFastForwardMode(config, mergeOptions);
		squash = isMergeConfigOptionSet("--squash", mergeOptions); //$NON-NLS-1$
		commit = !isMergeConfigOptionSet("--no-commit", mergeOptions); //$NON-NLS-1$
	}

	private MergeConfig() {
		fastForwardMode = FastForwardMode.FF;
		squash = false;
		commit = true;
	}

	/**
	 * Get the fast forward mode configured for this branch
	 *
	 * @return the fast forward mode configured for this branch
	 */
	public FastForwardMode getFastForwardMode() {
		return fastForwardMode;
	}

	/**
	 * Whether merges into this branch are configured to be squash merges, false
	 * otherwise
	 *
	 * @return true if merges into this branch are configured to be squash
	 *         merges, false otherwise
	 */
	public boolean isSquash() {
		return squash;
	}

	/**
	 * Whether {@code --no-commit} option is not set.
	 *
	 * @return {@code false} if --no-commit is configured for this branch,
	 *         {@code true} otherwise (even if --squash is configured)
	 */
	public boolean isCommit() {
		return commit;
	}

	private static FastForwardMode getFastForwardMode(Config config,
			String[] mergeOptions) {
		for (String option : mergeOptions) {
			for (FastForwardMode mode : FastForwardMode.values())
				if (mode.matchConfigValue(option))
					return mode;
		}
		FastForwardMode ffmode = FastForwardMode.valueOf(config.getEnum(
				ConfigConstants.CONFIG_KEY_MERGE, null,
				ConfigConstants.CONFIG_KEY_FF, FastForwardMode.Merge.TRUE));
		return ffmode;
	}

	private static boolean isMergeConfigOptionSet(String optionToLookFor,
			String[] mergeOptions) {
		for (String option : mergeOptions) {
			if (optionToLookFor.equals(option))
				return true;
		}
		return false;
	}

	private static String[] getMergeOptions(String branch, Config config) {
		String mergeOptions = config.getString(
				ConfigConstants.CONFIG_BRANCH_SECTION, branch,
				ConfigConstants.CONFIG_KEY_MERGEOPTIONS);
		if (mergeOptions != null) {
			return mergeOptions.split("\\s"); //$NON-NLS-1$
		}
		return new String[0];
	}

	private static class MergeConfigSectionParser implements
			SectionParser<MergeConfig> {

		private final String branch;

		public MergeConfigSectionParser(String branch) {
			this.branch = branch;
		}

		@Override
		public MergeConfig parse(Config cfg) {
			return new MergeConfig(branch, cfg);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MergeConfigSectionParser) {
				return branch.equals(((MergeConfigSectionParser) obj).branch);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return branch.hashCode();
		}

	}

}
