

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This is a thread that checks if packets need to be sent to a
 * client/slave/server and send them
 */

public class ObjectOutputThread extends Thread {
	private Socket socket; // This is the socket - used to check if still connected
	private String connectionType; // String for output and debugging
	private Queue<Packet> list; // List that needs to be sent
	private ObjectOutputStream out; // Location to be sent to
	private Object editingList; // Key to edit the list

	// Constructor
	public ObjectOutputThread(Socket socket, String connectionType, ObjectOutputStream outputStream, Queue<Packet> list,
			Object editingList) {
		this.socket = socket;
		this.connectionType = connectionType;
		this.list = list;
		out = outputStream;
		this.editingList = editingList;
	}

	@Override
	public void run() {
		System.out.println("New thread created to send to " + connectionType);
		Packet pack = null;
		// Continues until socket disconnects
		while (socket.isConnected()) {
			// Continues when there is a packet to be sent
			while (!list.isEmpty()) {
				// Remove first packet from list
				synchronized (editingList) {
					pack = list.poll();
				}
				// Send it
				try {
					out.writeObject(new Packet(pack.getID(), pack.getStatus(), pack.getType(), pack.getJobType()));
					System.out.println("Sent Job #" + pack.getID() + " to " + connectionType);
				} catch (IOException e) {
					System.out.println("Threw I/O Exception");
					e.printStackTrace();
				}
			}
			// Wait to prevent possible indefinite postponement
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e1) {
				System.out.println(connectionType + " Socket disconnected.");
				e1.printStackTrace();
			}
		}
		System.out.println(connectionType + " Socket disconnected.");
	}
}
