package com.dts.roadp;

import android.os.AsyncTask;
import android.os.Handler;

import java.util.ArrayList;

public class wsBase {

    public Runnable callBack=null;
    public String error="";
    public boolean errflag;
    public ArrayList<String> items=new ArrayList<String>();

    public String URL, NAMESPACE ="http://tempuri.org/";

    private boolean idle=true;

    public wsBase(String Url) {
        URL=Url;
    }

    public void execute(Runnable afterfinish) {
        if (idle) {
            errflag=false;error="";
            callBack=afterfinish;
            execute();
        }
    }

    public void pause() {
        idle=false;
    }

    public void resume() {
        idle=true;
    }

    protected void wsExecute() {
    }

    protected void wsFinished() {
        runCallBack();
    }

    private void execute() {
        try {
            AsyncCallwsBase wstask = new AsyncCallwsBase();
            wstask.execute();
        } catch (Exception e) {
            error=e.getMessage();errflag=true;
        }
    }

    private void runCallBack() {
        if (callBack==null) return;

        final Handler cbhandler = new Handler();
        cbhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callBack.run();
            }
        }, 50);
    }

    private class AsyncCallwsBase extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                wsExecute();
            } catch (Exception e) {
                error=e.getMessage();errflag=true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                wsFinished();
            } catch (Exception e) {}
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

    }

}
