package hu.elte.sbzbxr.phoneconnect.model.actions.networkstate;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;

public class Action_NetworkStateConnected extends NetworkAction implements NetworkStateAction{
    private final String ip;
    private final int port;

    public Action_NetworkStateConnected(String ip, int port) {
        super(ActionType.JUST_CONNECTED);
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public ActionType getType() {
        return type;
    }
}
