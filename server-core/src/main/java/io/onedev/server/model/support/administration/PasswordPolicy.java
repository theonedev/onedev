package io.onedev.server.model.support.administration;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.Editable;

@Editable
public class PasswordPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    private int minLength = 8;
    
    private boolean mustContainUppercase = true;

    private boolean mustContainLowercase = true;

    private boolean mustContainNumber = true;

    private boolean mustContainSpecial = true;

    @Editable(order=100, name="PasswordMinimum Length", description="Minimum length of the password")
    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    @Editable(order=200, name="Password Must Contain Uppercase", description="Whether the password must contain at least one uppercase letter")
    public boolean isMustContainUppercase() {
        return mustContainUppercase;
    }

    public void setMustContainUppercase(boolean mustContainUppercase) {
        this.mustContainUppercase = mustContainUppercase;
    }

    @Editable(order=300, name="Password Must Contain Lowercase", description="Whether the password must contain at least one lowercase letter")
    public boolean isMustContainLowercase() {
        return mustContainLowercase;
    }

    public void setMustContainLowercase(boolean mustContainLowercase) {
        this.mustContainLowercase = mustContainLowercase;
    }

    @Editable(order=400, name="Password Must Contain Digit", description="Whether the password must contain at least one number")
    public boolean isMustContainNumber() {
        return mustContainNumber;
    }

    public void setMustContainNumber(boolean mustContainNumber) {
        this.mustContainNumber = mustContainNumber;
    }

    @Editable(order=500, name="Password Must Contain Special Character", description="Whether the password must contain at least one special character")
    public boolean isMustContainSpecial() {
        return mustContainSpecial;
    }

    public void setMustContainSpecial(boolean mustContainSpecial) {
        this.mustContainSpecial = mustContainSpecial;
    }

    @Nullable
    public String checkPassword(String password) {
        boolean hasErrors = false;
        if (password.length() < minLength) 
            hasErrors = true;
        
        if (mustContainUppercase && !password.matches(".*[A-Z].*")) 
            hasErrors = true;
        
        if (mustContainLowercase && !password.matches(".*[a-z].*")) 
            hasErrors = true;
        
        if (mustContainNumber && !password.matches(".*\\d.*")) 
            hasErrors = true;
        
        if (mustContainSpecial && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) 
            hasErrors = true;
        
        if (hasErrors) {
            StringBuilder requirements = new StringBuilder("Password requirements:\n");
            requirements.append("- Minimum length: ").append(minLength).append(" characters\n");            
            if (mustContainUppercase) 
                requirements.append("- Must contain at least one uppercase letter\n");
            
            if (mustContainLowercase) 
                requirements.append("- Must contain at least one lowercase letter\n");
            
            if (mustContainNumber) 
                requirements.append("- Must contain at least one number\n");
            
            if (mustContainSpecial) 
                requirements.append("- Must contain at least one special character\n");
            
            return requirements.toString();
        } else {
            return null;
        }
    }
}
