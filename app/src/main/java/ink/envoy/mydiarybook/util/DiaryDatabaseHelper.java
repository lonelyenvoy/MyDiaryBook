package ink.envoy.mydiarybook.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DiaryDatabaseHelper extends SQLiteOpenHelper {

    public DiaryDatabaseHelper(Context context) {
        super(context, "diary.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE diary(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, createdAt INTEGER, updatedAt INTEGER)"
        );
        long time = System.currentTimeMillis();
        sqLiteDatabase.execSQL(
                "INSERT INTO diary(title, content, createdAt, updatedAt) values ('欢迎使用日记本', '用最简单的方式，抓住一闪而过的灵感，写下至关重要的句子，保存此时此刻的心情', ?, ?)",
                new Object[] { time, time });
        //
        sqLiteDatabase.execSQL(
                "INSERT INTO diary(title, content, createdAt, updatedAt) values ('Test', 'The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog ', ?, ?)",
                new Object[] { time, time });
        sqLiteDatabase.execSQL(
                "INSERT INTO diary(title, content, createdAt, updatedAt) values ('这是标题1', '这是内容1', ?, ?)",
                new Object[] { time, time });
        sqLiteDatabase.execSQL(
                "INSERT INTO diary(title, content, createdAt, updatedAt) values ('这是标题2', '这是内容2', ?, ?)",
                new Object[] { time, time });
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}
