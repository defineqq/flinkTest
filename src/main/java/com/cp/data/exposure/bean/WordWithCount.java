package com.cp.data.exposure.bean;

public  class WordWithCount{
    public String vid;
    public String uid;
    public long conut;

    @Override
    public String toString() {
        return "WordWithCount{" +
                "vid='" + vid + '\'' +
                ", uid='" + uid + '\'' +
                ", conut=" + conut +
                '}';
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getConut() {
        return conut;
    }

    public void setConut(Long conut) {
        this.conut = conut;
    }

    public WordWithCount(String vid, String uid, Long conut) {
        this.vid = vid;
        this.uid = uid;
        this.conut = conut;
    }
}