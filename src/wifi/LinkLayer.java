package wifi;
import java.io.PrintWriter;

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
	Thread senderThread;
	Thread receiverThread;
	Sender sender;
	Receiver receiver;

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
		senderThread = new Thread(sender);

	}

	/**
	 * Send method takes a destination, a buffer (array) of data, and the number
	 * of bytes to send.  See docs for full description.
	 */
	@Override
	public int send(short dest, byte[] data, int len) {
		output.println("LinkLayer: Sending "+len+" bytes to "+dest);

		Packet p = new Packet(0, (short) 0, dest, ourMAC, data);


		theRF.transmit(data);
		return len;
	}

	/**
	 * Recv method blocks until data arrives, then writes it an address info into
	 * the Transmission object.  See docs for full description.
	 */
	@Override
	public int recv(Transmission t) {
		output.println("LinkLayer: Pretending to block on recv()");
		while(true); // <--- This is a REALLY bad way to wait.  Sleep a little each time through.
		//return 0;
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
