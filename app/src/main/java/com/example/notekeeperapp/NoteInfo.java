package com.example.notekeeperapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_info")
public final class NoteInfo implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;
    @ColumnInfo(name = "note_title")
    private String mTitle;
    @ColumnInfo(name = "note_text")
    private String mText;
    @ColumnInfo(name = "course_id")
    private long mCourseId;


    public NoteInfo(long id, long courseId, String title, String text) {
        mCourseId = courseId;
        mTitle = title;
        mText = text;
        mId = id;
    }

    @Ignore
    public NoteInfo(long courseId, String title, String text) {
        mCourseId = courseId;
        mTitle = title;
        mText = text;
    }

    private NoteInfo(Parcel parcel) {
        mCourseId = parcel.readLong();
        mTitle = parcel.readString();
        mText = parcel.readString();
    }

    public long getId() { return mId; }

    public void setId(long id) { mId = id; }

    public long getCourseId() { return mCourseId; }

    public void setCourseId(long courseId) {
        mCourseId = courseId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    private String getCompareKey() {
        return mCourseId + "|" + mTitle + "|" + mText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoteInfo that = (NoteInfo) o;

        return getCompareKey().equals(that.getCompareKey());
    }

    @Override
    public int hashCode() {
        return getCompareKey().hashCode();
    }

    @Override
    public String toString() {
        return getCompareKey();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mCourseId);
        parcel.writeString(mTitle);
        parcel.writeString(mText);
    }

    public static final Parcelable.Creator<NoteInfo> CREATOR =
            new Parcelable.Creator<NoteInfo>() {

                @Override
                public NoteInfo createFromParcel(Parcel parcel) {
                    return new NoteInfo(parcel);
                }

                @Override
                public NoteInfo[] newArray(int size) {
                    return new NoteInfo[size];
                }
            };
}
