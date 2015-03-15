package com.pmease.gitplex.web.page.repository.code.commit.diff.patch;

import static org.eclipse.jgit.lib.Constants.encodeASCII;
import static org.eclipse.jgit.util.RawParseUtils.decode;
import static org.eclipse.jgit.util.RawParseUtils.decodeNoFallback;
import static org.eclipse.jgit.util.RawParseUtils.extractBinaryString;
import static org.eclipse.jgit.util.RawParseUtils.match;
import static org.eclipse.jgit.util.RawParseUtils.nextLF;
import static org.eclipse.jgit.util.RawParseUtils.parseBase10;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.patch.BinaryHunk;
import org.eclipse.jgit.util.QuotedString;
import org.eclipse.jgit.util.RawParseUtils;
import org.eclipse.jgit.util.TemporaryBuffer;

/** Patch header describing an action for a single file path. */
public class FileHeader extends DiffEntry {
	private static final byte[] OLD_MODE = encodeASCII("old mode "); //$NON-NLS-1$

	private static final byte[] NEW_MODE = encodeASCII("new mode "); //$NON-NLS-1$

	static final byte[] DELETED_FILE_MODE = encodeASCII("deleted file mode "); //$NON-NLS-1$

	static final byte[] NEW_FILE_MODE = encodeASCII("new file mode "); //$NON-NLS-1$

	private static final byte[] COPY_FROM = encodeASCII("copy from "); //$NON-NLS-1$

	private static final byte[] COPY_TO = encodeASCII("copy to "); //$NON-NLS-1$

	private static final byte[] RENAME_OLD = encodeASCII("rename old "); //$NON-NLS-1$

	private static final byte[] RENAME_NEW = encodeASCII("rename new "); //$NON-NLS-1$

	private static final byte[] RENAME_FROM = encodeASCII("rename from "); //$NON-NLS-1$

	private static final byte[] RENAME_TO = encodeASCII("rename to "); //$NON-NLS-1$

	private static final byte[] SIMILARITY_INDEX = encodeASCII("similarity index "); //$NON-NLS-1$

	private static final byte[] DISSIMILARITY_INDEX = encodeASCII("dissimilarity index "); //$NON-NLS-1$

	static final byte[] INDEX = encodeASCII("index "); //$NON-NLS-1$

	static final byte[] OLD_NAME = encodeASCII("--- "); //$NON-NLS-1$

	static final byte[] NEW_NAME = encodeASCII("+++ "); //$NON-NLS-1$

	/** Type of patch used by this file. */
	public static enum PatchType {
		/** A traditional unified diff style patch of a text file. */
		UNIFIED,

		/** An empty patch with a message "Binary files ... differ" */
		BINARY,

		/** A Git binary patch, holding pre and post image deltas */
		GIT_BINARY;
	}

	/** Buffer holding the patch data for this file. */
	final byte[] buf;

	/** Offset within {@link #buf} to the "diff ..." line. */
	final int startOffset;

	/** Position 1 past the end of this file within {@link #buf}. */
	int endOffset;

	/** Type of patch used to modify this file */
	PatchType patchType;

	/** The hunks of this file */
	private List<HunkHeader> hunks;

	/** If {@link #patchType} is {@link PatchType#GIT_BINARY}, the new image */
	BinaryHunk forwardBinaryHunk;

	/** If {@link #patchType} is {@link PatchType#GIT_BINARY}, the old image */
	BinaryHunk reverseBinaryHunk;

	final DiffStat diffStat = new DiffStat();
	
	/**
	 * Constructs a new FileHeader
	 *
	 * @param headerLines
	 *            buffer holding the diff header for this file
	 * @param edits
	 *            the edits for this file
	 * @param type
	 *            the type of patch used to modify this file
	 */
	public FileHeader(final byte[] headerLines, EditList edits, PatchType type) {
		this(headerLines, 0);
		endOffset = headerLines.length;
		int ptr = parseGitFileName(Patch.DIFF_GIT.length, headerLines.length);
		parseGitHeaders(ptr, headerLines.length);
		this.patchType = type;
		addHunk(new HunkHeader(this, edits));
	}

