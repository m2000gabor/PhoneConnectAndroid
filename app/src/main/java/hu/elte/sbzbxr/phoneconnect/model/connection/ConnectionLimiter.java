package hu.elte.sbzbxr.phoneconnect.model.connection;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionLimiter {
    private final Timer timer;
    private final AtomicInteger bytesSentInThisSecond;
    private final AtomicInteger maxBytesPerSecond;

    private ConnectionLimiter(int maxBytesPerSecond) {
        timer = new Timer();
        bytesSentInThisSecond = new AtomicInteger(0);
        this.maxBytesPerSecond = new AtomicInteger(maxBytesPerSecond);
    }

    public static ConnectionLimiter create(int maxBytesPerSecond){
        return new ConnectionLimiter(maxBytesPerSecond);
    }

    /*
    public static ConnectionLimiter getInstance(){
        if(instance==null){instance=new ConnectionLimiter();}
        return instance;
    }*/

    public void start(){
        if(!hasLimit()) return;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                bytesSentInThisSecond.set(0);
                synchronized (this){
                    this.notifyAll();
                }

            }},0,1000);
    }

    public void stop(){
        timer.cancel();
        maxBytesPerSecond.set(-1);
        synchronized (this){
            notifyAll();
        }
    }

    public void send(byte b){
        if(!hasLimit()) return;
        try{
            int i = bytesSentInThisSecond.incrementAndGet();
            while (i>=maxBytesPerSecond.get() && maxBytesPerSecond.get()>0){
                synchronized (this) {
                    wait();
                }
                i = bytesSentInThisSecond.incrementAndGet();
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public static ConnectionLimiter noLimit(){
        return new ConnectionLimiter(-1);
    }

    public boolean hasLimit(){
        return maxBytesPerSecond.get()>0;
    }
}
