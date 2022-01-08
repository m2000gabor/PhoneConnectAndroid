package hu.elte.sbzbxr.phoneconnect.model;

public class ConnectionManager {

    public ConnectionManager() {
    }

    public boolean connect(String ip, int port){
        if(ip.equals("") || port==-1){return false;}
        return true;
    }

    public boolean ping(){
        return false;
    }

    public boolean startStreaming(){
        return false;
    }
}
