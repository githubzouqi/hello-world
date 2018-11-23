package com.example.pc2.carmapproject.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 小车心跳或实时包未收到超时监听返回数据实体类
 */
public class RtHeartTimeoutEntity implements Parcelable {

    private int robotId;// 小车id
    private int type;// 小车类型。3：表示叉车、1：表示可以驮pod的小车
    private String time;// 时间（格式：yyyy-MM-dd HH:mm:ss）

    public RtHeartTimeoutEntity() {
    }

    public RtHeartTimeoutEntity(int robotId, int type, String time) {
        this.robotId = robotId;
        this.type = type;
        this.time = time;
    }

    public int getRobotId() {
        return robotId;
    }

    public void setRobotId(int robotId) {
        this.robotId = robotId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "RtHeartTimeoutEntity{" +
                "robotId=" + robotId +
                ", type=" + type +
                ", time='" + time + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.robotId);
        dest.writeInt(this.type);
        dest.writeString(this.time);
    }

    protected RtHeartTimeoutEntity(Parcel in) {
        this.robotId = in.readInt();
        this.type = in.readInt();
        this.time = in.readString();
    }

    public static final Creator<RtHeartTimeoutEntity> CREATOR = new Creator<RtHeartTimeoutEntity>() {
        @Override
        public RtHeartTimeoutEntity createFromParcel(Parcel source) {
            return new RtHeartTimeoutEntity(source);
        }

        @Override
        public RtHeartTimeoutEntity[] newArray(int size) {
            return new RtHeartTimeoutEntity[size];
        }
    };
}
