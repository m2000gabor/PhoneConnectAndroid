package hu.elte.sbzbxr.phoneconnect.ui;

import java.util.Objects;

public class ConnectedFragmentUIData {
    private final String ip;
    private final String port;
    private final boolean isStreaming;
    private final boolean isNotification;
    private final boolean isDemo;
    private final boolean isLimited;


    public ConnectedFragmentUIData(String ip, String port, boolean isStreaming, boolean isNotification, boolean isDemo, boolean isLimited) {
        this.ip = ip;
        this.port = port;
        this.isStreaming = isStreaming;
        this.isNotification = isNotification;
        this.isDemo = isDemo;
        this.isLimited = isLimited;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public boolean isNotificationForwarded() {
        return isNotification;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public boolean isLimited() {
        return isLimited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectedFragmentUIData data = (ConnectedFragmentUIData) o;
        return isStreaming == data.isStreaming && isNotification == data.isNotification && isDemo == data.isDemo && isLimited == data.isLimited && Objects.equals(ip, data.ip) && Objects.equals(port, data.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, isStreaming, isNotification, isDemo, isLimited);
    }
}
