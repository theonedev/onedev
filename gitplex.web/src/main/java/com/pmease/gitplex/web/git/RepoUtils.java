package com.pmease.gitplex.web.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.gitplex.web.util.JnaUtils;

public class RepoUtils {

	private static final Logger logger = LoggerFactory.getLogger(RepoUtils.class);
	
	private RepoUtils() {}
	
	public static Repository open(File dir) {
		try {
			RepositoryCache.FileKey key;
			key = RepositoryCache.FileKey.lenient(dir, FS.DETECTED);
			
			return new RepositoryBuilder()
						.setFS(FS.DETECTED)
						.setGitDir(key.getFile())
						.setMustExist(true)
						.build();
		} catch (RepositoryNotFoundException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}
	
	public static void close(Repository db) {
		RepositoryCache.close(db);
		if (db != null) {
			db.close();
		}
	}
	
	/**
	 * Encapsulates the result of cloning or pulling from a repository.
	 */
	public static class CloneResult {
		public String name;
		public FetchResult fetchResult;
		public boolean createdRepository;
	}

	/**
	 * Clone or Fetch a repository. If the local repository does not exist,
	 * clone is called. If the repository does exist, fetch is called. By
	 * default the clone/fetch retrieves the remote heads, tags, and notes.
	 *
	 * @param repositoriesFolder
	 * @param name
	 * @param fromUrl
	 * @return CloneResult
	 * @throws Exception
	 */
	public static CloneResult cloneRepository(File repositoriesFolder, String name, String fromUrl)
			throws Exception {
		return cloneRepository(repositoriesFolder, name, fromUrl, true, null);
	}

	/**
	 * Clone or Fetch a repository. If the local repository does not exist,
	 * clone is called. If the repository does exist, fetch is called. By
	 * default the clone/fetch retrieves the remote heads, tags, and notes.
	 *
	 * @param repositoriesFolder
	 * @param name
	 * @param fromUrl
	 * @param bare
	 * @param credentialsProvider
	 * @return CloneResult
	 * @throws Exception
	 */
	public static CloneResult cloneRepository(File repositoriesFolder, String name, String fromUrl,
			boolean bare, CredentialsProvider credentialsProvider) throws Exception {
		CloneResult result = new CloneResult();
		if (bare) {
			// bare repository, ensure .git suffix
			if (!name.toLowerCase().endsWith(Constants.DOT_GIT_EXT)) {
				name += Constants.DOT_GIT_EXT;
			}
		} else {
			// normal repository, strip .git suffix
			if (name.toLowerCase().endsWith(Constants.DOT_GIT_EXT)) {
				name = name.substring(0, name.indexOf(Constants.DOT_GIT_EXT));
			}
		}
		result.name = name;

		File folder = new File(repositoriesFolder, name);
		if (folder.exists()) {
			File gitDir = FileKey.resolve(new File(repositoriesFolder, name), FS.DETECTED);
			Repository repository = new FileRepositoryBuilder().setGitDir(gitDir).build();
			result.fetchResult = fetchRepository(credentialsProvider, repository);
			repository.close();
		} else {
			CloneCommand clone = new CloneCommand();
			clone.setBare(bare);
			clone.setCloneAllBranches(true);
			clone.setURI(fromUrl);
			clone.setDirectory(folder);
			if (credentialsProvider != null) {
				clone.setCredentialsProvider(credentialsProvider);
			}
			Repository repository = clone.call().getRepository();

			// Now we have to fetch because CloneCommand doesn't fetch
			// refs/notes nor does it allow manual RefSpec.
			result.createdRepository = true;
			result.fetchResult = fetchRepository(credentialsProvider, repository);
			repository.close();
		}
		return result;
	}

	/**
	 * Fetch updates from the remote repository. If refSpecs is unspecifed,
	 * remote heads, tags, and notes are retrieved.
	 *
	 * @param credentialsProvider
	 * @param repository
	 * @param refSpecs
	 * @return FetchResult
	 * @throws Exception
	 */
	public static FetchResult fetchRepository(CredentialsProvider credentialsProvider,
			Repository repository, RefSpec... refSpecs) throws Exception {
		Git git = new Git(repository);
		FetchCommand fetch = git.fetch();
		List<RefSpec> specs = new ArrayList<RefSpec>();
		if (refSpecs == null || refSpecs.length == 0) {
			specs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
			specs.add(new RefSpec("+refs/tags/*:refs/tags/*"));
			specs.add(new RefSpec("+refs/notes/*:refs/notes/*"));
		} else {
			specs.addAll(Arrays.asList(refSpecs));
		}
		if (credentialsProvider != null) {
			fetch.setCredentialsProvider(credentialsProvider);
		}
		fetch.setRefSpecs(specs);
		FetchResult fetchRes = fetch.call();
		return fetchRes;
	}

	/**
	 * Creates a bare repository.
	 *
	 * @param repositoriesFolder
	 * @param name
	 * @return Repository
	 */
	public static Repository createRepository(File repositoriesFolder, String name) {
		return createRepository(repositoriesFolder, name, "FALSE");
	}

	/**
	 * Creates a bare, shared repository.
	 *
	 * @param repositoriesFolder
	 * @param name
	 * @param shared
	 *          the setting for the --shared option of "git init".
	 * @return Repository
	 */
	public static Repository createRepository(File repositoriesFolder, String name, String shared) {
		try {
			Repository repo = null;
			try {
				Git git = Git.init().setDirectory(new File(repositoriesFolder, name)).setBare(true).call();
				repo = git.getRepository();
			} catch (GitAPIException e) {
				throw new RuntimeException(e);
			}

			GitConfigSharedRepository sharedRepository = new GitConfigSharedRepository(shared);
			if (sharedRepository.isShared()) {
				StoredConfig config = repo.getConfig();
				config.setString("core", null, "sharedRepository", sharedRepository.getValue());
				config.setBoolean("receive", null, "denyNonFastforwards", true);
				config.save();

				if (! JnaUtils.isWindows()) {
					Iterator<File> iter = org.apache.commons.io.FileUtils.iterateFilesAndDirs(repo.getDirectory(),
							TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
					// Adjust permissions on file/directory
					while (iter.hasNext()) {
						adjustSharedPerm(iter.next(), sharedRepository);
					}
				}
			}

			return repo;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private enum GitConfigSharedRepositoryValue
	{
		UMASK("0", 0), FALSE("0", 0), OFF("0", 0), NO("0", 0),
		GROUP("1", 0660), TRUE("1", 0660), ON("1", 0660), YES("1", 0660),
		ALL("2", 0664), WORLD("2", 0664), EVERYBODY("2", 0664),
		Oxxx(null, -1);

		private String configValue;
		private int permValue;
		private GitConfigSharedRepositoryValue(String config, int perm) { configValue = config; permValue = perm; };

		public String getConfigValue() { return configValue; };
		public int getPerm() { return permValue; };

	}

	private static class GitConfigSharedRepository
	{
		private int intValue;
		private GitConfigSharedRepositoryValue enumValue;

		GitConfigSharedRepository(String s) {
			if ( s == null || s.trim().isEmpty() ) {
				enumValue = GitConfigSharedRepositoryValue.GROUP;
			}
			else {
				try {
					// Try one of the string values
					enumValue = GitConfigSharedRepositoryValue.valueOf(s.trim().toUpperCase());
				} catch (IllegalArgumentException  iae) {
					try {
						// Try if this is an octal number
						int i = Integer.parseInt(s, 8);
						if ( (i & 0600) != 0600 ) {
							String msg = String.format("Problem with core.sharedRepository filemode value (0%03o).\nThe owner of files must always have read and write permissions.", i);
							throw new IllegalArgumentException(msg);
						}
						intValue = i & 0666;
						enumValue = GitConfigSharedRepositoryValue.Oxxx;
					} catch (NumberFormatException nfe) {
						throw new IllegalArgumentException("Bad configuration value for 'shared': '" + s + "'");
					}
				}
			}
		}

		String getValue() {
			if ( enumValue == GitConfigSharedRepositoryValue.Oxxx ) {
				if (intValue == 0) return "0";
				return String.format("0%o", intValue);
			}
			return enumValue.getConfigValue();
		}

		int getPerm() {
			if ( enumValue == GitConfigSharedRepositoryValue.Oxxx ) return intValue;
			return enumValue.getPerm();
		}

		boolean isCustom() {
			return enumValue == GitConfigSharedRepositoryValue.Oxxx;
		}

		boolean isShared() {
			return (enumValue.getPerm() > 0) || enumValue == GitConfigSharedRepositoryValue.Oxxx;
		}
	}
	
	/**
	 * Adjust file permissions of a file/directory for shared repositories
	 *
	 * @param path
	 * 			File that should get its permissions changed.
	 * @param configShared
	 * 			Configuration setting for the shared mode.
	 * @return Upon successful completion, a value of 0 is returned. Otherwise, a value of -1 is returned.
	 */
	public static int adjustSharedPerm(File path, GitConfigSharedRepository configShared) {
		if (! configShared.isShared()) return 0;
		if (! path.exists()) return -1;

		int perm = configShared.getPerm();
		JnaUtils.Filestat stat = JnaUtils.getFilestat(path);
		if (stat == null) return -1;
		int mode = stat.mode;
		if (mode < 0) return -1;

		// Now, here is the kicker: Under Linux, chmod'ing a sgid file whose guid is different from the process'
		// effective guid will reset the sgid flag of the file. Since there is no way to get the sgid flag back in
		// that case, we decide to rather not touch is and getting the right permissions will have to be achieved
		// in a different way, e.g. by using an appropriate umask for the Gitblit process.
		if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
			if ( ((mode & (JnaUtils.S_ISGID | JnaUtils.S_ISUID)) != 0)
				&& stat.gid != JnaUtils.getegid() ) {
				logger.debug("Not adjusting permissions to prevent clearing suid/sgid bits for '" + path + "'" );
				return 0;
			}
		}

		// If the owner has no write access, delete it from group and other, too.
		if ((mode & JnaUtils.S_IWUSR) == 0) perm &= ~0222;
		// If the owner has execute access, set it for all blocks that have read access.
		if ((mode & JnaUtils.S_IXUSR) == JnaUtils.S_IXUSR) perm |= (perm & 0444) >> 2;

		if (configShared.isCustom()) {
			// Use the custom value for access permissions.
			mode = (mode & ~0777) | perm;
		}
		else {
			// Just add necessary bits to existing permissions.
			mode |= perm;
		}

		if (path.isDirectory()) {
			mode |= (mode & 0444) >> 2;
			mode |= JnaUtils.S_ISGID;
		}

		return JnaUtils.setFilemode(path, mode);
	}
	
	/**
	 * Adjust file permissions of a file/directory for shared repositories
	 *
	 * @param path
	 * 			File that should get its permissions changed.
	 * @param configShared
	 * 			Configuration string value for the shared mode.
	 * @return Upon successful completion, a value of 0 is returned. Otherwise, a value of -1 is returned.
	 */
	public static int adjustSharedPerm(File path, String configShared) {
		return adjustSharedPerm(path, new GitConfigSharedRepository(configShared));
	}

	/**
	 * Determine if a repository has any commits. This is determined by checking
	 * the for loose and packed objects.
	 *
	 * @param repository
	 * @return true if the repository has commits
	 */
	public static boolean hasCommits(Repository repository) {
		if (repository != null && repository.getDirectory().exists()) {
			return (new File(repository.getDirectory(), "objects").list().length > 2)
					|| (new File(repository.getDirectory(), "objects/pack").list().length > 0);
		}
		return false;
	}
}
