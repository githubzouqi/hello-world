package com.example.pc2.carmapproject.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * imxiaoqi
 * 货架id和地图上所在位置实体类
 */
public class HelperPodEntity implements Parcelable {

    private int podId;// 货架id
    private String position;// 货架所在位置

    public HelperPodEntity() {
    }

    public int getPodId() {
        return podId;
    }

    public void setPodId(int podId) {
        this.podId = podId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "HelperPodEntity{" +
                "podId=" + podId +
                ", position='" + position + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.podId);
        dest.writeString(this.position);
    }

    protected HelperPodEntity(Parcel in) {
        this.podId = in.readInt();
        this.position = in.readString();
    }

    public static final Parcelable.Creator<HelperPodEntity> CREATOR = new Parcelable.Creator<HelperPodEntity>() {
        @Override
        public HelperPodEntity createFromParcel(Parcel source) {
            return new HelperPodEntity(source);
        }

        @Override
        public HelperPodEntity[] newArray(int size) {
            return new HelperPodEntity[size];
        }
    };
}
