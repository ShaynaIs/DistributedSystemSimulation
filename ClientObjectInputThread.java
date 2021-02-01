

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;

/**
 * This thread listens to client input stream and add to list
 * 
 * Note: Similar to ObjectInputStream but implements added functionality that we
 * couldn't put them together
 */

public class ClientObjectInputThread extends Thread {
	private int id;
	private Socket socket;
	private ArrayList<Integer> clientList;
	private Queue<Packet> activeList;
	private ObjectInputStream in;
	private Object edittingClientList;
	private Object edittingActiveList;

	public ClientObjectInputThread(int id, Socket socket, ObjectInputStream s, ArrayList<Integer> clientList,
			Object edittingClientList, Queue<Packet> activeList, Object edittingActiveList) {
		this.id = id;
		this.socket = socket;
		this.clientList = clientList;
		this.activeList = activeList;
		in = s;
		this.edittingClientList = edittingClientList;
		this.edittingActiveList = edittingActiveList;
	}

	@Override
	public void run() {
		System.out.println("New thread created to listen to client " + id);
		Packet pack;
		try {
			// Continues while socket is connected
			while (socket.isConnected()) {
				if ((pack = (Packet) in.readObject()) != null) {
					// change id to start with client connection id
					int newID = Integer.parseInt(Integer.toString(id) + Integer.toString(pack.getID()));
					pack.setID(newID);
					// Add to active list
					synchronized (edittingActiveList) {
						activeList.offer(pack);
					}
					// Add to clientList
					synchronized (edittingClientList) {
						clientList.add(pack.getID());
					}
					System.out.println("Recieved Packet(" + pack.toString() + ") from client " + id);
				}
			}
			// Close everything
			System.out.println("Socket disonnected");
			in.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exception when connecting to client " + id);
			e.printStackTrace();
		}

	}

}
