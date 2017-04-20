package com.penn.jba.realm.model;

import android.content.res.Resources;
import android.util.Log;

import com.penn.jba.PPApplication;
import com.penn.jba.R;
import com.penn.jba.util.PPHelper;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by penn on 09/04/2017.
 */

public class FootprintAll extends RealmObject {
    @PrimaryKey
    private String hash;

    private long createTime;

    private String id;

    private String status; //local, net, failed 暂时没用, 因为footprintAll中不包含自己创建的moment

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
            String idA = PPHelper.ppFromString(body, "detail.createdBy").getAsString();
            String idB = PPHelper.ppFromString(body, "detail.receivedBy").getAsString();
            String nicknameA = PPHelper.ppFromString(body, "relatedUsers.0.nickname").getAsString();
            String nicknameB = PPHelper.ppFromString(body, "relatedUsers.1.nickname").getAsString();
            if (idA == PPHelper.currentUserId) {
                return PPApplication.getContext().getString(R.string.i_send_a_mail_to) + nicknameB;
            } else {
                return nicknameA + PPApplication.getContext().getString(R.string.send_a_mail_to_me);
            }
        } else if (type == 9) {
            String idA = PPHelper.ppFromString(body, "detail.createdBy").getAsString();
            String idB = PPHelper.ppFromString(body, "detail.receivedBy").getAsString();
            String nicknameA = PPHelper.ppFromString(body, "relatedUsers.0.nickname").getAsString();
            String nicknameB = PPHelper.ppFromString(body, "relatedUsers.1.nickname").getAsString();
            if (idA == PPHelper.currentUserId) {
                String i_reply_to_sb = PPApplication.getContext().getString(R.string.i_reply_to_sb);
                return String.format(i_reply_to_sb, nicknameB);
            } else {
                String sb_reply_to_me = PPApplication.getContext().getString(R.string.sb_reply_to_me);
                return String.format(sb_reply_to_me, nicknameA);
            }
        }
        return "no type";
    }

    public String getAvatarName() {
        if (type == 8) {
            String idA = PPHelper.ppFromString(body, "detail.createdBy").getAsString();
            String idB = PPHelper.ppFromString(body, "detail.receivedBy").getAsString();

            if (idA == PPHelper.currentUserId) {
                return PPHelper.ppFromString(body, "relatedUsers.1.head").getAsString();
            } else {
                return PPHelper.ppFromString(body, "relatedUsers.0.head").getAsString();
            }
        } else if (type == 9) {
            String idA = PPHelper.ppFromString(body, "detail.createdBy").getAsString();
            String idB = PPHelper.ppFromString(body, "detail.receivedBy").getAsString();

            if (idA == PPHelper.currentUserId) {
                return PPHelper.ppFromString(body, "relatedUsers.1.head").getAsString();
            } else {
                return PPHelper.ppFromString(body, "relatedUsers.0.head").getAsString();
            }
        }
        return "no avatar";
    }
}
