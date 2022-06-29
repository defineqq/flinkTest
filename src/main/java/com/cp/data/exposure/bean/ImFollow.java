package com.cp.data.exposure.bean;


public class ImFollow {
    String uid;
    String vid;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }


    public ImFollow(String uid, String vid) {
        this.uid = uid;
        this.vid = vid;

    }
}
