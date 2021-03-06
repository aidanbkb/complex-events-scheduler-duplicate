package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

import java.util.List;

public class Coordinator implements SchedulingAlgorithm {

    private boolean GreedyVariant = false;
    private boolean Optimise = false;

    public Coordinator() {}
    public Coordinator(boolean greedy) { GreedyVariant = greedy; }
    public Coordinator(boolean greedy, boolean opt) {GreedyVariant = greedy; Optimise = opt; }

    @Override
    public Timetable generate(SchedulingProblem details) {
        String s = "";
        if (GreedyVariant) s="(Greedy variant)";
        if (Optimise) s += "(Optimised)";
        if (Main.DEBUG) System.out.println("\nAttempting to generate a schedule with algorithm one. "+s+"\n");

        // Input validation step
        if (!details.potentially_schedulable()) {
            System.err.println("The given scheduling problem details were invalid."); return null;
        }

        // Create an empty timetable
        Timetable schedule = new Timetable(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms);

        // Place the pre-determined sessions into the timetable
        for (PredeterminedSession pds : details.PDS_Details) {
            if (Main.DEBUG) System.out.println("Adding predetermined session #"+pds.Session_ID +" at day: "+pds.PDS_Day+", time: "+pds.PDS_Start_Time+", room: "+ pds.PDS_Room);
            schedule.set(pds.PDS_Day, pds.PDS_Start_Time, pds.PDS_Room, pds);
        }

        // Pass it into BruteForce solver, or the GreedyVersion solver
        if (!GreedyVariant) {
            BruteForce solver = new BruteForce(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms, details.KeyInd_Details,
                details.Room_Occupancy_Limits, details.Session_Details);
            schedule = solver.insert_sessions(schedule, details.Session_Details);
        } else {
            GreedyMethod solver = new GreedyMethod(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms, details.KeyInd_Details,
                    details.Room_Occupancy_Limits, details.Session_Details);
            schedule = solver.insert_sessions(schedule, details.Session_Details);
        }


        // Optional Naive Optimisation Step
        if (Optimise) {
            System.out.print(" - optimising - ");
            NaiveOptimiser optimiser = new NaiveOptimiser();
            Timetable optimisedSchedule = optimiser.optimisation_pass(schedule, details);
            schedule = optimisedSchedule;

        }


        // Check it with the TimetableVerifier
        TimetableVerifier verifier = new TimetableVerifier();
        boolean coherent = verifier.timetable_is_valid(schedule, details);
        if (!coherent) {
            System.err.println("Timetable was not valid after brute force "+s+" approach.");
            if (Main.DEBUG && schedule != null) System.out.println(schedule.toString());
            return null;
        }

        // Inform the user if this algorithm has failed
        if (schedule == null) {
            System.err.println("Algorithm one failed to generate a schedule."); return null;
        }

        // Return schedule
        if (Main.DEBUG) System.out.println("Algorithm one has generated a schedule.");
        if (Main.DEBUG) System.out.println(schedule.toString());
        return schedule;
    }

    // Returns true if the session, at the given time, doesn't clash with an existing session in the same room
    //  and doesn't result in a participating individual being in two sessions at once
    static boolean check_session_doesnt_clash(Timetable tt, int day, int hour, int room, Session sesh, List<Session> AllSessions) {

        // First make sure that there are no sessions in that room, for the duration of the session
        for (int offset=0; offset < sesh.Session_Length; offset++) {
            int sid = tt.get_id(day, hour+offset, room);
            if (sid != -1) return false;
        }

        // Then ensure that all participating individuals have no other sessions at the same time
        for (int keyID : sesh.Session_KeyInds) {

            // Iterate through all parallel rooms, for all hours of this session
            for (int offset=0; offset < sesh.Session_Length; offset++) {
                for (int r=0; r < tt.Total_Rooms; r++) {

                    // Find the session which is happening in this parallel room, and determine if our key individual is involved
                    int sid = tt.get_id(day,hour+offset,r);
                    if (sid == -1) continue;
                    Session s = AllSessions.get(sid);
                    if(s.Session_KeyInds.contains(keyID)) return false;
                }
            }

        }

        // If we have found no clashes, return true
        return true;
    }

}
