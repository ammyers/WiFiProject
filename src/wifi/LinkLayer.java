package wifi;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

import rf.RF;

/**
 * Use this layer as a starting point for your project code.  See {@link Dot11Interface} for more
 * details on these routines.
 * @author richards
 */
public class LinkLayer implements Dot11Interface {
	private RF theRF;           // You'll need one of these eventually
	private short ourMAC;       // Our MAC address
	private PrintWriter output; // The output stream we'll write to
	Sender sender;
	Receiver receiver;

    private static final int QUEUE_SIZE = 4;

    private BlockingQueue<Packet> in = new ArrayBlockingQueue(QUEUE_SIZE);
    private BlockingQueue<Packet> out = new ArrayBlockingQueue(QUEUE_SIZE);

    // These Queues will facilitate communication between the LinkLayer and its
    // Sender and Receiver helper classes.
    public synchronized BlockingQueue<Packet> getIn() {
        return in;
    }
    public synchronized BlockingQueue<Packet> getOut() {
        return out;
    }

	/**
	 * Constructor takes a MAC address and the PrintWriter to which our output will
	 * be written.
	 * @param ourMAC  MAC address
	 * @param output  Output stream associated with GUI
	 */
	public LinkLayer(short ourMAC, PrintWriter output) {
		this.ourMAC = ourMAC;
		this.output = output;
		theRF = new RF(null, null);
		output.println("LinkLayer: Constructor ran.");

		sender = new Sender(ourMAC, theRF);
		receiver = new Receiver(theRF);
		Thread senderThread = new Thread(sender);
        Thread receiverThread = new Thread(receiver); // Threads them

        receiverThread.start(); // Starts the threads running
        senderThread.start();
	}

	/**
	 * Send method takes a destination, a buffer (array) of data, and the number
	 * of bytes to send.  See docs for full description.
	 */
	@Override
	public int send(short dest, byte[] data, int len) {
		output.println("LinkLayer: Sending "+len+" bytes to "+dest);

		Packet p = new Packet(0, (short) 0, dest, ourMAC, data);
        //Puts the created packet into the outgoing queue
        try {
            out.put(p);
        } catch (InterruptedException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }

		return len;
	}

	/**
	 * Recv method blocks until data arrives, then writes it an address info into
	 * the Transmission object.  See docs for full description.
	 */
	@Override
	public int recv(Transmission t) {
		output.println("LinkLayer: Pretending to block on recv()");
        Packet p;
        try {
            //Grabs the next packet from the incoming queue
            p = in.take();
            //Extracts the necessary parts from the packet and puts them into the supplied transmission object
            byte[] data = p.getData();
            t.setSourceAddr((short) p.getSenderAddr());
            t.setDestAddr((short) p.getDestAddr());
            t.setBuf(data);
            //Returns the length of the data received
            return data.length;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
	}

	/**
	 * Returns a current status code.  See docs for full description.
	 */
	@Override
	public int status() {
		output.println("LinkLayer: Faking a status() return value of 0");
		return 0;
	}

	/**
	 * Passes command info to your link layer.  See docs for full description.
	 */
	@Override
	public int command(int cmd, int val) {
		output.println("LinkLayer: Sending command "+cmd+" with value "+val);
		return 0;
	}
}
