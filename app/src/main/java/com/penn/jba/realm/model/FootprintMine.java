package com.penn.jba.realm.model;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.penn.jba.PPApplication;
import com.penn.jba.R;
import com.penn.jba.util.PPHelper;

import java.util.ArrayList;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import static android.R.attr.type;
import static com.penn.jba.util.PPHelper.ppFromString;

/**
 * Created by penn on 09/04/2017.
 */

public class FootprintMine extends RealmObject {
    @PrimaryKey
    private String hash;

    private long createTime;

    private String id;

    private String status; //local, net, failed

    private int type;

    private String body;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        if (type == 8) {
            String idA = ppFromString(body, "detail.createdBy").getAsString();
            String idB = ppFromString(body, "detail.receivedBy").getAsString();
            String nicknameA = ppFromString(body, "relatedUsers.0.nickname").getAsString();
            String nicknameB = ppFromString(body, "relatedUsers.1.nickname").getAsString();
            if (idA == PPHelper.currentUserId) {
                return PPApplication.getContext().getString(R.string.i_send_a_mail_to) + nicknameB;
            } else {
                return nicknameA + PPApplication.getContext().getString(R.string.send_a_mail_to_me);
            }
        } else if (type == 9) {
            String idA = ppFromString(body, "detail.createdBy").getAsString();
            String idB = ppFromString(body, "detail.receivedBy").getAsString();
            String nicknameA = ppFromString(body, "relatedUsers.0.nickname").getAsString();
            String nicknameB = ppFromString(body, "relatedUsers.1.nickname").getAsString();
            if (idA == PPHelper.currentUserId) {
                String i_reply_to_sb = PPApplication.getContext().getString((R.string.i_reply_to_sb));
                return String.format(i_reply_to_sb, nicknameB);
            } else {
                String sb_reply_to_me = PPApplication.getContext().getString((R.string.sb_reply_to_me));
                return String.format(sb_reply_to_me, nicknameA);
            }
        } else if (type == 1) {
            String idA = ppFromString(body, "relatedUsers.0.id").getAsString();
            String idB = ppFromString(body, "relatedUsers.1.id").getAsString();
            String nicknameA = ppFromString(body, "relatedUsers.0.nickname").getAsString();
            String nicknameB = ppFromString(body, "relatedUsers.1.nickname").getAsString();
            String beFriend = ppFromString(body, "detail.beFriend").getAsInt() == 1 ? PPApplication.getContext().getString(R.string.be_friend) : "";
            if (idA == PPHelper.currentUserId) {
                String i_follow_to_sb = PPApplication.getContext().getString(R.string.i_follow_to_sb);
                return String.format(i_follow_to_sb, nicknameB, beFriend);
            } else {
                String sb_follow_to_me = PPApplication.getContext().getString(R.string.sb_follow_to_me);
                return String.format(sb_follow_to_me, nicknameA, beFriend);
            }
        } else if (type == 3) {
            Log.v("pplog22", ppFromString(body, "detail.content").getAsString());
            return ppFromString(body, "detail.content").getAsString();
        }
        return "no type";
    }

    public String getAvatarName() {
        if (type == 8) {
            String idA = ppFromString(body, "detail.createdBy").getAsString();
            String idB = ppFromString(body, "detail.receivedBy").getAsString();

            if (idA == PPHelper.currentUserId) {
                return ppFromString(body, "relatedUsers.1.head").getAsString();
            } else {
                return ppFromString(body, "relatedUsers.0.head").getAsString();
            }
        } else if (type == 9) {
            String idA = ppFromString(body, "detail.createdBy").getAsString();
            String idB = ppFromString(body, "detail.receivedBy").getAsString();

            if (idA == PPHelper.currentUserId) {
                return ppFromString(body, "relatedUsers.1.head").getAsString();
            } else {
                return ppFromString(body, "relatedUsers.0.head").getAsString();
            }
        } else if (type == 1) {
            String idA = ppFromString(body, "relatedUsers.0.id").getAsString();
            String idB = ppFromString(body, "relatedUsers.1.id").getAsString();

            if (idA == PPHelper.currentUserId) {
                return ppFromString(body, "relatedUsers.1.head").getAsString();
            } else {
                return ppFromString(body, "relatedUsers.0.head").getAsString();
            }
        }
        return "no avatar";
    }

    public ArrayList<String> getImages() {
        ArrayList<String> result = new ArrayList();
        if (type == 3) {
            JsonArray pics = ppFromString(body, "detail.pics").getAsJsonArray();
            for (JsonElement item : pics) {
                result.add(item.getAsString());
            }
        } else {
            //do nothing
        }
        return result;
    }

    public String getPlace() {
        if (type == 3) {
            return ppFromString(body, "detail.location.city").getAsString() + ppFromString(body, "detail.location.detail").getAsString();
        } else {
            return "";
        }
    }
}
