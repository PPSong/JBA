package com.penn.jba.realm.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by penn on 09/04/2017.
 */

public class CurrentUserSetting extends RealmObject {
    //记录是否在足迹页面当前显示的是我的moment
    private boolean footprintMine;

    public boolean isFootprintMine() {
        return footprintMine;
    }

    public void setFootprintMine(boolean footprintMine) {
        this.footprintMine = footprintMine;
    }
}
