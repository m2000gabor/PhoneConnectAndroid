package hu.elte.sbzbxr.phoneconnect.model.connection.buffer;

import androidx.annotation.NonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import hu.elte.sbzbxr.phoneconnect.model.connection.ScreenShotFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NetworkFrame;

public class OutgoingBuffer {
    private final ConcurrentHashMap<BufferPriority, BlockingQueue<NetworkFrame>> map;

    public void clear() {
        map.forEach((prio,queue)->queue.clear());
    }

    public void removeOutgoingFileFrames() {
        map.remove(getPriority(FrameType.FILE));
    }

    private enum BufferPriority{
        INSTANT(1),
        IMPORTANT(2),
        DEFAULT(3),
        FILE(4),
        SEGMENT(5);

        public final byte v;

        BufferPriority(int val){this.v=(byte)val;}
    }

    public OutgoingBuffer(){
        map = new ConcurrentHashMap<>();
        for(BufferPriority priority : BufferPriority.values()){
            if(getMaxSize(priority) != Integer.MAX_VALUE){
                map.put(priority,new ArrayBlockingQueue<>(getMaxSize(priority)));
            }else{
                map.put(priority,new LinkedBlockingQueue<>());
            }
        }
    }

    /**
     * Potentially blocking operation. Takes the most important element from the buffer. If none is available, wait until it is.
     * @return first element
     */
    public NetworkFrame take(){
        NetworkFrame ret=null;
        while (ret==null){
            for(BufferPriority p : BufferPriority.values()){
                BlockingQueue<NetworkFrame> queue = map.get(p);
                if(queue==null) continue;
                ret = queue.poll();
                if(ret !=null) break;
            }
        }
        if(ret.type == FrameType.SEGMENT){((ScreenShotFrame)ret).getScreenShot().addTimestamp("takenFromBuffer",System.currentTimeMillis());}
        return ret;
    }


    public void put(NetworkFrame frame) throws InterruptedException {
        BufferPriority priority = getPriority(frame.type);
        if(frame.type== FrameType.SEGMENT){((ScreenShotFrame)frame).getScreenShot().addTimestamp("beforeInsert",System.currentTimeMillis());}
        if(priority==BufferPriority.SEGMENT){
            if(!map.get(priority).offer(frame)){
                onBufferIsFull(map.get(priority),frame);
            }
        }else{
            map.get(priority).put(frame);
        }
        if(frame.type== FrameType.SEGMENT){((ScreenShotFrame)frame).getScreenShot().addTimestamp("inserted",System.currentTimeMillis());}
    }

    @NonNull
    private BufferPriority getPriority(FrameType type) {
        BufferPriority priority;
        switch (type){
            default:priority=BufferPriority.DEFAULT;break;
            case INTERNAL_MESSAGE:priority=BufferPriority.INSTANT;break;
            case NOTIFICATION: priority=BufferPriority.IMPORTANT;break;
            case SEGMENT:priority=BufferPriority.SEGMENT; break;
            case FILE: priority=BufferPriority.FILE;break;
        }
        return priority;
    }

    private static int getMaxSize(BufferPriority type){
        switch (type){
            default: return Integer.MAX_VALUE;
            case SEGMENT: return 5;
            case FILE: return 10;
        }
    }


    private static void onBufferIsFull(BlockingQueue<NetworkFrame> queue,NetworkFrame toInsert){
        //BufferReducerAlgorithms.removeEvenIndices(queue,toInsert);
        BufferReducerAlgorithms.clearQueue(queue, toInsert);
    }
}
