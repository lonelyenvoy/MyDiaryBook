package ink.envoy.mydiarybook.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import ink.envoy.mydiarybook.model.Diary;
import ink.envoy.mydiarybook.model.DiarySaveStatus;

public class DiaryDataAccessor {

    private final Context applicationContext;

    public DiaryDataAccessor(Context context) {
        applicationContext = context;
    }

    private SQLiteDatabase getDatabase() {
        return new DiaryDatabaseHelper(applicationContext).getReadableDatabase();
    }

    public List<Diary> getAll() {
        SQLiteDatabase db = getDatabase();
        Cursor cursor = db.query(
                "diary",
                new String[] {"_id", "title", "content", "updatedAt"},
                "",
                null,
                null,
                null,
                null);
        List<Diary> results = new LinkedList<>();
        while (cursor.moveToNext()) {
            results.add(new Diary(
                    cursor.getInt(0), // id
                    cursor.getString(1), // title
                    cursor.getString(2), // content
                    cursor.getLong(3) // updatedAt
            ));
        }
        cursor.close();
        db.close();
        return results;
    }

    public Diary get(int _id) {
        List<Diary> diaries = execQueryingSql("SELECT * FROM diary WHERE _id=" + _id);
        if (diaries.isEmpty()) return null;
        return diaries.get(0);
    }

    public long post(Diary diary) {
        SQLiteDatabase db = getDatabase();
        long l = innerPost(db, diary);
        db.close();
        return l;
    }

    public void post(List<Diary> diaries) {
        SQLiteDatabase db = getDatabase();
        for (Diary diary : diaries) {
            innerPost(db, diary);
        }
        db.close();
    }

    protected long innerPost(SQLiteDatabase db, Diary diary) {
        ContentValues values = new ContentValues();
        values.put("title", diary.title);
        values.put("content", diary.content);
        values.put("updatedAt", diary.updatedAt);
        return db.insert("diary", null, values);
    }

    public void update(Diary diary) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();
        values.put("title", diary.title);
        values.put("content", diary.content);
        values.put("updatedAt", diary.updatedAt);
        db.update("diary", values, "_id=?", new String[] {diary._id+""});
        db.close();
    }

    public DiarySaveStatus put(Diary diary) {
        List<Diary> diaries = execQueryingSql("SELECT * from diary WHERE _id=" + diary._id);
        if (diaries.isEmpty()) {
            post(diary);
            return DiarySaveStatus.CREATED;
        } else {
            Diary oldDiary = diaries.get(0);
            if (oldDiary.title.equals(diary.title) && oldDiary.content.equals(diary.content)) {
                return DiarySaveStatus.NO_NEED_TO_SAVE;
            } else {
                update(diary);
                return DiarySaveStatus.UPDATED;
            }
        }
    }

    public void delete(long id) {
        SQLiteDatabase db = getDatabase();
        db.delete("diary", "_id=?", new String[] {id+""});
        db.close();
    }

    public void clear() {
        SQLiteDatabase db = getDatabase();
        db.execSQL("DELETE FROM diary");
        db.close();
    }

    /**
     * 执行非破坏性SQL语句
     * @param sql SQL语句
     * @return 查询到的结果
     * @throws SQLException
     */
    protected List<Diary> execQueryingSql(@NonNull String sql) throws SQLException {
        List<Diary> list = new LinkedList<>();
        SQLiteDatabase db = getDatabase();
        Cursor cursor = db.rawQuery(sql, new String[] {});
        while (cursor.moveToNext()) {
            list.add(new Diary(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getLong(4)
            ));
        }
        cursor.close();
        db.close();
        return list;
    }

}
