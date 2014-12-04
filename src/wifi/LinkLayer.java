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

    /**
     * Status codes to help with debugging
     */
    public int currentStatus;
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

    public int debug = 0;
    public final int FULL_DEBUG = -1;
    /**
     * random slots needs to adjust window size in sender. if True the retry limit hasn't been hit for resending ACK
     * Need to make a random and define the slot as random.nextInt(collisionWindow), else slot = collisionWindow
     */
    public boolean randomSlots = true;
    public int beaconDelay = -1;

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

        output.println("Send command 0 to see a list of supported commands");

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
            if (debug == FULL_DEBUG) {
                output.println("Queueing " + packet.getData().length + " bytes for " + dest);
            }
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
	public int command(int command, int value) {
        switch (command) {
            case 0:
                output.println("Options & Settings:");
                output.println("-----------------------------------------");
                output.println("Command 0: \t View all options and settings.");
                output.println("Command 1: \t Set debug value. Debug currently at "
                        + debug);
                output.println("\t Use -1 for full debug output, 0 for no output.");
                output.println("Command 2: \t Set slot for link layer. 0 for random slot time otherwise max slot time.");
                output.println("Command 3: \t Set desired wait time between start of beacon transmissions (in seconds).");
                break;
            case 1:
                currentStatus = SUCCESS;
                if (value == FULL_DEBUG) {
                    debug = FULL_DEBUG;
                }

                if(value == 0){
                    debug = 0;
                }
                output.println("Setting debug to " + debug);
                break;
            case 2:
                currentStatus = SUCCESS;
                if (value == 0) {
                    output.println("Using random slot times");
                    randomSlots = true;
                } else {
                    output.println("Using maximum slot times");
                    randomSlots = false;
                }
                break;
            case 3:
                currentStatus = SUCCESS;
                if (value < 0) {
                    beaconDelay = -1;
                    output.println("Disabling beacons");
                } else {
                    output.println("Using a beacon delay of " + value + " seconds");
                    //Convert to milliseconds
                    beaconDelay = value * 1000;
                }
                break;
            default: //Unknown command
                currentStatus = ILLEGAL_ARGUMENT;
                output.println("Command " + command + " not recognized.");
        }
        return 0;
	}
}