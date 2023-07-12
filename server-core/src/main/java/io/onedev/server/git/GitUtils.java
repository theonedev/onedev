package io.onedev.server.git;

import com.google.common.base.Objects;
import com.google.common.base.*;
import com.google.common.collect.Iterables;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.git.command.IsAncestorCommand;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.git.exception.ObsoleteCommitException;
import io.onedev.server.git.exception.RefUpdateException;
import io.onedev.server.git.service.DiffEntryFacade;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.util.GpgUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.Merger;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.SystemReader;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.jgit.lib.Constants.R_HEADS;
import static org.eclipse.jgit.lib.Constants.R_TAGS;

public class GitUtils {

	public static final int SHORT_SHA_LENGTH = 8;

	private static final Logger logger = LoggerFactory.getLogger(GitUtils.class);

	public static boolean isEmptyPath(String path) {
		return Strings.isNullOrEmpty(path) || Objects.equal(path, DiffEntry.DEV_NULL);
	}

	public static String abbreviateSHA(String sha, int length) {
		Preconditions.checkArgument(ObjectId.isId(sha));
		return sha.substring(0, length);
	}

	public static String abbreviateSHA(String sha) {
		return abbreviateSHA(sha, SHORT_SHA_LENGTH);
	}

	@Nullable
	public static String getDefaultBranch(Repository repository) {
		try {
			Ref headRef = repository.findRef("HEAD");
			if (headRef != null
					&& headRef.isSymbolic()
					&& headRef.getTarget().getName().startsWith(R_HEADS)
					&& headRef.getObjectId() != null) {
				return Repository.shortenRefName(headRef.getTarget().getName());
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setDefaultBranch(Repository repository, String defaultBranch) {
		var defaultBranchRef = branch2ref(defaultBranch);
		try {
			if (repository.findRef(defaultBranchRef) != null) {
				RefUpdate refUpdate = getRefUpdate(repository, "HEAD");
				linkRef(refUpdate, branch2ref(defaultBranch));
			} else {
				throw new ExplicitException("Branch not exist: " + defaultBranch);	
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<DiffEntry> diff(Repository repository, AnyObjectId oldRevId, AnyObjectId newRevId) {
		List<DiffEntry> diffs = new ArrayList<>();
		try (DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE);
			 RevWalk revWalk = new RevWalk(repository);
			 ObjectReader reader = repository.newObjectReader();) {
			diffFormatter.setRepository(repository);
			diffFormatter.setDetectRenames(true);
			diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);

			CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
			if (!oldRevId.equals(ObjectId.zeroId()))
				oldTreeParser.reset(reader, revWalk.parseCommit(oldRevId).getTree());

			CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
			if (!newRevId.equals(ObjectId.zeroId()))
				newTreeParser.reset(reader, revWalk.parseCommit(newRevId).getTree());

			for (DiffEntry entry : diffFormatter.scan(oldTreeParser, newTreeParser)) {
				if (!Objects.equal(entry.getOldPath(), entry.getNewPath())
						|| !Objects.equal(entry.getOldMode(), entry.getNewMode()) || entry.getOldId() == null
						|| !entry.getOldId().isComplete() || entry.getNewId() == null || !entry.getNewId().isComplete()
						|| !entry.getOldId().equals(entry.getNewId())) {
					diffs.add(entry);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return diffs;
	}

	public static InputStream getInputStream(Repository repository, ObjectId revId, String path) {
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevTree revTree = revWalk.parseCommit(revId).getTree();
			TreeWalk treeWalk = TreeWalk.forPath(repository, path, revTree);
			if (treeWalk != null) {
				ObjectLoader objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
				return objectLoader.openStream();
			} else {
				throw new ObjectNotFoundException("Unable to find blob path '" + path + "' in revision '" + revId + "'");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public static RevCommit getLastCommit(Repository repository) {
		RevCommit lastCommit = null;
		try (RevWalk revWalk = new RevWalk(repository)) {
			for (Ref ref: repository.getRefDatabase().getRefsByPrefix(R_HEADS)) {
				if (ref.getObjectId() != null) {
					RevCommit commit =  parseCommit(revWalk, ref.getObjectId());
					if (commit != null && (lastCommit == null || lastCommit.getCommitTime() < commit.getCommitTime())) {
						lastCommit = commit;
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return lastCommit;
	}

	@Nullable
	public static String getDetailMessage(RevCommit commit) {
		int start = 0;
		String fullMessage = commit.getFullMessage();
		while (true) {
			int index = fullMessage.indexOf('\n', start);
			if (index == -1)
				return null;
			start = index + 1;
			int nextIndex = fullMessage.indexOf('\n', start);
			if (nextIndex == -1)
				return null;
			start = nextIndex + 1;
			if (fullMessage.substring(index, nextIndex).trim().length() == 0) {
				String detailMessage = fullMessage.substring(start).trim();
				return detailMessage.length() != 0 ? detailMessage : null;
			}
		}
	}

	public static PersonIdent newPersonIdent(String name, String email, Date when) {
		return new PersonIdent(name, email, when.getTime(), SystemReader.getInstance().getTimezone(when.getTime()));
	}

	/**
	 * Parse the original git raw date to Java date. The raw git date is in unix
	 * timestamp with timezone like: 1392312299 -0800
	 *
	 * @param input the input raw date string
	 * @return Java date
	 */
	public static Date parseRawDate(String input) {
		String[] pieces = Iterables.toArray(Splitter.on(" ").split(input), String.class);
		return new Date(Long.valueOf(pieces[0]) * 1000L);
	}

	public static int comparePath(@Nullable String path1, @Nullable String path2) {
		List<String> segments1 = splitPath(path1);
		List<String> segments2 = splitPath(path2);

		int index = 0;
		for (String segment1 : segments1) {
			if (index < segments2.size()) {
				int result = segment1.compareTo(segments2.get(index));
				if (result != 0)
					return result;
			} else {
				return 1;
			}
			index++;
		}
		if (index < segments2.size())
			return -1;
		else
			return 0;
	}

	public static List<String> splitPath(@Nullable String path) {
		List<String> pathSegments;
		if (path != null)
			pathSegments = Splitter.on("/").omitEmptyStrings().splitToList(path);
		else
			pathSegments = new ArrayList<>();
		return pathSegments;
	}

	public static @Nullable String normalizePath(@Nullable String path) {
		List<String> pathSegments = splitPath(PathUtils.normalizeDots(path));
		if (!pathSegments.isEmpty())
			return Joiner.on("/").join(pathSegments);
		else
			return null;
	}

	/**
	 * Convert a git reference name to branch name.
	 *
	 * @param refName name of the git reference
	 * @return name of the branch, or <tt>null</tt> if specified ref does not
	 *         represent a branch
	 */
	public static @Nullable String ref2branch(String refName) {
		if (refName.startsWith(R_HEADS))
			return refName.substring(R_HEADS.length());
		else
			return null;
	}

	public static String branch2ref(String branch) {
		if (!branch.startsWith(R_HEADS))
			return R_HEADS + branch;
		else 
			return branch;
	}

	/**
	 * Convert a git reference name to tag name.
	 *
	 * @param refName name of the git reference
	 * @return name of the tag, or <tt>null</tt> if specified ref does not represent
	 *         a tag
	 */
	public static @Nullable String ref2tag(String refName) {
		if (refName.startsWith(R_TAGS))
			return refName.substring(R_TAGS.length());
		else
			return null;
	}

	public static String tag2ref(String tag) {
		if (!tag.startsWith(R_TAGS))
			return R_TAGS + tag;
		else 
			return tag;
	}

	public static BlobIdent getOldBlobIdent(DiffEntryFacade diffEntry, String oldRev) {
		BlobIdent blobIdent;
		if (diffEntry.getChangeType() != ChangeType.ADD) {
			blobIdent = new BlobIdent(oldRev, diffEntry.getOldPath(), diffEntry.getOldMode());
		} else {
			blobIdent = new BlobIdent(oldRev, null, null);
		}
		return blobIdent;
	}

	public static Collection<RevCommit> getReachableCommits(Repository repository,
														   Collection<ObjectId> sinceCommits,
														   Collection<ObjectId> untilCommits) {
		try (RevWalk revWalk = new RevWalk(repository)) {
			var reachableCommits = new LinkedHashSet<RevCommit>();
			for (var commitId: untilCommits)
				revWalk.markStart(revWalk.parseCommit(commitId));
			for (var commitId: sinceCommits)
				revWalk.markUninteresting(revWalk.parseCommit(commitId));
			RevCommit commit;
			while ((commit = revWalk.next()) != null)
				reachableCommits.add(commit);
			return reachableCommits;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static BlobIdent getNewBlobIdent(DiffEntryFacade diffEntry, String newRev) {
		BlobIdent blobIdent;
		if (diffEntry.getChangeType() != ChangeType.DELETE) {
			blobIdent = new BlobIdent(newRev, diffEntry.getNewPath(), diffEntry.getNewMode());
		} else {
			blobIdent = new BlobIdent(newRev, null, null);
		}
		return blobIdent;
	}

	/**
	 * @return merge base of specified commits, or <tt>null</tt> if two commits do
	 *         not have related history. In this case, these two commits can not be
	 *         merged
	 */
	@Nullable
	public static ObjectId getMergeBase(Repository repository, ObjectId commitId1, ObjectId commitId2) {
		try (RevWalk revWalk = new RevWalk(repository)) {
			revWalk.setRevFilter(RevFilter.MERGE_BASE);

			revWalk.markStart(revWalk.parseCommit(commitId1));
			revWalk.markStart(revWalk.parseCommit(commitId2));
			RevCommit mergeBase = revWalk.next();
			return mergeBase != null ? mergeBase.copy() : null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isMergedInto(Repository repository, @Nullable Map<String, String> gitEnvs, ObjectId base,
									   ObjectId tip) {
		if (gitEnvs != null && !gitEnvs.isEmpty()) {
			return new IsAncestorCommand(repository.getDirectory(), base.name(), tip.name(), gitEnvs).run();
		} else {
			try (RevWalk revWalk = new RevWalk(repository)) {
				RevCommit baseCommit;
				try {
					baseCommit = revWalk.parseCommit(base);
				} catch (MissingObjectException e) {
					return false;
				}
				return revWalk.isMergedInto(baseCommit, revWalk.parseCommit(tip));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Get commit of specified revision id.
	 *
	 * @param revWalk
	 * @param revId
	 * @return <tt>null</tt> if specified id does not exist or does not represent a
	 *         commit
	 */
	@Nullable
	public static RevCommit parseCommit(RevWalk revWalk, ObjectId revId) {
		RevObject peeled;
		try {
			peeled = revWalk.peel(revWalk.parseAny(revId));
			if (peeled instanceof RevCommit)
				return (RevCommit) peeled;
			else
				return null;
		} catch (MissingObjectException e) {
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Collection<RefFacade> getCommitRefs(Repository repository, @Nullable String prefix) {
		try (RevWalk revWalk = new RevWalk(repository)) {
			List<Ref> refs;
			if (prefix != null)
				refs = repository.getRefDatabase().getRefsByPrefix(prefix);
			else
				refs = repository.getRefDatabase().getRefs();
			return refs.stream()
					.map(ref->new RefFacade(revWalk, ref))
					.filter(refFacade->refFacade.getPeeledObj() instanceof RevCommit)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public static ObjectId resolve(Repository repository, String revision, boolean errorIfInvalid) {
		try {
			return repository.resolve(revision);
		} catch (RevisionSyntaxException | AmbiguousObjectException | IncorrectObjectTypeException e) {
			if (errorIfInvalid)
				throw new RuntimeException(e);
			else
				return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public static ObjectId rebase(Repository repository, ObjectId source, ObjectId target, PersonIdent committer) {
		try (RevWalk revWalk = new RevWalk(repository); ObjectInserter inserter = repository.newObjectInserter();) {
			RevCommit sourceCommit = revWalk.parseCommit(source);
			RevCommit targetCommit = revWalk.parseCommit(target);
			revWalk.setRevFilter(RevFilter.NO_MERGES);
			List<RevCommit> commits = RevWalkUtils.find(revWalk, sourceCommit, targetCommit);
			Collections.reverse(commits);
			RevCommit headCommit = targetCommit;
			for (RevCommit commit : commits) {
				ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository, true);
				merger.setBase(commit.getParent(0));
				if (merger.merge(headCommit, commit)) {
					if (!headCommit.getTree().getId().equals(merger.getResultTreeId())) {
						if (!commit.getTree().getId().equals(merger.getResultTreeId())
								|| !commit.getParent(0).equals(headCommit)) {
							CommitBuilder commitBuilder = new CommitBuilder();
							commitBuilder.setAuthor(commit.getAuthorIdent());
							commitBuilder.setCommitter(committer);
							commitBuilder.setParentId(headCommit);
							commitBuilder.setMessage(commit.getFullMessage());
							commitBuilder.setTreeId(merger.getResultTreeId());
							headCommit = revWalk.parseCommit(inserter.insert(commitBuilder));
						} else {
							headCommit = commit;
						}
					}
				} else {
					return null;
				}
			}
			inserter.flush();
			return headCommit.copy();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public static ObjectId merge(Repository repository, ObjectId targetCommitId, ObjectId sourceCommitId,
								 boolean squash, PersonIdent committer, PersonIdent author, String commitMessage,
								 boolean useOursOnConflict) {
		try (RevWalk revWalk = new RevWalk(repository); ObjectInserter inserter = repository.newObjectInserter();) {
			RevCommit sourceCommit = revWalk.parseCommit(sourceCommitId);
			RevCommit targetCommit = revWalk.parseCommit(targetCommitId);
			Merger merger;
			if (useOursOnConflict)
				merger = MergeStrategy.OURS.newMerger(repository, true);
			else
				merger = MergeStrategy.RECURSIVE.newMerger(repository, true);
			if (merger.merge(targetCommit, sourceCommit)) {
				CommitBuilder mergedCommit = new CommitBuilder();
				mergedCommit.setAuthor(author);
				mergedCommit.setCommitter(committer);
				if (squash)
					mergedCommit.setParentId(targetCommit);
				else
					mergedCommit.setParentIds(targetCommit, sourceCommit);
				mergedCommit.setMessage(commitMessage);
				mergedCommit.setTreeId(merger.getResultTreeId());
				ObjectId mergedCommitId = inserter.insert(mergedCommit);
				inserter.flush();
				return mergedCommitId;
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Collection<String> getChangedFiles(Repository repository, ObjectId oldCommitId,
													 ObjectId newCommitId) {
		Collection<String> changedFiles = new HashSet<>();
		try (RevWalk revWalk = new RevWalk(repository); TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.setFilter(TreeFilter.ANY_DIFF);
			treeWalk.setRecursive(true);
			RevCommit oldCommit = revWalk.parseCommit(oldCommitId);
			RevCommit newCommit = revWalk.parseCommit(newCommitId);
			treeWalk.addTree(oldCommit.getTree());
			treeWalk.addTree(newCommit.getTree());
			while (treeWalk.next()) {
				changedFiles.add(treeWalk.getPathString());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return changedFiles;
	}

	public static boolean isValid(File gitDir) {
		return new File(gitDir, "objects").exists();
	}

	public static RefUpdate getRefUpdate(Repository repository, String refName) {
		try {
			return repository.updateRef(refName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void updateRef(RefUpdate refUpdate) {
		try {
			RefUpdate.Result result = refUpdate.forceUpdate();
			if (result == RefUpdate.Result.LOCK_FAILURE && refUpdate.getExpectedOldObjectId() != null
					&& !refUpdate.getExpectedOldObjectId().equals(refUpdate.getOldObjectId())) {
				throw new ObsoleteCommitException(refUpdate.getOldObjectId());
			} else if (result != RefUpdate.Result.FAST_FORWARD && result != RefUpdate.Result.FORCED
					&& result != RefUpdate.Result.NEW && result != RefUpdate.Result.NO_CHANGE) {
				throw new RefUpdateException(result);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deleteRef(RefUpdate refUpdate) {
		try {
			refUpdate.setForceUpdate(true);
			RefUpdate.Result result = refUpdate.delete();
			if (result == RefUpdate.Result.LOCK_FAILURE && refUpdate.getExpectedOldObjectId() != null
					&& !refUpdate.getExpectedOldObjectId().equals(refUpdate.getOldObjectId())) {
				throw new ObsoleteCommitException(refUpdate.getOldObjectId());
			} else if (result != RefUpdate.Result.FAST_FORWARD && result != RefUpdate.Result.FORCED
					&& result != RefUpdate.Result.NEW && result != RefUpdate.Result.NO_CHANGE) {
				throw new RefUpdateException(result);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void linkRef(RefUpdate refUpdate, String target) {
		try {
			RefUpdate.Result result = refUpdate.link(target);
			if (result != RefUpdate.Result.FORCED && result != RefUpdate.Result.NEW
					&& result != RefUpdate.Result.NO_CHANGE)
				throw new RefUpdateException(result);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getBlobName(String blobPath) {
		String blobName = blobPath;
		if (blobPath.indexOf('/') != -1)
			blobName = StringUtils.substringAfterLast(blobPath, "/");
		return blobName;
	}
	
	public static void sign(ObjectBuilder object, PGPSecretKeyRing signingKey) {
		JcePBESecretKeyDecryptorBuilder decryptorBuilder = new JcePBESecretKeyDecryptorBuilder()
				.setProvider(BouncyCastleProvider.PROVIDER_NAME);
		PGPPrivateKey privateKey;
		try {
			privateKey = signingKey.getSecretKey().extractPrivateKey(
					decryptorBuilder.build(new char[0]));
		} catch (PGPException e) {
			throw new RuntimeException(e);
		}

		PGPPublicKey publicKey = signingKey.getPublicKey();

		PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
				new JcaPGPContentSignerBuilder(publicKey.getAlgorithm(), HashAlgorithmTags.SHA256)
						.setProvider(BouncyCastleProvider.PROVIDER_NAME));
		try {
			signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);
			PGPSignatureSubpacketGenerator subpackets = new PGPSignatureSubpacketGenerator();
			subpackets.setIssuerFingerprint(false, publicKey);

			String emailAddress = GpgUtils.getEmailAddress(publicKey.getUserIDs().next());
			subpackets.addSignerUserID(false, emailAddress);

			signatureGenerator.setHashedSubpackets(subpackets.generate());
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			try (BCPGOutputStream out = new BCPGOutputStream(new ArmoredOutputStream(buffer))) {
				signatureGenerator.update(object.build());
				signatureGenerator.generate().encode(out);
			}
			object.setGpgSignature(new GpgSignature(buffer.toByteArray()));
		} catch (IOException | PGPException e) {
			throw new RuntimeException(e);
		}
	}

}
