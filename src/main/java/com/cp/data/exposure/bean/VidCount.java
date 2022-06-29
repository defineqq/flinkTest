package com.cp.data.exposure.bean;

public  class VidCount {
    public String vid;
    public long conut;

    @Override
    public String toString() {
        return "WordWithCount{" +
                "vid='" + vid + '\'' +
                ", conut=" + conut +
                '}';
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }


    public Long getConut() {
        return conut;
    }

    public void setConut(Long conut) {
        this.conut = conut;
    }

    public VidCount(String vid, Long conut) {
        this.vid = vid;
        this.conut = conut;
    }
}