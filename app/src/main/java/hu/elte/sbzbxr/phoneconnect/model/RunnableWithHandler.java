package hu.elte.sbzbxr.phoneconnect.model;

import android.os.Handler;

import java.util.Objects;

public abstract class RunnableWithHandler implements Runnable {
    private Handler handler;
    public void setHandler(Handler onUiThread){handler=onUiThread;}
    public Handler getHandler(){return handler;}

    /**
     * Runs in the background
     */
    abstract void runMain();

    /**
     * Run on the ui thread when this Background task finished
     */
    abstract void onFinished();

    @Override
    public final void run() {
        if(Objects.isNull(getHandler())){throw new RuntimeException("Use at least once call addHandler()");}
        runMain();
        getHandler().post(this::onFinished);
    }
}
