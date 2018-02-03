package com.android.yzd.iceolate.dummy;

import android.os.Parcel;
import android.os.Parcelable;

public class DummyItem implements Parcelable {
        public final String id;
        public final float content;

        public DummyItem(String id, float content) {
            this.id = id;
            this.content = content;
        }

        protected DummyItem(Parcel in) {
            id = in.readString();
            content = in.readFloat();
        }

        public static final Creator<DummyItem> CREATOR = new Creator<DummyItem>() {
            @Override
            public DummyItem createFromParcel(Parcel in) {
                return new DummyItem(in);
            }

            @Override
            public DummyItem[] newArray(int size) {
                return new DummyItem[size];
            }
        };

        @Override
        public String toString() {
            return content + "";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeFloat(content);
        }
    }