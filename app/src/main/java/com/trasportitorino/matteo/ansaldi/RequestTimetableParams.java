package com.trasportitorino.matteo.ansaldi;

/**
 * Created by Matteo on 13/04/2016.
 */
public class RequestTimetableParams {
    private String stopNumRead;
    private GttBusStop busStop;


    public GttBusStop getBusStop() {
        return busStop;
    }

    public String getStopNumRead() {
        return stopNumRead;
    }

    public RequestTimetableParams(String stopNumRead, GttBusStop busStop){

        this.busStop = busStop;
        this.stopNumRead = stopNumRead;

    }

}
