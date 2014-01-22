package com.pmease.gitop.web.page.project.source.commit.diff.patch;

import static com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader.NEW_NAME;
import static com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader.OLD_NAME;
import static com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader.isHunkHdr;
import static org.eclipse.jgit.lib.Constants.encodeASCII;
import static org.eclipse.jgit.util.RawParseUtils.match;
import static org.eclipse.jgit.util.RawParseUtils.nextLF;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.util.TemporaryBuffer;

public class Patch {
	static final byte[] DIFF_GIT = encodeASCII("diff --git "); //$NON-NLS-1$

	private static final byte[] DIFF_CC = encodeASCII("diff --cc "); //$NON-NLS-1$

	private static final byte[] DIFF_COMBINED = encodeASCII("diff --combined "); //$NON-NLS-1$

	private static final byte[][] BIN_HEADERS = new byte[][] {
			encodeASCII("Binary files "), encodeASCII("Files "), }; //$NON-NLS-1$ //$NON-NLS-2$

	private static final byte[] BIN_TRAILER = encodeASCII(" differ\n"); //$NON-NLS-1$

	private static final byte[] GIT_BINARY = encodeASCII("GIT binary patch\n"); //$NON-NLS-1$

	static final byte[] SIG_FOOTER = encodeASCII("-- \n"); //$NON-NLS-1$

	/** The files, in the order they were parsed out of the input. */
	private final List<FileHeader> files;

	/** Formatting errors, if any were identified. */
	private final List<FormatError> errors;

	final DiffStat diffStat = new DiffStat();
	
	/** Create an empty patch. */
	public Patch() {
		files = new ArrayList<FileHeader>();
		errors = new ArrayList<FormatError>(0);
	}

	/**
	 * Add a single file to this patch.
	 * <p>
	 * Typically files should be added by parsing the text through one of this
	 * class's parse methods.
	 *
	 * @param fh
	 *            the header of the file.
	 */
	public void addFile(final FileHeader fh) {
		files.add(fh);
	}

	/** @return list of files described in the patch, in occurrence order. */
	public List<? extends FileHeader> getFiles() {
		return files;
	}

	/**
	 * Add a formatting error to this patch script.
	 *
	 * @param err
	 *            the error description.
	 */
	public void addError(final FormatError err) {
		errors.add(err);
	}

	/** @return collection of formatting errors, if any. */
	public List<FormatError> getErrors() {
		return errors;
	}

	/**
	 * Parse a patch received from an InputStream.
	 * <p>
	 * Multiple parse calls on the same instance will concatenate the patch
	 * data, but each parse input must start with a valid file header (don't
	 * split a single file across parse calls).
	 *
	 * @param is
	 *            the stream to read the patch data from. The stream is read
	 *            until EOF is reached.
	 * @throws IOException
	 *             there was an error reading from the input stream.
	 */
	public void parse(final InputStream is) throws IOException {
		final byte[] buf = readFully(is);
		parse(buf, 0, buf.length);
	}

	private static byte[] readFully(final InputStream is) throws IOException {
		final TemporaryBuffer b = new TemporaryBuffer.LocalFile();
		try {
			b.copy(is);
			b.close();
			return b.toByteArray();
		} finally {
			b.destroy();
		}
	}

	/**
	 * Parse a patch stored in a byte[].
	 * <p>
	 * Multiple parse calls on the same instance will concatenate the patch
	 * data, but each parse input must start with a valid file header (don't
	 * split a single file across parse calls).
	 *
	 * @param buf
	 *            the buffer to parse.
	 * @param ptr
	 *            starting position to parse from.
	 * @param end
	 *            1 past the last position to end parsing. The total length to
	 *            be parsed is <code>end - ptr</code>.
	 */
	public void parse(final byte[] buf, int ptr, final int end) {
		while (ptr < end)
			ptr = parseFile(buf, ptr, end);
	}

	private int parseFile(final byte[] buf, int c, final int end) {
		while (c < end) {
			if (isHunkHdr(buf, c, end) >= 1) {
				// If we find a disconnected hunk header we might
				// have missed a file header previously. The hunk
				// isn't valid without knowing where it comes from.
				//
				error(buf, c, JGitText.get().hunkDisconnectedFromFile);
				c = nextLF(buf, c);
				continue;
			}

			// Valid git style patch?
			//
			if (match(buf, c, DIFF_GIT) >= 0)
				return parseDiffGit(buf, c, end);
			if (match(buf, c, DIFF_CC) >= 0)
				return parseDiffCombined(DIFF_CC, buf, c, end);
			if (match(buf, c, DIFF_COMBINED) >= 0)
				return parseDiffCombined(DIFF_COMBINED, buf, c, end);

			// Junk between files? Leading junk? Traditional
			// (non-git generated) patch?
			//
			final int n = nextLF(buf, c);
			if (n >= end) {
				// Patches cannot be only one line long. This must be
				// trailing junk that we should ignore.
				//
				return end;
			}

			if (n - c < 6) {
				// A valid header must be at least 6 bytes on the
				// first line, e.g. "--- a/b\n".
				//
				c = n;
				continue;
			}

			if (match(buf, c, OLD_NAME) >= 0 && match(buf, n, NEW_NAME) >= 0) {
				// Probably a traditional patch. Ensure we have at least
				// a "@@ -0,0" smelling line next. We only check the "@@ -".
				//
				final int f = nextLF(buf, n);
				if (f >= end)
					return end;
				if (isHunkHdr(buf, f, end) == 1)
					return parseTraditionalPatch(buf, c, end);
			}

			c = n;
		}
		return c;
	}

