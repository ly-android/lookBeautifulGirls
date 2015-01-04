package com.allen.mm.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 描述:
 *
 * @author: liyong on 2014/12/31
 */
public class Model implements Parcelable {
    //头像
    public String thumbnail_url;
    public String thumb_large_url;
    public String image_url;//大图
    public String desc;//标题


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.thumbnail_url);
        dest.writeString(this.thumb_large_url);
        dest.writeString(this.image_url);
        dest.writeString(this.desc);
    }

    public Model() {
    }

    private Model(Parcel in) {
        this.thumbnail_url = in.readString();
        this.thumb_large_url = in.readString();
        this.image_url = in.readString();
        this.desc = in.readString();
    }

    public static final Parcelable.Creator<Model> CREATOR = new Parcelable.Creator<Model>() {
        public Model createFromParcel(Parcel source) {
            return new Model(source);
        }

        public Model[] newArray(int size) {
            return new Model[size];
        }
    };
}
