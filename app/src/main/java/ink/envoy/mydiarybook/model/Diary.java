package ink.envoy.mydiarybook.model;

import android.content.Context;

public class Diary
{
    public Integer _id;
    public String title;
    public String content;
    public Long updatedAt;

    public Diary(String title, String content)
    {
        this.title = title;
        this.content = content;
    }

    public Diary(int _id, String title, String content)
    {
        this._id = _id;
        this.title = title;
        this.content = content;
    }

    public Diary(int _id, String title, String content, long updatedAt)
    {
        this._id = _id;
        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
    }
}