	private int parseDiffGit(final byte[] buf, final int start, final int end) {
		final FileHeader fh = new FileHeader(buf, start);
		int ptr = fh.parseGitFileName(start + DIFF_GIT.length, end);
		if (ptr < 0)
			return skipFile(buf, start);

		ptr = fh.parseGitHeaders(ptr, end);
		ptr = parseHunks(fh, ptr, end);
		fh.endOffset = ptr;
		addFile(fh);
		return ptr;
	}

	private int parseDiffCombined(final byte[] hdr, final byte[] buf,
			final int start, final int end) {
		throw new UnsupportedOperationException();
	}
	
	private int parseTraditionalPatch(final byte[] buf, final int start,
			final int end) {
		throw new UnsupportedOperationException();
	}
	
//	private int parseDiffCombined(final byte[] hdr, final byte[] buf,
//			final int start, final int end) {
//		final CombinedFileHeader fh = new CombinedFileHeader(buf, start);
//		int ptr = fh.parseGitFileName(start + hdr.length, end);
//		if (ptr < 0)
//			return skipFile(buf, start);
//
//		ptr = fh.parseGitHeaders(ptr, end);
//		ptr = parseHunks(fh, ptr, end);
//		fh.endOffset = ptr;
//		addFile(fh);
//		return ptr;
//	}

//	private int parseTraditionalPatch(final byte[] buf, final int start,
//			final int end) {
//		final FileHeader fh = new FileHeader(buf, start);
//		int ptr = fh.parseTraditionalHeaders(start, end);
//		ptr = parseHunks(fh, ptr, end);
//		fh.endOffset = ptr;
//		addFile(fh);
//		return ptr;
//	}

	private static int skipFile(final byte[] buf, int ptr) {
		ptr = nextLF(buf, ptr);
		if (match(buf, ptr, OLD_NAME) >= 0)
			ptr = nextLF(buf, ptr);
		return ptr;
	}

	private int parseHunks(final FileHeader fh, int c, final int end) {
		final byte[] buf = fh.buf;
		while (c < end) {
			// If we see a file header at this point, we have all of the
			// hunks for our current file. We should stop and report back
			// with this position so it can be parsed again later.
			//
			if (match(buf, c, DIFF_GIT) >= 0)
				break;
			if (match(buf, c, DIFF_CC) >= 0)
				break;
			if (match(buf, c, DIFF_COMBINED) >= 0)
				break;
			if (match(buf, c, OLD_NAME) >= 0)
				break;
			if (match(buf, c, NEW_NAME) >= 0)
				break;

			if (isHunkHdr(buf, c, end) == fh.getParentCount()) {
				final HunkHeader h = fh.newHunkHeader(c);
				h.parseHeader();
				c = h.parseBody(this, end);
				h.endOffset = c;
				fh.addHunk(h);
				if (c < end) {
					switch (buf[c]) {
					case '@':
					case 'd':
					case '\n':
						break;
					default:
						if (match(buf, c, SIG_FOOTER) < 0)
							warn(buf, c, JGitText.get().unexpectedHunkTrailer);
					}
				}
				continue;
			}

			final int eol = nextLF(buf, c);
			if (fh.getHunks().isEmpty() && match(buf, c, GIT_BINARY) >= 0) {
				fh.patchType = FileHeader.PatchType.GIT_BINARY;
				return parseGitBinary(fh, eol, end);
			}

			if (fh.getHunks().isEmpty() && BIN_TRAILER.length < eol - c
					&& match(buf, eol - BIN_TRAILER.length, BIN_TRAILER) >= 0
					&& matchAny(buf, c, BIN_HEADERS)) {
				// The patch is a binary file diff, with no deltas.
				//
				fh.patchType = FileHeader.PatchType.BINARY;
				return eol;
			}

			// Skip this line and move to the next. Its probably garbage
			// after the last hunk of a file.
			//
			c = eol;
		}

		if (fh.getHunks().isEmpty()
				&& fh.getPatchType() == FileHeader.PatchType.UNIFIED
				&& !fh.hasMetaDataChanges()) {
			// Hmm, an empty patch? If there is no metadata here we
			// really have a binary patch that we didn't notice above.
			//
			fh.patchType = FileHeader.PatchType.BINARY;
		}

		return c;
	}

	private int parseGitBinary(final FileHeader fh, int c, final int end) {
		throw new UnsupportedOperationException();
	}
	
//	private int parseGitBinary(final FileHeader fh, int c, final int end) {
//		final BinaryHunk postImage = new BinaryHunk(fh, c);
//		final int nEnd = postImage.parseHunk(c, end);
//		if (nEnd < 0) {
//			// Not a binary hunk.
//			//
//			error(fh.buf, c, JGitText.get().missingForwardImageInGITBinaryPatch);
//			return c;
//		}
//		c = nEnd;
//		postImage.endOffset = c;
//		fh.forwardBinaryHunk = postImage;
//
//		final BinaryHunk preImage = new BinaryHunk(fh, c);
//		final int oEnd = preImage.parseHunk(c, end);
//		if (oEnd >= 0) {
//			c = oEnd;
//			preImage.endOffset = c;
//			fh.reverseBinaryHunk = preImage;
//		}
//
//		return c;
//	}

	void warn(final byte[] buf, final int ptr, final String msg) {
		addError(new FormatError(buf, ptr, FormatError.Severity.WARNING, msg));
	}

	void error(final byte[] buf, final int ptr, final String msg) {
		addError(new FormatError(buf, ptr, FormatError.Severity.ERROR, msg));
	}

	private static boolean matchAny(final byte[] buf, final int c,
			final byte[][] srcs) {
		for (final byte[] s : srcs) {
			if (match(buf, c, s) >= 0)
				return true;
		}
		return false;
	}
	
	public DiffStat getDiffStat() {
		return diffStat;
	}
}
