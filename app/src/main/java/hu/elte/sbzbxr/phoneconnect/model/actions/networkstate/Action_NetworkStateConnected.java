package hu.elte.sbzbxr.phoneconnect.model.actions.networkstate;

import java.util.Objects;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;

public final class Action_NetworkStateConnected extends NetworkAction implements NetworkStateAction{
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action_NetworkStateConnected that = (Action_NetworkStateConnected) o;
        return port == that.port && Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
