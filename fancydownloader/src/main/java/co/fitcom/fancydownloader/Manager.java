/*
 * Created By Osei Fortune on 15/17/17 3:30 AM
 * Copyright (c) 2017 - 2018
 * Last modified 2/28/18 11:40 AM
 */

package co.fitcom.fancydownloader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;



public class Manager {
    public static final String DB_NAME = "fancy_downloader";
    public static final String DB_PROP_NAME = "task";
    public static final String DB_PROP_CALL = "call";
    public static final String DB_PROP_BUILDER = "builder";
    public static final String DB_PROP_REQUEST = "request";
    private static Manager mManager;
    private static ManagerService mService;
    private ServiceConnection mConnection;
    private Manager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("The provided context must not be null!");
        }
        this.createConnection();
        Intent intent = new Intent(context, ManagerService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public static void init(Context context) {
        if (mManager == null) {
            mManager = new Manager(context.getApplicationContext());
        }
    }

    public static Manager getInstance() {
        if (mManager == null) {
            throw new IllegalStateException("Manager Service is not initiated please call Manager.init(context)");
        }
        return mManager;
    }

    public static void setTimeout(long timeout){
        ManagerService.setTimeout(timeout);
    }

    public void cleanUp() {
        mService.cleanUp();
    }

    public String create(Request request) {
        return mService.create(request);
    }

    public void start(String id) {
        mService.start(id);
    }

    public void cancel(String id) {
        mService.cancel(id);
    }

    public void cancelAll() {
        mService.cancelAll();
    }

    public void resume(String id) {
        mService.resume(id);
    }

    public void pause(String id) {
        mService.pause(id);
    }

    public void pauseAll() {
        mService.pauseAll();
    }

    private void createConnection() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ManagerService.ManagerBinder binder = (ManagerService.ManagerBinder) service;
                mService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };
    }

}
