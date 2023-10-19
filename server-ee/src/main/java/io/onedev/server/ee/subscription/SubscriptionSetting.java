package io.onedev.server.ee.subscription;

import io.onedev.license.LicensePayload;
import io.onedev.license.LicenseeUpdate;
import io.onedev.license.SubscriptionCharge;
import io.onedev.license.TrialSubscription;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.CipherService;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;

public class SubscriptionSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	static final CipherService cipherService = new AesCipherService();

	/*
	 * OneDev license (https://code.onedev.io/onedev/server/~files/main/license.txt) does NOT allow
	 * anyone to encrypt/decrypt custom data with below key
	 */
	static final String ENCRYPTION_KEY = "5pV39IaAmO88SI3frAcadT/uwdh/WtrKqho/Opp+nNg=";

	private Set<String> usedSubscriptionKeyUUIDs = new LinkedHashSet<>();

	private Subscription subscription;

	public Set<String> getUsedSubscriptionKeyUUIDs() {
		return usedSubscriptionKeyUUIDs;
	}

	@Nullable
	public Subscription getSubscription() {
		return subscription;
	}

	public void setSubscription(@Nullable Subscription subscription) {
		this.subscription = subscription;
	}

	@Nullable
	public String install(String subscriptionKey) {
		var payload = LicensePayload.verifyLicense(subscriptionKey);
		if (payload != null) {
			if (!usedSubscriptionKeyUUIDs.contains(payload.getUuid())) {
				var now = new DateTime();
				if (payload.getValidUntil().after(now.toDate())) {
					var licenseGroup = subscription != null? subscription.getLicenseGroup(): null;
					if (subscription == null || subscription.isTrial()) {
						if (payload instanceof TrialSubscription) {
							TrialSubscription trialSubscription = (TrialSubscription) payload;
							if (trialSubscription.isCheckUUID() && !trialSubscription.getUuid().equals(OneDev.getInstance(SettingManager.class).getSystemUUID())) {
								return "Trial subscription key not applicable for this installation";
							} else {
								subscription = new Subscription();
								subscription.setLicensee(trialSubscription.getLicensee());
								if (trialSubscription.getLicenseGroup() != null)
									subscription.setLicenseGroup(trialSubscription.getLicenseGroup());
								else
									subscription.setLicenseGroup(licenseGroup);
								subscription.setUserDays(trialSubscription.getDays());
								subscription.setTrial(true);
								usedSubscriptionKeyUUIDs.add(payload.getUuid());
								return null;
							}
						} else if (payload instanceof LicenseeUpdate) {
							if (subscription == null) {
								return "Subscription key not applicable: this key is intended to update licensee of an existing subscription";
							} else {
								LicenseeUpdate licenseeUpdate = (LicenseeUpdate) payload;
								subscription.setLicensee(licenseeUpdate.getLicensee());
								usedSubscriptionKeyUUIDs.add(payload.getUuid());
								return null;
							}
						} else {
							SubscriptionCharge subscriptionCharge = (SubscriptionCharge) payload;
							subscription = new Subscription();
							subscription.setLicensee(subscriptionCharge.getLicensee());
							if (subscriptionCharge.getLicenseGroup() != null)
								subscription.setLicenseGroup(subscriptionCharge.getLicenseGroup());
							else
								subscription.setLicenseGroup(licenseGroup);
							subscription.setUserDays(subscriptionCharge.getUserMonths() * 31);
							usedSubscriptionKeyUUIDs.add(payload.getUuid());
							return null;
						}
					} else if (payload instanceof TrialSubscription) {
						return "Subscription key not applicable: this key is intended to activate a trial subscription";
					} else if (payload instanceof LicenseeUpdate) {
						LicenseeUpdate licenseeUpdate = (LicenseeUpdate) payload;
						subscription.setLicensee(licenseeUpdate.getLicensee());
						usedSubscriptionKeyUUIDs.add(payload.getUuid());
						return null;
					} else {
						SubscriptionCharge subscriptionCharge = (SubscriptionCharge) payload;
						subscription.setUserDays(subscription.getUserDays() + subscriptionCharge.getUserMonths() * 31);
						if (subscriptionCharge.getLicenseGroup() != null)
							subscription.setLicenseGroup(subscriptionCharge.getLicenseGroup());
						else
							subscription.setLicenseGroup(licenseGroup);
						usedSubscriptionKeyUUIDs.add(payload.getUuid());
						return null;
					}
				} else {
					return "This subscription key was expired";
				}
			} else {
				return "This subscription key was already used";
			}
		} else {
			return "Invalid subscription key";
		}
	}

	private static SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}

	public static SubscriptionSetting load() {
		var data = getSettingManager().getSubscriptionData();
		if (data == null)
			return new SubscriptionSetting();
		else
			return deserialize(cipherService.decrypt(decodeBase64(data), decodeBase64(ENCRYPTION_KEY)).getBytes());
	}

	public void save() {
		getSettingManager().saveSubscriptionData(encodeBase64String(cipherService.encrypt(serialize(this), decodeBase64(ENCRYPTION_KEY)).getBytes()));
	}

}
