package com.cp.data.exposure.bean;


public class ImOut {
    String distinct_id;
    String status;
    String followuid;
    Long time;

    @Override
    public String toString() {
        return "ImFollow{" +
                "distinct_id='" + distinct_id + '\'' +
                ", status='" + status + '\'' +
                ", followuid='" + followuid + '\'' +
                ", time=" + time +
                '}';
    }

    public String getDistinct_id() {
        return distinct_id;
    }

    public void setDistinct_id(String distinct_id) {
        this.distinct_id = distinct_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFollowuid() {
        return followuid;
    }

    public void setFollowuid(String followuid) {
        this.followuid = followuid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public ImOut(String distinct_id, String status, String followuid, Long time) {
        this.distinct_id = distinct_id;
        this.status = status;
        this.followuid = followuid;
        this.time = time;
    }
}
