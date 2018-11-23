package com.example.pc2.carmapproject.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 地图主界面选项实体类：包含选项图标和图标说明
 */
public class MenuEntity implements Parcelable {

    private int iconId;// 图标资源id
    private String iconShows;// 图标说明

    public MenuEntity() {
    }

    public MenuEntity(int iconId, String iconShows) {
        this.iconId = iconId;
        this.iconShows = iconShows;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getIconShows() {
        return iconShows;
    }

    public void setIconShows(String iconShows) {
        this.iconShows = iconShows;
    }

    @Override
    public String toString() {
        return "MenuEntity{" +
                "iconId=" + iconId +
                ", iconShows='" + iconShows + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.iconId);
        dest.writeString(this.iconShows);
    }

    protected MenuEntity(Parcel in) {
        this.iconId = in.readInt();
        this.iconShows = in.readString();
    }

    public static final Parcelable.Creator<MenuEntity> CREATOR = new Parcelable.Creator<MenuEntity>() {
        @Override
        public MenuEntity createFromParcel(Parcel source) {
            return new MenuEntity(source);
        }

        @Override
        public MenuEntity[] newArray(int size) {
            return new MenuEntity[size];
        }
    };
}
