package com.example.notekeeperapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "course_info")
public final class CourseInfo implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;
    @ColumnInfo(name = "course_id")
    private String mCourseId;
    @ColumnInfo(name = "course_title")
    private String mTitle;

    public CourseInfo(long id, String courseId, String title) {
        this.mId = id;
        this.mCourseId = courseId;
        this.mTitle = title;
    }

    @Ignore
    public CourseInfo(String courseId, String title) {
        this.mCourseId = courseId;
        this.mTitle = title;
    }

    private CourseInfo(Parcel source) {
        mCourseId = source.readString();
        mTitle = source.readString();
    }

    public long getId() { return mId; }

    public void setId(int id) { mId = id; }

    public String getCourseId() {
        return mCourseId;
    }

    void setCourseId(String courseId) { mCourseId = courseId; }

    public String getTitle() { return mTitle; }

    public void setTitle(String title) { mTitle = title; }

    @Override
    public String toString() {
        return mTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseInfo that = (CourseInfo) o;

        return mCourseId.equals(that.mCourseId);

    }

    @Override
    public int hashCode() {
        return mCourseId.hashCode();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCourseId);
        dest.writeString(mTitle);
    }

    public static final Creator<CourseInfo> CREATOR =
            new Creator<CourseInfo>() {

                @Override
                public CourseInfo createFromParcel(Parcel source) {
                    return new CourseInfo(source);
                }

                @Override
                public CourseInfo[] newArray(int size) {
                    return new CourseInfo[size];
                }
            };

}
