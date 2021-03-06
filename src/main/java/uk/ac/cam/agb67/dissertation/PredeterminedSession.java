package uk.ac.cam.agb67.dissertation;

import java.util.List;

public class PredeterminedSession extends Session {

    // Additional details for a Predetermined Session (PDS)
    public int PDS_Day;
    public int PDS_Start_Time;
    public int PDS_Room;

    // Constructors
    public PredeterminedSession(int id, String name, int len, List<Integer> kids) {
        super(id, name, len, kids);
    }
    public PredeterminedSession(int id, String name, int len, List<Integer> kids, int day, int start, int room) {
        super(id, name, len, kids);
        PDS_Day = day;
        PDS_Start_Time = start;
        PDS_Room = room;
    }

    @Override
    public String toString() {
        return "<Session #"+Session_ID+": Name:'"+Session_Name+"', Length:"+Session_Length+", Participants:"+Session_KeyInds.toString()+" " +
                "Day/Time:"+PDS_Day+"/"+PDS_Start_Time+" Room:"+PDS_Room+">";
    }
}
