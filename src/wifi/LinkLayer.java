package wifi;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import rf.RF;

/**
 * See {@link Dot11Interface} for more details on these routines.
 *
 * @author richards
 */
public class LinkLayer implements Dot11Interface {
	private RF theRF;           // You'll need one of these eventually
	protected short ourMAC;       // Our MAC address
	protected PrintWriter output; // The output stream we'll write to
	Sender sender;
	Receiver receiver;

    protected int currentStatus;
    public final int SUCCESS = 1;
    public final int UNSPECIFIED_ERROR = 2;
    public final int RF_INIT_FAILED = 3;
    public final int TX_DELIVERED = 4;
    public final int TX_FAILED = 5;
    public final int BAD_BUF_SIZE = 6;
    public final int BAD_ADDRESS = 7;
    public final int BAD_MAC_ADDRESS = 8;
    public final int ILLEGAL_ARGUMENT = 9;
    public final int INSUFFICIENT_BUFFER_SPACE = 10;

    protected HashMap<Short, ArrayList<Short>> receivedACKS = new HashMap();
    protected HashMap<Short,Short> sendHash = new HashMap();
    protected HashMap<Short,Short> recvHash = new HashMap();

    // Limit of packets queued for transmission or receipt
    private static final int QUEUE_SIZE = 4;

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

        // Creating and Starting threads
		sender = new Sender(theRF, this);
		receiver = new Receiver(theRF, this);
		Thread senderThread = new Thread(sender);
        Thread receiverThread = new Thread(receiver);

        receiverThread.start();
        senderThread.start();
	}

    /**
     * Increments the next sequence number for a given address
     * @param seqNum Sequence number of packet
     * @return the next sequence number
     */
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

    /**
     * Verifies the given address for received sequences
     * @param seqNum Sequence number of packet
     * @return the next expected sequence number
     */
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

		Packet packet;
        packet = new Packet(0, seqNum, dest, ourMAC, data);

        if(outgoingBlock.size() < QUEUE_SIZE) {
            // Puts the created packet into the outgoing queue
            try {
                outgoingBlock.put(packet);
            } catch (InterruptedException e) {
                currentStatus = UNSPECIFIED_ERROR;
                e.printStackTrace();
            }

            // Successful transmission
            currentStatus = SUCCESS;
            return len;
        }
        // Otherwise the outgoing queue is full
        else{
            currentStatus = INSUFFICIENT_BUFFER_SPACE;
            return 0;
        }
	}

	/**
	 * Recv method blocks until data arrives, then writes it an address info into
	 * the Transmission object.  See docs for full description.
	 */
	@Override
	public int recv(Transmission t) {
        // should add check for t being null

		//output.println("LinkLayer: Pretending to block on recv()");
        Packet packet;
        if(t == null){
            currentStatus = ILLEGAL_ARGUMENT;
        }
        try {
            // Grabs the next packet from the incoming queue
            packet = incomingBlock.take();
            if(packet.getSeqNum() < recvHash.get(packet.getSenderAddr())) {
                output.println("Already got this");
            }
            else {
                // Extracts the necessary pieces and puts them into the transmission object
                byte[] data = packet.getData();
                t.setSourceAddr(packet.getSenderAddr());
                t.setDestAddr(packet.getDestAddr());
                t.setBuf(data);
                // Length of the data received
                return data.length;
            }
        } catch (InterruptedException e) {
            currentStatus = UNSPECIFIED_ERROR;
            e.printStackTrace();
        }
        return -1;
	}

	/**
	 * Returns a current status code.  See docs for full description.
	 */
	@Override
	public int status() {
		return currentStatus;
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