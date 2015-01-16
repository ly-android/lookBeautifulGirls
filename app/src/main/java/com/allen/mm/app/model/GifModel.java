package com.allen.mm.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/13
 */
public class GifModel implements Parcelable {
    public String text;
    public int width;
    public int height;
    public String image0;
    public String image1;
    public String image2;
    public int is_gif;
    public String gifFistFrame;


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
        dest.writeInt(this.is_gif);
        dest.writeString(this.gifFistFrame);
    }

    public GifModel() {
    }

    private GifModel(Parcel in) {
        this.text = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.image0 = in.readString();
        this.image1 = in.readString();
        this.image2 = in.readString();
        this.is_gif = in.readInt();
        this.gifFistFrame = in.readString();
    }

    public static final Parcelable.Creator<GifModel> CREATOR = new Parcelable.Creator<GifModel>() {
        public GifModel createFromParcel(Parcel source) {
            return new GifModel(source);
        }

        public GifModel[] newArray(int size) {
            return new GifModel[size];
        }
    };
}
