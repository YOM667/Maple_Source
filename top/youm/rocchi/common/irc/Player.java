package top.youm.rocchi.common.irc;

import top.youm.rocchi.Rocchi;

import java.io.IOException;
import java.io.Serializable;

public class Player implements Serializable , ClientPlayer{
    private String name;
    private String uuid;

    public Player() {
    }

    public Player(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public void sendMessage(String receiver, String message) {
        try {
            Rocchi.getInstance().getClient().send(receiver,message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}