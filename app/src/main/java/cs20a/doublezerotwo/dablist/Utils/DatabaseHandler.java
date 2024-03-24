package cs20a.doublezerotwo.dablist.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import cs20a.doublezerotwo.dablist.Model.taskModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String NAME = "tasksDB";
    private static final String TASK_TABLE = "todo";
    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";
    public static final String SUGGESTED_TASK_TABLE = "suggestedTasks";
    public static final String SUGGESTED_TASK = "suggestedTask";

    private static final String CREATE_SUGGESTED_TASK_TABLE = "CREATE TABLE " + SUGGESTED_TASK_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + SUGGESTED_TASK + " TEXT)";
    private static final String CREATE_TASK_TABLE = "CREATE TABLE " + TASK_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK + " TEXT, "
            + STATUS + " INTEGER)";

    private SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TASK_TABLE);
        db.execSQL(CREATE_SUGGESTED_TASK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SUGGESTED_TASK_TABLE);
        onCreate(db);
    }

    public void openDatabase(){
        db = this.getWritableDatabase();
    }

    public void insertTask(taskModel task) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(TASK, task.getTask());
            cv.put(STATUS, task.getStatus());
            db.insert(TASK_TABLE, null, cv);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("Range")
    public List<taskModel> getTasks(){
        List<taskModel> taskList = new ArrayList<>();
        db.beginTransaction();
        try (Cursor cur = db.query(TASK_TABLE, null, null, null, null, null, STATUS + " DESC")) {
            if (cur != null) {
                if (cur.moveToFirst()) {
                    do {
                        taskModel task = new taskModel();
                        task.setId(cur.getInt(cur.getColumnIndex(ID)));
                        task.setTask(cur.getString(cur.getColumnIndex(TASK)));
                        task.setStatus(cur.getInt(cur.getColumnIndex(STATUS)));
                        taskList.add(task);
                    }
                    while (cur.moveToNext());
                }
            }
        } finally {
            db.endTransaction();
        }
    return taskList;
    }

    public void updateStatus(int id, int status){
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TASK_TABLE, cv, ID + "=?", new String[] {String.valueOf(id)});
    }

    public void updateTask(int id, String task){
        ContentValues cv = new ContentValues();
        cv.put(TASK, task);
        db.update(TASK_TABLE, cv, ID + "=?", new String[] {String.valueOf(id)});
    }

    public void deleteTask(int id){
        db.delete(TASK_TABLE, ID + "=?", new String[] {String.valueOf(id)});
    }

    public void deleteChecked() {
        db.delete(TASK_TABLE, STATUS + "=?", new String[]{String.valueOf(1)});
    }

    public boolean noUnchecked() {
        Cursor cursor = null;
        boolean allUnchecked = true;
        db.beginTransaction();
        try {
            cursor = db.query(TASK_TABLE, null, STATUS + "=?", new String[]{String.valueOf(1)}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                allUnchecked = false;
            }
        } finally {
            db.endTransaction();
            if (cursor != null) {
                cursor.close();
            }
        }
        return allUnchecked;
    }


    public void deleteAll(){
        db.delete(TASK_TABLE, null, null);
    }

    public void insertSuggestedTask(String task) {

        Cursor cursor = db.query(SUGGESTED_TASK_TABLE, null, SUGGESTED_TASK + "=?", new String[]{task}, null, null, null);

        if (cursor != null && cursor.getCount() == 0) {

            ContentValues cv = new ContentValues();
            cv.put(SUGGESTED_TASK, task);
            db.insert(SUGGESTED_TASK_TABLE, null, cv);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    public List<String> getSuggestedTasks() {
        List<String> suggestedTasks = new ArrayList<>();
        db.beginTransaction();
        try (Cursor cursor = db.query(SUGGESTED_TASK_TABLE, new String[]{SUGGESTED_TASK}, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range")
                    String task = cursor.getString(cursor.getColumnIndex(SUGGESTED_TASK));
                    suggestedTasks.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            db.endTransaction();
        }
        return suggestedTasks;
    }

    public void deleteAllSuggestions() {
        db.delete(SUGGESTED_TASK_TABLE, null, null);
    }

    public boolean isTableEmpty() {
        boolean isEmpty;
        db.beginTransaction();
        try (Cursor cursor = db.rawQuery("SELECT 1 FROM " + SUGGESTED_TASK_TABLE + " LIMIT 1", null)) {
            isEmpty = (cursor == null || cursor.getCount() == 0);
        } finally {
            db.endTransaction();
        }
        return !isEmpty;
    }

        public void closeDB() {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

}
