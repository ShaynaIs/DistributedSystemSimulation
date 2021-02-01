import java.io.Serializable;

/**
 * This object is sent between the server and client
 * and between the server and slaves
 * to communicate
 */

public class Packet implements Serializable, Comparable<Packet> {
	private static final long serialVersionUID = 1L;
	private int id;
	private Status status;
	private Type type;
	private JobType job;
	
	// Constructors
	public Packet(int id, Status status, Type packetType, JobType jobType) {
		this.id = id;
		this.status = status;
		this.type = packetType;
		this.job = jobType;
	}
	
	// Getters
	public int getID() {
		return id;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public Type getType() {
		return type;
	}
	
	public JobType getJobType() {
		return job;
	}
	
	// Setters
	public void setStatus(Status status) {
		this.status = status;
	}

	public void setID(int id) {
		this.id = id;
	}
	
	// Methods
	public String toString() {
		return "ID = " + id + ", Status = " + status.toString() + ", Packet Type = " + type.toString() + ", Job Type = " + job.toString();
	}
	
	public boolean equals(Object o) {
		if(o.getClass() != getClass())
			return false;
		Packet p = (Packet) o;
		return p.getID() == getID();
	}
	
	public int compareTo(Packet p) {
		return ((Integer) getID()).compareTo((Integer) p.getID());
	}

}
