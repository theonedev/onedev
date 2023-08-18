package io.onedev.server.ee.subscription;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.validation.Validatable;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Editable(name="Install Subscription Key")
@ClassValidating
public class SubscriptionKeyEditBean implements Serializable, Validatable {
	
	private String subscriptionKey;

	@Editable(placeholder = "Paste subscription key here")
	@NotEmpty
	@OmitName
	@Multiline(monospace = true)
	public String getSubscriptionKey() {
		return subscriptionKey;
	}

	public void setSubscriptionKey(String subscriptionKey) {
		this.subscriptionKey = subscriptionKey;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		var subscriptionSetting = SubscriptionSetting.load();
		var errorMessage = subscriptionSetting.install(subscriptionKey);
		if (errorMessage != null) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		}
		return true;
	}
	
}
