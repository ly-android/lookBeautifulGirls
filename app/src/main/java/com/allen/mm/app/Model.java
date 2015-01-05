package com.allen.mm.app;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 描述:
 *
 * @author: liyong on 2014/12/31
 */
public class Model implements Parcelable,Serializable{
    //头像
    public String thumbnail_url;
    public int thumbnail_width;
    public int thumbnail_height;
    public int thumb_large_width;
    public int thumb_large_height;
    public String thumb_large_url;//中图
    public String image_url;//原图
    public String desc;//标题


    public Model() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.thumbnail_url);
        dest.writeInt(this.thumbnail_width);
        dest.writeInt(this.thumbnail_height);
        dest.writeInt(this.thumb_large_width);
        dest.writeInt(this.thumb_large_height);
        dest.writeString(this.thumb_large_url);
        dest.writeString(this.image_url);
        dest.writeString(this.desc);
    }

    private Model(Parcel in) {
        this.thumbnail_url = in.readString();
        this.thumbnail_width = in.readInt();
        this.thumbnail_height = in.readInt();
        this.thumb_large_width = in.readInt();
        this.thumb_large_height = in.readInt();
        this.thumb_large_url = in.readString();
        this.image_url = in.readString();
        this.desc = in.readString();
    }

    public static final Creator<Model> CREATOR = new Creator<Model>() {
        public Model createFromParcel(Parcel source) {
            return new Model(source);
        }

        public Model[] newArray(int size) {
            return new Model[size];
        }
    };
}