	FileHeader(final byte[] b, final int offset) {
		buf = b;
		startOffset = offset;
		changeType = ChangeType.MODIFY; // unless otherwise designated
		patchType = PatchType.UNIFIED;
	}

	int getParentCount() {
		return 1;
	}

	/** @return the byte array holding this file's patch script. */
	public byte[] getBuffer() {
		return buf;
	}

	/** @return offset the start of this file's script in {@link #getBuffer()}. */
	public int getStartOffset() {
		return startOffset;
	}

	/** @return offset one past the end of the file script. */
	public int getEndOffset() {
		return endOffset;
	}

	/**
	 * Convert the patch script for this file into a string.
	 * <p>
	 * The default character encoding ({@link Constants#CHARSET}) is assumed for
	 * both the old and new files.
	 *
	 * @return the patch script, as a Unicode string.
	 */
	public String getScriptText() {
		return getScriptText(null, null);
	}

	/**
	 * Convert the patch script for this file into a string.
	 *
	 * @param oldCharset
	 *            hint character set to decode the old lines with.
	 * @param newCharset
	 *            hint character set to decode the new lines with.
	 * @return the patch script, as a Unicode string.
	 */
	public String getScriptText(Charset oldCharset, Charset newCharset) {
		return getScriptText(new Charset[] { oldCharset, newCharset });
	}

	String getScriptText(Charset[] charsetGuess) {
		if (getHunks().isEmpty()) {
			// If we have no hunks then we can safely assume the entire
			// patch is a binary style patch, or a meta-data only style
			// patch. Either way the encoding of the headers should be
			// strictly 7-bit US-ASCII and the body is either 7-bit ASCII
			// (due to the base 85 encoding used for a BinaryHunk) or is
			// arbitrary noise we have chosen to ignore and not understand
			// (e.g. the message "Binary files ... differ").
			//
			return extractBinaryString(buf, startOffset, endOffset);
		}

		if (charsetGuess != null && charsetGuess.length != getParentCount() + 1)
			throw new IllegalArgumentException(MessageFormat.format(
					JGitText.get().expectedCharacterEncodingGuesses,
					Integer.valueOf(getParentCount() + 1)));

		if (trySimpleConversion(charsetGuess)) {
			Charset cs = charsetGuess != null ? charsetGuess[0] : null;
			if (cs == null)
				cs = Constants.CHARSET;
			try {
				return decodeNoFallback(cs, buf, startOffset, endOffset);
			} catch (CharacterCodingException cee) {
				// Try the much slower, more-memory intensive version which
				// can handle a character set conversion patch.
			}
		}

		final StringBuilder r = new StringBuilder(endOffset - startOffset);

		// Always treat the headers as US-ASCII; Git file names are encoded
		// in a C style escape if any character has the high-bit set.
		//
		final int hdrEnd = getHunks().get(0).getStartOffset();
		for (int ptr = startOffset; ptr < hdrEnd;) {
			final int eol = Math.min(hdrEnd, nextLF(buf, ptr));
			r.append(extractBinaryString(buf, ptr, eol));
			ptr = eol;
		}

		final String[] files = extractFileLines(charsetGuess);
		final int[] offsets = new int[files.length];
		for (final HunkHeader h : getHunks())
			h.extractFileLines(r, files, offsets);
		return r.toString();
	}

