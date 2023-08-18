package io.onedev.license;

import io.onedev.server.annotation.Editable;

import java.io.Serializable;

@Editable
public class ServerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String installType;

    private String os;

    private String jvm;

    private String totalMemory;

    private String usedMemory;

    private String systemDate;

    @Editable(order=100)
    public String getInstallType() {
        return installType;
    }

    public void setInstallType(String installType) {
        this.installType = installType;
    }

    @Editable(order=200, name="OS")
    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    @Editable(order=300, name="JVM")
    public String getJvm() {
        return jvm;
    }

    public void setJvm(String jvm) {
        this.jvm = jvm;
    }

    @Editable(order=400)
    public String getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(String totalMemory) {
        this.totalMemory = totalMemory;
    }

    @Editable(order=500)
    public String getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(String usedMemory) {
        this.usedMemory = usedMemory;
    }

    @Editable(order=600)
    public String getSystemDate() {
        return systemDate;
    }

    public void setSystemDate(String systemDate) {
        this.systemDate = systemDate;
    }

}
