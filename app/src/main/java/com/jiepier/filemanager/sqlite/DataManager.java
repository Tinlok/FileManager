package com.jiepier.filemanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by panruijie on 17/1/4.
 * Email : zquprj@gmail.com
 */

public class DataManager implements CRUD{

    private static DataManager sInstance;
    public static final String DOC = "doc";
    public static final String ZIP = "zip";
    public static final String APK = "apk";
    private DBOpenHelper mDocHelper;
    private DBOpenHelper mZipHelper;
    private DBOpenHelper mApkHelper;
    private SQLiteDatabase mDb;

    public static void init(Context context,int version){

        if (sInstance == null){
            synchronized (DataManager.class){
                if (sInstance == null)
                    sInstance = new DataManager(context,version);
            }
        }
    }

    private DataManager(Context context,int version){

        mDocHelper = new DBOpenHelper(context,"doc.db",null,version);
        mZipHelper = new DBOpenHelper(context,"zip.db",null,version);
        mApkHelper = new DBOpenHelper(context,"apk.db",null,version);
    }

    public static DataManager getInstance(){

        if (sInstance == null){
            throw new IllegalStateException("You must be init DataManager first");
        }
        return sInstance;
    }


    @Override
    public boolean insertSQL(String type, String path) {

        mDb = getSQLite(type);
        ContentValues newValues = new ContentValues();
        newValues.put("path",path);

        return mDb.insert(type,null,newValues)>0;

    }

    @Override
    public Observable<Boolean> insertSQLUsingObservable(String type, String path) {

        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            subscriber.onNext(insertSQL(type,path));
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public boolean updateSQL(String type, List<String> list) {

        mDb = getSQLite(type);
        mDb.delete(type,null,null);

        for (int i= 0; i<list.size() ;i++)
            insertSQL(type,list.get(i));

        return true;
    }

    @Override
    public Observable<Boolean> updateSQLUsingObservable(String type, List<String> list) {

        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            subscriber.onNext(updateSQL(type,list));
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public boolean deleteSQL(String type, String path) {

        mDb = getSQLite(type);
        return mDb.delete(type,"path=?",new String[]{path}) > 0;
    }

    @Override
    public Observable<Boolean> deleteSQLUsingObservable(String type, String path) {

        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            subscriber.onNext(deleteSQL(type,path));
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public boolean updateSQL(String type, String orignalPath, String path) {

        mDb = getSQLite(type);
        ContentValues newValues = new ContentValues();
        newValues.put("path",path);

        return mDb.update(type,newValues,"path=?",new String[]{orignalPath}) > 0;
    }

    @Override
    public Observable<Boolean> updateSQLUsingObservable(String type,String orignalPath, String path) {

        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            subscriber.onNext(updateSQL(type,orignalPath,path));
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public SQLiteDatabase getSQLite(String type) {
        switch (type){
            case DOC:
                return mDocHelper.getWritableDatabase();
            case ZIP:
                return mZipHelper.getWritableDatabase();
            case APK:
                return mApkHelper.getWritableDatabase();
            default:
                return null;
        }
    }

    @Override
    public Observable<ArrayList<String>> selectUsingObservable(String type) {

        return Observable.create(new Observable.OnSubscribe<ArrayList<String>>(){

            @Override
            public void call(Subscriber<? super ArrayList<String>> subscriber) {
                subscriber.onNext(select(type));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public ArrayList<String> select(String type) {

        mDb = getSQLite(type);
        Cursor cursor = mDb.rawQuery("select path from "+type,null);

        ArrayList<String> list = new ArrayList<>();
        if (cursor.moveToFirst()){
            do {
                String path = cursor.getString(cursor.getColumnIndex("path"));
                list.add(path);
            }while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
}
