package io.onedev.server.model.support;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;

public class TwoFactorAuthentication implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String secretKey;
	
	private final List<String> scratchCodes;
	
	public TwoFactorAuthentication(String secretKey, List<String> scratchCodes) {
		this.secretKey = secretKey;
		this.scratchCodes = scratchCodes;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public List<String> getScratchCodes() {
		return scratchCodes;
	}
	
	public String getTOTPCode() {
	    Base32 base32 = new Base32();
	    byte[] bytes = base32.decode(secretKey);
	    String hexKey = Hex.encodeHexString(bytes);
	    return TOTP.getOTP(hexKey);
	}
	
	public void writeQRCode(User user, int size, OutputStream os) {
		String barCode;
	    try {
			String serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
			String issuer = "OneDev@" + new URL(serverUrl).getHost();
	        barCode = "otpauth://totp/"
	                + URLEncoder.encode(issuer + ":" + user.getName(), "UTF-8").replace("+", "%20")
	                + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
	                + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
	    } catch (UnsupportedEncodingException | MalformedURLException e) {
	        throw new RuntimeException(e);
	    }
		try {
			BitMatrix matrix = new MultiFormatWriter().encode(barCode, BarcodeFormat.QR_CODE, size, size);
			MatrixToImageWriter.writeToStream(matrix, "png", os);
		} catch (WriterException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
