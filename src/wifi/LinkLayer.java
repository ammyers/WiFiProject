package wifi;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
	protected short ourMAC;       // Our MAC address
	protected PrintWriter output; // The output stream we'll write to
	Sender sender;
	Receiver receiver;

    protected HashMap<Short, ArrayList<Short>> receivedACKS = new HashMap();
    protected HashMap<Short,Short> sendHash = new HashMap();
    protected HashMap<Short,Short> recvHash = new HashMap();

    // Random guess at size
    private static final int QUEUE_SIZE = 10;

    private BlockingQueue<Packet> incomingBlock = new ArrayBlockingQueue(QUEUE_SIZE);
    private BlockingQueue<Packet> outgoingBlock = new ArrayBlockingQueue(QUEUE_SIZE);

    // These Queues will help with plumbing between
    // the LinkLayer, Sender, and Receiver classes.
    public synchronized BlockingQueue<Packet> getIncomingBlock() {
        return incomingBlock;
    }
    public synchronized BlockingQueue<Packet> getOutgoingBlock() {
        return outgoingBlock;
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

        // Creating and Starting threads
		sender = new Sender(theRF, this);
		receiver = new Receiver(theRF, this);
		Thread senderThread = new Thread(sender);
        Thread receiverThread = new Thread(receiver);

        receiverThread.start();
        senderThread.start();
	}

    public short nextSeqNum(short seqNum) {
        short nextSeq;
        if(sendHash.containsKey(seqNum)) {
            nextSeq = (short) (sendHash.get(seqNum) + 1);
        }
        else{
            nextSeq = 0;
        }
        this.sendHash.put(seqNum, nextSeq);
        return nextSeq;
    }


    public short gotSeqNum(short seqNum) {
        short nextSeq;
        if(sendHash.containsKey(seqNum)) {
            nextSeq = (short) (sendHash.get(seqNum) + 1);
        }
        else{
            nextSeq = 0;
        }
        this.recvHash.put(seqNum, nextSeq);
        return nextSeq;
    }

	/**
	 * Send method takes a destination, a buffer (array) of data, and the number
	 * of bytes to send.  See docs for full description.
	 */
	@Override
	public int send(short dest, byte[] data, int len) {
		output.println("LinkLayer: Sending " + len + " bytes to " + dest);

        short seqNum = nextSeqNum(dest);

		Packet p;
        p = new Packet(0, seqNum, dest, ourMAC, data);

        // Puts the created packet into the outgoing queue
        try {
            outgoingBlock.put(p);
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
        // should add check for t being null

		output.println("LinkLayer: Pretending to block on recv()");
        Packet p;
        try {
            // Grabs the next packet from the incoming queue
            p = incomingBlock.take();
            if(p.getSeqNum() < recvHash.get(p.getSenderAddr())) {
                output.println("Already got this");
            }
            else {
                // Extracts the necessary pieces and puts them into the transmission object
                byte[] data = p.getData();
                t.setSourceAddr(p.getSenderAddr());
                t.setDestAddr(p.getDestAddr());
                t.setBuf(data);
                // Length of the data received
                return data.length;
            }
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
