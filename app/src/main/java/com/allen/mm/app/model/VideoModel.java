package com.allen.mm.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/14
 */
public class VideoModel implements Parcelable {
    public String text;
    public int width;
    public int height;
    public String image0;
    public String image1;
    public String image2;
    public String videouri;
    public long playcount;
    public int videotime;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.image0);
        dest.writeString(this.image1);
        dest.writeString(this.image2);
        dest.writeString(this.videouri);
        dest.writeLong(this.playcount);
        dest.writeInt(this.videotime);
    }

    public VideoModel() {
    }

    private VideoModel(Parcel in) {
        this.text = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.image0 = in.readString();
        this.image1 = in.readString();
        this.image2 = in.readString();
        this.videouri = in.readString();
        this.playcount = in.readLong();
        this.videotime = in.readInt();
    }

    public static final Parcelable.Creator<VideoModel> CREATOR = new Parcelable.Creator<VideoModel>() {
        public VideoModel createFromParcel(Parcel source) {
            return new VideoModel(source);
        }

        public VideoModel[] newArray(int size) {
            return new VideoModel[size];
        }
    };
}
