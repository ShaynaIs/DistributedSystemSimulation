import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * This thread is made has a 1:1 connection with each client It goes through the
 * "done jobs" list and checks if it belongs to this client If it does, it adds
 * it to the list for client done jobs So that it can be sent to the correct
 * client
 */

public class CollectJobsThread extends Thread {
	private ArrayList<Packet> doneJobs; // Complete list of all finished jobs
	private ArrayList<Integer> clientJobIds; // List of active job ids that this client sent
	private Queue<Packet> clientDoneJobs; // List of finished jobd for this client
	private String clientID; // Client ID
	private Object edittingDoneJobs; // Key to edit doneJobs
	private Object edittingClientJobIds; // Key to edit clientJobIds
	private Object edittingClientDoneJobs; // Key to edit clientDoneJobs

	// Constructor
	public CollectJobsThread(int id, ArrayList<Packet> doneJobs, Object edittingDoneJobs,
			ArrayList<Integer> clientJobIds, Object edittingClientJobIds, Queue<Packet> clientDoneJobs,
			Object edittingclientDoneJobs) {
		this.clientID = Integer.toString(id);
		this.doneJobs = doneJobs;
		this.clientJobIds = clientJobIds;
		this.clientDoneJobs = clientDoneJobs;
		this.edittingDoneJobs = edittingDoneJobs;
		this.edittingClientJobIds = edittingClientJobIds;
		this.edittingClientDoneJobs = edittingclientDoneJobs;
	}

	// Main method
	public void run() {
		Packet pack = null;
		while (true) {
			// Checks if done jobs and list of active client ids are not empty
			while (!doneJobs.isEmpty() && !clientJobIds.isEmpty()) {
				// Iterates through done jobs
				for (int i = 0; i < doneJobs.size(); i++) {
					try {
						// Checks if the jobs id is in the client active ids
						if (clientJobIds.contains(doneJobs.get(i).getID())) {
							// Retrieves & delete from done job
							synchronized (edittingDoneJobs) {
								pack = doneJobs.get(i);
								doneJobs.remove(i);
							}
							System.out.println("Packet #" + pack.getID() + " is in client " + clientID
									+ " job ID list and adding to their done list");
							// Change the id from the servers identifying id (ClientID + JobID)
							// To the id that the server uses
							int newID = Integer.parseInt(Integer.toString(pack.getID()).substring(clientID.length()));
							pack.setID(newID);
							// Add the job to the client done list
							synchronized (edittingClientDoneJobs) {
								clientDoneJobs.add(pack);
							}
							// Remove the id from the client active job list
							synchronized (edittingClientJobIds) {
								clientJobIds.remove(Integer.valueOf(newID));
							}
						}
					} catch (NullPointerException e) {
					} catch (IndexOutOfBoundsException e) {
					}
				}
			}
			// Wait to prevent possible indefinite postponement
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e1) {
				System.out.println("Error when looking at jobs for " + clientID);
				e1.printStackTrace();
			}
		}
	}
}
