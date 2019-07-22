package io.onedev.server.util;

import java.nio.charset.Charset;

import org.apache.tika.detect.TextStatistics;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.utils.CharsetUtils;
import org.mozilla.universalchardet.CharsetListener;
import org.mozilla.universalchardet.Constants;
import org.mozilla.universalchardet.UniversalDetector;

/**
 * Copied from tika-parsers
 */

/**
 * Helper class used by {@link UniversalEncodingDetector} to access the
 * <code>juniversalchardet</code> detection logic.
 */
class UniversalEncodingListener implements CharsetListener {

    private static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

    private static final String CHARSET_ISO_8859_15 = "ISO-8859-15";

    private final TextStatistics statistics = new TextStatistics();

    private final UniversalDetector detector = new UniversalDetector(this);

    private String hint = null;

    private Charset charset = null;

    public UniversalEncodingListener(Metadata metadata) {
        MediaType type = MediaType.parse(metadata.get(Metadata.CONTENT_TYPE));
        if (type != null) {
            hint = type.getParameters().get("charset");
        }
        if (hint == null) {
            hint = metadata.get(Metadata.CONTENT_ENCODING);
        }
    }

    @Override
	public void report(String name) {
        if (Constants.CHARSET_WINDOWS_1252.equals(name)) {
            if (hint != null) {
                // Use the encoding hint when available
                name = hint;
            } else if (statistics.count('\r') == 0) {
                // If there are no CR(LF)s, then the encoding is more
                // likely to be ISO-8859-1(5) than windows-1252
                if (statistics.count(0xa4) > 0) { // currency/euro sign
                    // The general currency sign is hardly ever used in
                    // ISO-8859-1, so it's more likely that we're dealing
                    // with ISO-8859-15, where the character is used for
                    // the euro symbol, which is more commonly used.
                    name = CHARSET_ISO_8859_15;
                } else {
                    name = CHARSET_ISO_8859_1;
                }
            }
        }
        try {
            this.charset = CharsetUtils.forName(name);
        } catch (Exception e) {
            // ignore
        }
    }

    public boolean isDone() {
        return detector.isDone();
    }

    public void handleData(byte[] buf, int offset, int length) {
        statistics.addData(buf, offset, length);
        detector.handleData(buf, offset, length);
    }

    public Charset dataEnd() {
        detector.dataEnd();
        if (charset == null && statistics.isMostlyAscii()) {
            report(Constants.CHARSET_WINDOWS_1252);
        }
        return charset;
    }

}