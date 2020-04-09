package io.onedev.server.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.sshd.common.keyprovider.KeyPairProvider;

@Singleton
public class DefaultTestKeyPairProvider implements KeyPairProvider {

    private byte[] loadPEM(String resource) throws IOException {
        URL url = getClass().getResource(resource);
        InputStream in = url.openStream();
        String pem = new String(readAllBytes(in), StandardCharsets.ISO_8859_1);
        Pattern parse = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
        String encoded = parse.matcher(pem).replaceFirst("$1");
        return Base64.getDecoder().decode(encoded.replaceAll("\n", ""));
    }
    
    private byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (int read=0; read != -1; read = in.read(buf)) { baos.write(buf, 0, read); }
        return baos.toByteArray();
    }

    @Override
    public Iterable<KeyPair> loadKeys() {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(loadPEM("/test_private.pem")));
            PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(loadPEM("/test_public.pem")));

            KeyPair keyPair = new KeyPair(publicKey, privateKey);
            List<KeyPair> key = new ArrayList<>();
            
            key.add(keyPair);
            
            return key;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
