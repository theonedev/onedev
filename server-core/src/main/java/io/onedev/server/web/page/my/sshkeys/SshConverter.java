package io.onedev.server.web.page.my.sshkeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Locale;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import io.onedev.server.git.ssh.SshKeyUtils;

public class SshConverter implements IConverter<String> {

    private FormComponent<String> keyContentField;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SshConverter(FormComponent<String> keyContentField) {
        super();
        this.keyContentField = keyContentField;
    }

    @Override
    public String convertToObject(String value, Locale locale) throws ConversionException {
        
        try {
            PublicKey pubEntry = SshKeyUtils.loadPublicKey(keyContentField.getInput());
            String fingerPrint = KeyUtils.getFingerPrint(SshKeyUtils.MD5_DIGESTER, pubEntry);
            
            return fingerPrint;
        } catch (IOException | GeneralSecurityException e) {
            throw new ConversionException("xxx");
        }
    }

    @Override
    public String convertToString(String value, Locale locale) {
        return value;
    }

}
