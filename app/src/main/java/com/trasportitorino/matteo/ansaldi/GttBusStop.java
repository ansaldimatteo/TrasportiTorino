package com.trasportitorino.matteo.ansaldi;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * Created by Matteo on 12/04/2016.
 */
public class GttBusStop {

    private ArrayList<BusInfo> stopInfo;


    public void updateBusInfo(int stopNumber) throws Exception{
        String timetable;
        stopInfo = new ArrayList();

        String busNum;
        String time;
        String passaggio;
        String direction;


        //Get the html table containing the bus timetable
        timetable = getGttTable("http://gttweb.5t.torino.it/gtt/it/trasporto/arrivi-ricerca.jsp?shortName="+stopNumber+"&stoppingPointCtl:getTransits");

        //Parse the html table using JSoup
        Document doc = Jsoup.parse(timetable);

        Element table = doc.select("table").first();

        Iterator<Element> ite = table.select("td").iterator();

        //Place the info in an arraylist of BusInfo
        while(ite.hasNext() == true){
            busNum = ite.next().text();
            time = ite.next().text();
            passaggio = ite.next().text();
            direction = ite.next().text();
            stopInfo.add(new BusInfo(busNum, time, passaggio, direction));
        }




    }

    private String getGttTable(String url) throws IOException {

        boolean table = false;
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {

            // get redirect url from "location" header field
            String newUrl = con.getHeaderField("Location");

            // get the cookie for the stop's timetable
            String cookies = con.getHeaderField("Set-Cookie");

            // open the new connnection again
            con = (HttpURLConnection) new URL(newUrl).openConnection();
            con.setRequestProperty("Cookie", cookies);
            con.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            con.addRequestProperty("User-Agent", "Mozilla");
            con.addRequestProperty("Referer", "google.com");


        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer html = new StringBuffer();

        //Only output the html table with the necessary data
        while ((inputLine = in.readLine()) != null) {
            if(inputLine.contains("<table")){
                table = true;
            }

            if(inputLine.contains("</table>")){
                html.append(inputLine);
                table = false;
            }

            if(table == true){
                html.append(inputLine);
            }
        }
        in.close();

        return html.toString();
    }


    public ArrayList<BusInfo> getStopInfo() {
        return stopInfo;
    }
}
