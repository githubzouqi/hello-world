package com.example.pc2.carmapproject.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 货架及点位实体
 * 一个货架对应有一个点位
 */
public class PodAddressEntity implements Parcelable {

    private int podCodeID;// 货架的id
    private int addressCodeID;// 货架对应的点位坐标

    public PodAddressEntity() {
    }

    public PodAddressEntity(int podCodeID, int addressCodeID) {
        this.podCodeID = podCodeID;
        this.addressCodeID = addressCodeID;
    }

    public int getPodCodeID() {
        return podCodeID;
    }

    public void setPodCodeID(int podCodeID) {
        this.podCodeID = podCodeID;
    }

    public int getAddressCodeID() {
        return addressCodeID;
    }

    public void setAddressCodeID(int addressCodeID) {
        this.addressCodeID = addressCodeID;
    }

    @Override
    public String toString() {
        return "PodAddressEntity{" +
                "podCodeID=" + podCodeID +
                ", addressCodeID=" + addressCodeID +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.podCodeID);
        dest.writeInt(this.addressCodeID);
    }

    protected PodAddressEntity(Parcel in) {
        this.podCodeID = in.readInt();
        this.addressCodeID = in.readInt();
    }

    public static final Parcelable.Creator<PodAddressEntity> CREATOR = new Parcelable.Creator<PodAddressEntity>() {
        @Override
        public PodAddressEntity createFromParcel(Parcel source) {
            return new PodAddressEntity(source);
        }

        @Override
        public PodAddressEntity[] newArray(int size) {
            return new PodAddressEntity[size];
        }
    };
}
