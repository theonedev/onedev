package io.onedev.license;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.annotation.Editable;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

@Editable
public abstract class LicensePayload implements Serializable {

    private static final long serialVersionUID = 1L;
	
	private static final int VALID_DAYS = 14;

    private static final Logger logger = LoggerFactory.getLogger(LicensePayload.class);

    private static final int MAX_PAYLOAD_LEN = 8192;

    private static final String KEY_ALGORITHM = "RSA";

    private static final String SIGNATURE_ALGORITHM = "SHA512withRSA";

    private static final byte[] PUBLIC_KEY;

    private String uuid;

    private String licensee;

    private Date issueDate;

    static {
        try (var is = LicensePayload.class.getResourceAsStream("public.key")) {
            PUBLIC_KEY = IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
	
    public Date getIssueDate() {
        return issueDate;
    }
	
	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	@Editable(order=10)
	@NotEmpty
	public String getLicensee() {
		return licensee;
	}

	public void setLicensee(String licensee) {
		this.licensee = licensee;
	}
	
	public Date getValidUntil() {
		return new DateTime(issueDate).plusDays(VALID_DAYS).toDate();
	}

    public String generateLicense(byte[] privateKeyBytes) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

            var privateKey = keyFactory.generatePrivate(privateKeySpec);

            var payload = SerializationUtils.serialize(this);
            if (payload.length > MAX_PAYLOAD_LEN)
                throw new ExplicitException("License payload too long");

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(payload);
            var signed = signature.sign();

            var license = new byte[Integer.BYTES + payload.length + signed.length];
            ByteBuffer.wrap(license).putInt(payload.length);
            System.arraycopy(payload, 0, license, Integer.BYTES, payload.length);
            System.arraycopy(signed, 0, license, Integer.BYTES + payload.length, signed.length);
            return new String(Base64.encodeBase64Chunked(license));
        } catch (Exception e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    @Nullable
    public static LicensePayload verifyLicense(String license) {
        try {
            var licenseBytes = Base64.decodeBase64(license);
            var payloadLength = ByteBuffer.wrap(licenseBytes).getInt();

            if (payloadLength > MAX_PAYLOAD_LEN)
                return null;

            var payload = new byte[payloadLength];
            System.arraycopy(licenseBytes, Integer.BYTES, payload, 0, payloadLength);
            var signed = new byte[licenseBytes.length - Integer.BYTES - payloadLength];
            System.arraycopy(licenseBytes, Integer.BYTES + payloadLength, signed, 0, signed.length);

            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(PUBLIC_KEY);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(payload);
            if (signature.verify(signed))
                return SerializationUtils.deserialize(payload);
            else
                return null;
        } catch (Exception e) {
            logger.error("Error verifying license", e);
            return null;
        }
    }

}