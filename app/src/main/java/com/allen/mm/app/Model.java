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
    public String thumbURL;
    public String hoverURL;
    public String objURL;//大图
    public String setTittle;//标题


    public Model() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.thumbURL);
        dest.writeString(this.hoverURL);
        dest.writeString(this.objURL);
        dest.writeString(this.setTittle);
    }

    private Model(Parcel in) {
        this.thumbURL = in.readString();
        this.hoverURL = in.readString();
        this.objURL = in.readString();
        this.setTittle = in.readString();
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
