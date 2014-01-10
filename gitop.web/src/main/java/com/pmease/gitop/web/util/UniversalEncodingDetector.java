package com.pmease.gitop.web.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.tika.metadata.Metadata;
import org.mozilla.universalchardet.UniversalDetector;

import com.google.common.io.Closeables;

/**
 * Copied from tika UniversalEncodingDetector
 *
 */
public class UniversalEncodingDetector {
	private static final int BUFSIZE = 1024;

    private static final int LOOKAHEAD = 16 * BUFSIZE;

    public static Charset detect(InputStream input)
            throws IOException {
        if (input == null) {
            return null;
        }

        input.mark(LOOKAHEAD);
        try {
            UniversalEncodingListener listener =
                    new UniversalEncodingListener(new Metadata());

            byte[] b = new byte[BUFSIZE];
            int n = 0;
            int m = input.read(b);
            while (m != -1 && n < LOOKAHEAD && !listener.isDone()) {
                n += m;
                listener.handleData(b, 0, m);
                m = input.read(b, 0, Math.min(b.length, LOOKAHEAD - n));
            }

            return listener.dataEnd();
        } catch (IOException e) {
            throw e;
        } catch (LinkageError e) {
            return null; // juniversalchardet is not available
        } finally {
            input.reset();
        }
    }
    
    public static Charset detect(byte[] buffer) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(buffer); //ByteStreams.newInputStreamSupplier(buffer).getInput();
		try {
			return detect(in);
		} finally {
			Closeables.close(in, false);
		}
    }
    
    public static boolean isBinary(InputStream in) throws IOException {
        byte[] buf = new byte[4];
        in.mark(5);
        int len = in.read(buf);
        in.reset();

        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(buf, 0, len);
        if (detector.isDone()) {
            return false;
        }

        //Not UTF check ASCII text
        in.mark(LOOKAHEAD);
        len = 0;
        int b;
        while ((b = in.read()) != -1 && len < (LOOKAHEAD - 192)) {
            len++;
            if (b == 0) {
                in.reset();
                return true;
            }
        }
        in.reset();
        return false;
    }

}
