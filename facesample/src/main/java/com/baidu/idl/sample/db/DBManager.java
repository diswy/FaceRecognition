package com.baidu.idl.sample.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;


import com.baidu.idl.facesdk.model.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class DBManager {
    /**
     * The constant TAG
     */
    private static final String TAG = "DBManager";

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private static DBManager instance;
    private static SQLiteOpenHelper mDBHelper;
    private SQLiteDatabase mDatabase;
    private boolean allowTransaction = true;
    private Lock writeLock = new ReentrantLock();
    private volatile boolean writeLocked = false;

    /**
     * 获取DBManager 实例
     *
     * @return DBManager
     */
    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    public void release() {
        if (mDBHelper != null) {
            mDBHelper.close();
            mDBHelper = null;
        }
        instance = null;
    }

    public void init(Context context) {
        if (context == null) {
            return;
        }

        if (mDBHelper == null) {
            mDBHelper = new DBHelper(context.getApplicationContext());
        }
    }

    /**
     * 打开数据库
     * @return SQLiteDatabase
     */
    public synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            try {
                mDatabase = mDBHelper.getWritableDatabase();
            } catch (Exception e) {
                mDatabase = mDBHelper.getReadableDatabase();
            }
        }
        return mDatabase;
    }

    /**
     * 关闭数据库
     */
    public synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();
        }
    }

    public boolean addFeature(Feature feature) {
        if (mDBHelper == null) {
            return false;
        }

        try {
            mDatabase = mDBHelper.getWritableDatabase();
            beginTransaction(mDatabase);

            ContentValues cv = new ContentValues();
            cv.put("face_token", feature.getFaceToken());
            cv.put("group_id", feature.getGroupId());
            cv.put("user_id", feature.getUserId());
            cv.put("feature", feature.getFeature());
            cv.put("image_name", feature.getImageName());
            cv.put("user_name", feature.getUserName());
            cv.put("crop_name", feature.getCropImageName());
            cv.put("ctime", System.currentTimeMillis());
            cv.put("update_time", System.currentTimeMillis());

            long id = mDatabase.insert(DBHelper.TABLE_FEATURE, null, cv);

            if (id < 0) {
                return false;
            }
            setTransactionSuccessful(mDatabase);
        } finally {
            endTransaction(mDatabase);
        }
        return true;
    }

    public List<Feature> queryFeature() {
        ArrayList<Feature> featureList = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (mDBHelper == null) {
                return featureList;
            }
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            String where = "group_id = ? ";
            String[] whereValue = {"0"};
            cursor = db.query(DBHelper.TABLE_FEATURE, null, where, whereValue, null, null, null);
            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                int dbId = cursor.getInt(cursor.getColumnIndex("_id"));
                String faceToken = cursor.getString(cursor.getColumnIndex("face_token"));
                byte[] featureContent = cursor.getBlob(cursor.getColumnIndex("feature"));
                String userId = cursor.getString(cursor.getColumnIndex("user_id"));
                long updateTime = cursor.getLong(cursor.getColumnIndex("update_time"));
                long ctime = cursor.getLong(cursor.getColumnIndex("ctime"));
                String imageName = cursor.getString(cursor.getColumnIndex("image_name"));
                String userName = cursor.getString(cursor.getColumnIndex("user_name"));
                String cropName = cursor.getString(cursor.getColumnIndex("crop_name"));

                Feature feature = new Feature();
                feature.setId(dbId);
                feature.setFaceToken(faceToken);
                feature.setFeature(featureContent);
                feature.setCtime(ctime);
                feature.setUpdateTime(updateTime);
                feature.setGroupId("0");
                feature.setUserId(userId);
                feature.setImageName(imageName);
                feature.setUserName(userName);
                feature.setCropImageName(cropName);
                featureList.add(feature);
            }
        } finally {
            closeCursor(cursor);
        }
        return featureList;
    }

    public List<Feature> queryFeatureByName(String userName) {
        ArrayList<Feature> featureList = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (mDBHelper == null) {
                return featureList;
            }
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            String where = "user_name = ? ";
            String[] whereValue = {userName};
            cursor = db.query(DBHelper.TABLE_FEATURE, null, where, whereValue, null, null, null);
            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                int dbId = cursor.getInt(cursor.getColumnIndex("_id"));
                String groupId = cursor.getString(cursor.getColumnIndex("group_id"));
                String faceToken = cursor.getString(cursor.getColumnIndex("face_token"));
                byte[] featureContent = cursor.getBlob(cursor.getColumnIndex("feature"));
                String userId = cursor.getString(cursor.getColumnIndex("user_id"));
                long updateTime = cursor.getLong(cursor.getColumnIndex("update_time"));
                long ctime = cursor.getLong(cursor.getColumnIndex("ctime"));
                String imageName = cursor.getString(cursor.getColumnIndex("image_name"));
                String cropName = cursor.getString(cursor.getColumnIndex("crop_name"));

                Feature feature = new Feature();
                feature.setId(dbId);
                feature.setGroupId(groupId);
                feature.setFaceToken(faceToken);
                feature.setFeature(featureContent);
                feature.setCtime(ctime);
                feature.setUpdateTime(updateTime);
                feature.setGroupId("0");
                feature.setUserId(userId);
                feature.setImageName(imageName);
                feature.setUserName(userName);
                feature.setCropImageName(cropName);
                featureList.add(feature);
            }
        } finally {
            closeCursor(cursor);
        }
        return featureList;
    }

    public List<Feature> queryFeatureById(int _id) {
        ArrayList<Feature> featureList = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (mDBHelper == null) {
                return featureList;
            }
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            String where = "_id = ? ";
            String[] whereValue = {String.valueOf(_id)};
            cursor = db.query(DBHelper.TABLE_FEATURE, null, where, whereValue, null, null, null);
            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                String groupId = cursor.getString(cursor.getColumnIndex("group_id"));
                String faceToken = cursor.getString(cursor.getColumnIndex("face_token"));
                byte[] featureContent = cursor.getBlob(cursor.getColumnIndex("feature"));
                String userId = cursor.getString(cursor.getColumnIndex("user_id"));
                long updateTime = cursor.getLong(cursor.getColumnIndex("update_time"));
                long ctime = cursor.getLong(cursor.getColumnIndex("ctime"));
                String userName = cursor.getString(cursor.getColumnIndex("user_name"));
                String imageName = cursor.getString(cursor.getColumnIndex("image_name"));
                String cropName = cursor.getString(cursor.getColumnIndex("crop_name"));

                Feature feature = new Feature();
                feature.setId(_id);
                feature.setGroupId(groupId);
                feature.setFaceToken(faceToken);
                feature.setFeature(featureContent);
                feature.setCtime(ctime);
                feature.setUpdateTime(updateTime);
                feature.setGroupId("0");
                feature.setUserId(userId);
                feature.setImageName(imageName);
                feature.setUserName(userName);
                feature.setCropImageName(cropName);
                featureList.add(feature);
            }
        } finally {
            closeCursor(cursor);
        }
        return featureList;
    }

    public byte[] queryFeature(String faceToken) {
        byte[] feature = null;
        Cursor cursor = null;

        try {
            if (mDBHelper == null) {
                return feature;
            }
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            String where = "face_token = ? ";
            String[] whereValue = {faceToken};
            cursor = db.query(DBHelper.TABLE_FEATURE, null, where, whereValue, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                int dbId = cursor.getInt(cursor.getColumnIndex("_id"));
                feature = cursor.getBlob(cursor.getColumnIndex("feature"));

            }
        } finally {
            closeCursor(cursor);
        }
        return feature;
    }

    public boolean deleteFeature(String userId, String groupId, String faceToken) {
        boolean success = false;
        try {
            mDatabase = mDBHelper.getWritableDatabase();
            beginTransaction(mDatabase);

            if (!TextUtils.isEmpty(userId)) {
                String where = "user_id = ? and group_id = ? and face_token = ? ";
                String[] whereValue = {userId, groupId, faceToken};

                if (mDatabase.delete(DBHelper.TABLE_FEATURE, where, whereValue) < 0) {
                    return false;
                }
                setTransactionSuccessful(mDatabase);
                success = true;
            }

        } finally {
            endTransaction(mDatabase);
        }
        return success;
    }

    private void beginTransaction(SQLiteDatabase mDatabase) {
        if (allowTransaction) {
            mDatabase.beginTransaction();
        } else {
            writeLock.lock();
            writeLocked = true;
        }
    }

    private void setTransactionSuccessful(SQLiteDatabase mDatabase) {
        if (allowTransaction) {
            mDatabase.setTransactionSuccessful();
        }
    }

    private void endTransaction(SQLiteDatabase mDatabase) {
        if (allowTransaction) {
            mDatabase.endTransaction();
        }
        if (writeLocked) {
            writeLock.unlock();
            writeLocked = false;
        }
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable e) {
            }
        }
    }
}