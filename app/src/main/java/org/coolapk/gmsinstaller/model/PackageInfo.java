package org.coolapk.gmsinstaller.model;

import java.util.HashMap;

/**
 * Created by xifan on 15-4-1.
 */
public class PackageInfo extends HashMap<String, Object> {

    public String getPackageDescription() {
        Object obj = get("description");
        if (obj != null) {
            return obj.toString();
        } else {
            return null;
        }
    }

    public void setPackageDescription(String packageDescription) {
        put("description", packageDescription);
    }

    public Gpack getGpack() {
        Object obj = get("gpack");
        if (obj != null) {
            return (Gpack) obj;
        } else {
            return null;
        }
    }

    public void setGpack(Gpack gpack) {
        put("gpack", gpack);
    }

    public boolean isInstalled() {
        Object obj = get("installState");
        return obj != null && (boolean) obj;
    }

    public void setInstallState(boolean installState) {
        put("installState", installState);
    }
}
