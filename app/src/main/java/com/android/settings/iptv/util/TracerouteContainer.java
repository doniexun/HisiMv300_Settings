package com.android.settings.iptv.util;

/**
 * Created by Administrator on 2018/2/25 0025.
 */

public class TracerouteContainer {
    public String hostName;
    public String ip;
    public float time;

    public TracerouteContainer(String hostName , String ip , float time){
        this.hostName = hostName;
        this.ip = ip;
        this.time = time;
    }



    public String getIp(){
        return ip;
    }

    public void setHostName(String host){
        hostName = host;
    }

}
