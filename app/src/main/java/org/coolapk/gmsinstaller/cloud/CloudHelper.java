package org.coolapk.gmsinstaller.cloud;

import org.coolapk.gmsinstaller.model.Gapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BobPeng on 2015/3/19.
 */
public class CloudHelper {
    public static final String APP_KEY = "c6EE3W2aqH0WsTOGZwt3ahub";
    public static List<Gapp> getOptionalGapps() {
        // TODO get from cloud
        Gapp search = new Gapp();
        search.displayName = "搜索";
        Gapp inbox = new Gapp();
        inbox.displayName = "Inbox";
        Gapp gmail = new Gapp();
        gmail.displayName = "Gmail";
        Gapp keep = new Gapp();
        keep.displayName = "keep";
        List<Gapp> gapps = new ArrayList<>();
        gapps.add(search);
        gapps.add(inbox);
        gapps.add(gmail);
        gapps.add(keep);
        return gapps;
    }
}
