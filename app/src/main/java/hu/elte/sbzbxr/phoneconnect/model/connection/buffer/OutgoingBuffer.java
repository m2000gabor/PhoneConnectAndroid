package hu.elte.sbzbxr.phoneconnect.model.connection.buffer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import hu.elte.sbzbxr.phoneconnect.model.SendableFile;
import hu.elte.sbzbxr.phoneconnect.model.connection.FileCutter;
import hu.elte.sbzbxr.phoneconnect.model.connection.Sendable;
import hu.elte.sbzbxr.phoneconnect.model.notification.SendableNotification;
import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot;

public class OutgoingBuffer {
    private final AtomicReference<Sendable> nextElement = new AtomicReference<>();
    private final LinkedBlockingQueue<ScreenShot> screenShotQueue = new LinkedBlockingQueue<>(10);
    private final LinkedBlockingQueue<SendableNotification> notificationQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<SendableFile> fileQueue = new LinkedBlockingQueue<>(10);

    public OutgoingBuffer(){}

    /**
     * Potentially blocking operation. Takes the first element from the buffer. If none is available, wait until it is.
     * @return first element
     */
    public Sendable take(){
        Sendable ret=null;
        try {
            synchronized (nextElement){
                while(nextElement.get()==null){nextElement.wait();}
                ret = nextElement.get();
                nextElement.set(null);
            }
            updateNextElement();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private void updateNextElement(){
        synchronized (nextElement){
            if(nextElement.get()!=null){return;}
            Sendable next=notificationQueue.poll();
            if(next==null){next=fileQueue.poll();}
            if(next==null){next=screenShotQueue.poll();}
            nextElement.set(next);
            nextElement.notify();
        }
    }

    public void forceInsert(SendableNotification notification){
        synchronized (notificationQueue){
            notificationQueue.add(notification);
            updateNextElement();
        }
    }

    public void forceInsert(ScreenShot screenShot){
        synchronized (screenShotQueue){
            if(!screenShotQueue.offer(screenShot)){
                onBufferIsFull(screenShotQueue,screenShot);
            }
        }
        updateNextElement();
    }

    private static void onBufferIsFull(LinkedBlockingQueue<ScreenShot> queue,ScreenShot toInsert){
        //BufferReducerAlgorithms.removeEvenIndices(queue,toInsert);
        BufferReducerAlgorithms.clearQueue(queue, toInsert);
    }



    public void forceInsert(FileCutter cutter) {
        synchronized (fileQueue){
            while (!cutter.isEnd()){
                try {
                    fileQueue.put(cutter.current());
                    cutter.next();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateNextElement();
            }
        }
    }
}