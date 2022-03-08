package hu.elte.sbzbxr.phoneconnect.model.connection.buffer;

import java.util.concurrent.LinkedBlockingQueue;

import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot;

public class BufferReducerAlgorithms {
    public static void removeEvenIndices(LinkedBlockingQueue<ScreenShot> queue, ScreenShot toInsert){
        if(!queue.offer(toInsert)){
            ScreenShot tmp = queue.poll();
            int index = 0;
            int maxSize = queue.size();
            while(index<maxSize){
                if(tmp==null) {index++; continue;}
                if(index%2==0) queue.offer(tmp);
                index++;
                tmp = queue.poll();
            }
            queue.offer(toInsert);
            System.err.println("Bitmap queue overflown -> even indices removed");
        }
    }
    public static void clearQueue(LinkedBlockingQueue<ScreenShot> queue,ScreenShot toInsert){
        if(!queue.offer(toInsert)){
            queue.clear();
            queue.offer(toInsert);
            System.err.println("Bitmap queue overflown -> cleared");
        }
    }
}