	private static boolean trySimpleConversion(final Charset[] charsetGuess) {
		if (charsetGuess == null)
			return true;
		for (int i = 1; i < charsetGuess.length; i++) {
			if (charsetGuess[i] != charsetGuess[0])
				return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private String[] extractFileLines(final Charset[] csGuess) {
		final TemporaryBuffer[] tmp = new TemporaryBuffer[getParentCount() + 1];
		try {
			for (int i = 0; i < tmp.length; i++)
				tmp[i] = new TemporaryBuffer.LocalFile();
			for (final HunkHeader h : getHunks())
				h.extractFileLines(tmp);

			final String[] r = new String[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				Charset cs = csGuess != null ? csGuess[i] : null;
				if (cs == null)
					cs = Constants.CHARSET;
				r[i] = RawParseUtils.decode(cs, tmp[i].toByteArray());
			}
			return r;
		} catch (IOException ioe) {
			throw new RuntimeException(JGitText.get().cannotConvertScriptToText, ioe);
		} finally {
			for (final TemporaryBuffer b : tmp) {
				if (b != null)
					b.destroy();
			}
		}
	}

	/** @return style of patch used to modify this file */
	public PatchType getPatchType() {
		return patchType;
	}

	/** @return true if this patch modifies metadata about a file */
	public boolean hasMetaDataChanges() {
		return changeType != ChangeType.MODIFY || newMode != oldMode;
	}

	/** @return hunks altering this file; in order of appearance in patch */
	public List<? extends HunkHeader> getHunks() {
		if (hunks == null)
			return Collections.emptyList();
		return hunks;
	}

	void addHunk(final HunkHeader h) {
		if (h.getFileHeader() != this)
			throw new IllegalArgumentException(JGitText.get().hunkBelongsToAnotherFile);
		if (hunks == null)
			hunks = new ArrayList<HunkHeader>();
		hunks.add(h);
	}

	HunkHeader newHunkHeader(final int offset) {
		return new HunkHeader(this, offset);
	}

	/** @return if a {@link PatchType#GIT_BINARY}, the new-image delta/literal */
	public BinaryHunk getForwardBinaryHunk() {
		return forwardBinaryHunk;
	}

	/** @return if a {@link PatchType#GIT_BINARY}, the old-image delta/literal */
	public BinaryHunk getReverseBinaryHunk() {
		return reverseBinaryHunk;
	}

	/** @return a list describing the content edits performed on this file. */
	public EditList toEditList() {
		final EditList r = new EditList();
		for (final HunkHeader hunk : hunks)
			r.addAll(hunk.toEditList());
		return r;
	}

	/**
	 * Parse a "diff --git" or "diff --cc" line.
	 *
	 * @param ptr
	 *            first character after the "diff --git " or "diff --cc " part.
	 * @param end
	 *            one past the last position to parse.
	 * @return first character after the LF at the end of the line; -1 on error.
	 */
	int parseGitFileName(int ptr, final int end) {
		final int eol = nextLF(buf, ptr);
		final int bol = ptr;
		if (eol >= end) {
			return -1;
		}

		// buffer[ptr..eol] looks like "a/foo b/foo\n". After the first
		// A regex to match this is "^[^/]+/(.*?) [^/+]+/\1\n$". There
		// is only one way to split the line such that text to the left
		// of the space matches the text to the right, excluding the part
		// before the first slash.
		//

		final int aStart = nextLF(buf, ptr, '/');
		if (aStart >= eol)
			return eol;

		while (ptr < eol) {
			final int sp = nextLF(buf, ptr, ' ');
			if (sp >= eol) {
				// We can't split the header, it isn't valid.
				// This may be OK if this is a rename patch.
				//
				return eol;
			}
			final int bStart = nextLF(buf, sp, '/');
			if (bStart >= eol)
				return eol;

			// If buffer[aStart..sp - 1] = buffer[bStart..eol - 1]
			// we have a valid split.
			//
			if (eq(aStart, sp - 1, bStart, eol - 1)) {
				if (buf[bol] == '"') {
					// We're a double quoted name. The region better end
					// in a double quote too, and we need to decode the
					// characters before reading the name.
					//
					if (buf[sp - 2] != '"') {
						return eol;
					}
					oldPath = QuotedString.GIT_PATH.dequote(buf, bol, sp - 1);
					oldPath = p1(oldPath);
				} else {
					oldPath = decode(Constants.CHARSET, buf, aStart, sp - 1);
				}
				newPath = oldPath;
				return eol;
			}

			// This split wasn't correct. Move past the space and try
			// another split as the space must be part of the file name.
			//
			ptr = sp;
		}

		return eol;
	}

	int parseGitHeaders(int ptr, final int end) {
		while (ptr < end) {
			final int eol = nextLF(buf, ptr);
			if (isHunkHdr(buf, ptr, eol) >= 1) {
				// First hunk header; break out and parse them later.
				break;

			} else if (match(buf, ptr, OLD_NAME) >= 0) {
				parseOldName(ptr, eol);

			} else if (match(buf, ptr, NEW_NAME) >= 0) {
				parseNewName(ptr, eol);

			} else if (match(buf, ptr, OLD_MODE) >= 0) {
				oldMode = parseFileMode(ptr + OLD_MODE.length, eol);

			} else if (match(buf, ptr, NEW_MODE) >= 0) {
				newMode = parseFileMode(ptr + NEW_MODE.length, eol);

			} else if (match(buf, ptr, DELETED_FILE_MODE) >= 0) {
				oldMode = parseFileMode(ptr + DELETED_FILE_MODE.length, eol);
				newMode = FileMode.MISSING;
				changeType = ChangeType.DELETE;

			} else if (match(buf, ptr, NEW_FILE_MODE) >= 0) {
				parseNewFileMode(ptr, eol);

			} else if (match(buf, ptr, COPY_FROM) >= 0) {
				oldPath = parseName(oldPath, ptr + COPY_FROM.length, eol);
				changeType = ChangeType.COPY;

			} else if (match(buf, ptr, COPY_TO) >= 0) {
				newPath = parseName(newPath, ptr + COPY_TO.length, eol);
				changeType = ChangeType.COPY;

			} else if (match(buf, ptr, RENAME_OLD) >= 0) {
				oldPath = parseName(oldPath, ptr + RENAME_OLD.length, eol);
				changeType = ChangeType.RENAME;

			} else if (match(buf, ptr, RENAME_NEW) >= 0) {
				newPath = parseName(newPath, ptr + RENAME_NEW.length, eol);
				changeType = ChangeType.RENAME;

			} else if (match(buf, ptr, RENAME_FROM) >= 0) {
				oldPath = parseName(oldPath, ptr + RENAME_FROM.length, eol);
				changeType = ChangeType.RENAME;

			} else if (match(buf, ptr, RENAME_TO) >= 0) {
				newPath = parseName(newPath, ptr + RENAME_TO.length, eol);
				changeType = ChangeType.RENAME;

			} else if (match(buf, ptr, SIMILARITY_INDEX) >= 0) {
				score = parseBase10(buf, ptr + SIMILARITY_INDEX.length, null);

			} else if (match(buf, ptr, DISSIMILARITY_INDEX) >= 0) {
				score = parseBase10(buf, ptr + DISSIMILARITY_INDEX.length, null);

			} else if (match(buf, ptr, INDEX) >= 0) {
				parseIndexLine(ptr + INDEX.length, eol);

			} else {
				// Probably an empty patch (stat dirty).
				break;
			}

			ptr = eol;
		}
		return ptr;
	}

	void parseOldName(int ptr, final int eol) {
		oldPath = p1(parseName(oldPath, ptr + OLD_NAME.length, eol));
		if (oldPath == DEV_NULL)
			changeType = ChangeType.ADD;
	}

	void parseNewName(int ptr, final int eol) {
		newPath = p1(parseName(newPath, ptr + NEW_NAME.length, eol));
		if (newPath == DEV_NULL)
			changeType = ChangeType.DELETE;
	}

	void parseNewFileMode(int ptr, final int eol) {
		oldMode = FileMode.MISSING;
		newMode = parseFileMode(ptr + NEW_FILE_MODE.length, eol);
		changeType = ChangeType.ADD;
	}

	int parseTraditionalHeaders(int ptr, final int end) {
		while (ptr < end) {
			final int eol = nextLF(buf, ptr);
			if (isHunkHdr(buf, ptr, eol) >= 1) {
				// First hunk header; break out and parse them later.
				break;

			} else if (match(buf, ptr, OLD_NAME) >= 0) {
				parseOldName(ptr, eol);

			} else if (match(buf, ptr, NEW_NAME) >= 0) {
				parseNewName(ptr, eol);

			} else {
				// Possibly an empty patch.
				break;
			}

			ptr = eol;
		}
		return ptr;
	}

	private String parseName(final String expect, int ptr, final int end) {
		if (ptr == end)
			return expect;

		String r;
		if (buf[ptr] == '"') {
			// New style GNU diff format
			//
			r = QuotedString.GIT_PATH.dequote(buf, ptr, end - 1);
		} else {
			// Older style GNU diff format, an optional tab ends the name.
			//
			int tab = end;
			while (ptr < tab && buf[tab - 1] != '\t')
				tab--;
			if (ptr == tab)
				tab = end;
			r = decode(Constants.CHARSET, buf, ptr, tab - 1);
		}

		if (r.equals(DEV_NULL))
			r = DEV_NULL;
		return r;
	}

	private static String p1(final String r) {
		final int s = r.indexOf('/');
		return s > 0 ? r.substring(s + 1) : r;
	}

	FileMode parseFileMode(int ptr, final int end) {
		int tmp = 0;
		while (ptr < end - 1) {
			tmp <<= 3;
			tmp += buf[ptr++] - '0';
		}
		return FileMode.fromBits(tmp);
	}

	void parseIndexLine(int ptr, final int end) {
		// "index $asha1..$bsha1[ $mode]" where $asha1 and $bsha1
		// can be unique abbreviations
		//
		final int dot2 = nextLF(buf, ptr, '.');
		final int mode = nextLF(buf, dot2, ' ');

		oldId = AbbreviatedObjectId.fromString(buf, ptr, dot2 - 1);
		newId = AbbreviatedObjectId.fromString(buf, dot2 + 1, mode - 1);

		if (mode < end)
			newMode = oldMode = parseFileMode(mode, end);
	}

	private boolean eq(int aPtr, int aEnd, int bPtr, int bEnd) {
		if (aEnd - aPtr != bEnd - bPtr) {
			return false;
		}
		while (aPtr < aEnd) {
			if (buf[aPtr++] != buf[bPtr++])
				return false;
		}
		return true;
	}

	/**
	 * Determine if this is a patch hunk header.
	 *
	 * @param buf
	 *            the buffer to scan
	 * @param start
	 *            first position in the buffer to evaluate
	 * @param end
	 *            last position to consider; usually the end of the buffer (
	 *            <code>buf.length</code>) or the first position on the next
	 *            line. This is only used to avoid very long runs of '@' from
	 *            killing the scan loop.
	 * @return the number of "ancestor revisions" in the hunk header. A
	 *         traditional two-way diff ("@@ -...") returns 1; a combined diff
	 *         for a 3 way-merge returns 3. If this is not a hunk header, 0 is
	 *         returned instead.
	 */
	static int isHunkHdr(final byte[] buf, final int start, final int end) {
		int ptr = start;
		while (ptr < end && buf[ptr] == '@')
			ptr++;
		if (ptr - start < 2)
			return 0;
		if (ptr == end || buf[ptr++] != ' ')
			return 0;
		if (ptr == end || buf[ptr++] != '-')
			return 0;
		return (ptr - 3) - start;
	}
	
	public DiffStat getDiffStat() {
		return diffStat;
	}
}
