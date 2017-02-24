package com.trasportitorino.matteo.ansaldi;

/**
 * Created by Matteo on 12/04/2016.
 */
public class BusInfo {

    private String busNum;
    private String time;
    private String passaggio;
    private String direction;


    public BusInfo(String busNum, String time, String passaggio, String direction) {
        super();
        this.busNum = busNum;
        this.time = time;
        this.passaggio = passaggio;
        this.direction = direction;
    }


    public String getBusNum() {
        return busNum;
    }


    public String getTime() {
        return time;
    }


    public String getPassaggio() {
        return passaggio;
    }


    public String getDirection() {
        return direction;
    }
}
