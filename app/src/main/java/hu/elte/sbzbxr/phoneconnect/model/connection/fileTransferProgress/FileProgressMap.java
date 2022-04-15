package hu.elte.sbzbxr.phoneconnect.model.connection.fileTransferProgress;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import hu.elte.sbzbxr.phoneconnect.ui.progress.FrameProgressInfo;

public class FileProgressMap {
    private final ArrayDeque<String> deque = new ArrayDeque<>();
    private final HashMap<String, FrameProgressInfo> map = new HashMap<>();


    public boolean containsKey(String key){
        return map.containsKey(key);
    }

    public void put(String key, FrameProgressInfo frameProgressInfo){
        map.put(key, frameProgressInfo);
        deque.remove(key);
        deque.addFirst(key);
    }

    public FrameProgressInfo get(String key) {
        return map.get(key);
    }

    public FrameProgressInfo remove(String key) {
        deque.remove(key);
        return map.remove(key);
    }

    public Queue<FrameProgressInfo> getActiveTransfers() {
        Queue<FrameProgressInfo> ret = new ArrayDeque<>();
        for(int i=0;i<deque.size();i++){
            ret.add(map.get(deque.getFirst()));
        }
        return ret;
    }
}
