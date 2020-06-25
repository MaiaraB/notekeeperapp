package com.example.notekeeperapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "module_info")
public final class ModuleInfo implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int mId;
    @ColumnInfo(name = "module_id")
    private String mModuleId;
    @ColumnInfo(name = "module_title")
    private String mTitle;
    @ColumnInfo(name = "is_complete")
    private boolean mIsComplete = false;
    @ColumnInfo(name = "course_id")
    private long mCourseId;

    @Ignore
    public ModuleInfo(long courseId, String moduleId, String title) {
        this(courseId, moduleId, title, false);
    }

    public ModuleInfo(long courseId, String moduleId, String title, boolean isComplete) {
        mCourseId = courseId;
        mModuleId = moduleId;
        mTitle = title;
        mIsComplete = isComplete;
    }

    private ModuleInfo(Parcel source) {
        mModuleId = source.readString();
        mTitle = source.readString();
        mIsComplete = source.readByte() == 1;
    }

    public int getId() { return mId; }

    public void setId(int id) { mId = id; }

    public long getCourseId() { return mCourseId; }

    public void setCourseId(long courseId) { mCourseId = courseId; }

    public String getModuleId() {
        return mModuleId;
    }

    public void setModuleId(String moduleId) { mModuleId = moduleId; }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) { mTitle = title; }

    public boolean isComplete() {
        return mIsComplete;
    }

    public void setComplete(boolean complete) {
        mIsComplete = complete;
    }

    @Override
    public String toString() {
        return mTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleInfo that = (ModuleInfo) o;

        return mModuleId.equals(that.mModuleId);
    }

    @Override
    public int hashCode() {
        return mModuleId.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mModuleId);
        dest.writeString(mTitle);
        dest.writeByte((byte)(mIsComplete ? 1 : 0));
    }

    public static final Creator<ModuleInfo> CREATOR =
            new Creator<ModuleInfo>() {

                @Override
                public ModuleInfo createFromParcel(Parcel source) {
                    return new ModuleInfo(source);
                }

                @Override
                public ModuleInfo[] newArray(int size) {
                    return new ModuleInfo[size];
                }
            };

}
