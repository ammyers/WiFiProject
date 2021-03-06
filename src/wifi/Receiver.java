package wifi;

import rf.RF;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *  Thread that handles all receiving duties for the Link Layer and
 *  also creates ACKs as a response to receiving packets
 *
 *  @author Adam Myers
 *  @author Kaylene Barber
 */
public class Receiver implements Runnable {
    private RF theRF;
    private LinkLayer theLink;
    public int status;
    private int QUEUE_SIZE = 4;
    private static final long PROCESSING_DELAY = 450;
    private long ourOffset;

    /**
     * Constructor - checks initialization as well
     * @param theRF our instance of the RF layer
     * @param theLink our instance of Link Layer
     */
    public Receiver(RF theRF, LinkLayer theLink){
        this.theRF = theRF;
        this.theLink = theLink;
        status = theLink.currentStatus;
        // RF unable to start
        if(theRF == null){
            status = theLink.RF_INIT_FAILED;
        }
    }

    public void run() {

        while (true) {
            try {
                //Sleeps each time so we don't destroy the CPU
                Thread.sleep(10);
            } catch (InterruptedException e) {
                status = theLink.UNSPECIFIED_ERROR;
                e.printStackTrace();
            }

				// Gets data from the RF layer, turns it into packet form
				Packet packet = new Packet(theRF.receive());

            if(theLink.debug == theLink.FULL_DEBUG && packet.getFrameType() != 1){
                theLink.output.println("TX starting from " + packet.getSenderAddr() + " at local time " + getTime());
            }
				short destAddr = packet.getDestAddr();

            // If the incoming packet has a valid CRC
            if (packet.isValidCRC()) {

                if(theLink.debug == theLink.FULL_DEBUG) {
                    theLink.output.println("\tReceived packet with valid CRC: " + packet.toString());
                }
                // 0xffff = bottom 16 bits are 1's
                // If the packet is for us or is a BROADCAST packet
                if ((destAddr & 0xffff) == theLink.ourMAC || (destAddr & 0xffff) == 65535) {

                    // If we have a DATA packet and space in the incoming queue
                    if ((destAddr & 0xffff) == theLink.ourMAC && packet.getFrameType() == 0
                            && theLink.getIncomingBlock().size() < QUEUE_SIZE) {

                        short nextSeqNum = theLink.gotSeqNum(packet.getSenderAddr());

                        // if the packets sequence number is larger than the expected one
                        if (packet.getSeqNum() > nextSeqNum) {
                            // need to print to output stream
                            theLink.output.println("Packet out of order, expected: " + nextSeqNum + " but got: " + packet.getSeqNum());
                            if (theLink.debug == theLink.FULL_DEBUG) {
                                status = theLink.BAD_ADDRESS;
                            }
                        }
                        try {
                            // Puts the new Packet into the LinkLayer's inbound queue
                            theLink.getIncomingBlock().put(packet);
                        } catch (InterruptedException e) {
                            status = theLink.UNSPECIFIED_ERROR;
                            e.printStackTrace();
                        }

                        Packet ack = new Packet(1, packet.getSeqNum(), packet.getSenderAddr(), theLink.ourMAC, null);

                        try {
                            // Sleeps SIFS amount
                            Thread.sleep(RF.aSIFSTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // transmits our ACK
                        theRF.transmit(ack.getFrame());

                        if (theLink.debug == theLink.FULL_DEBUG) {
                            theLink.output.println("Sent ACK with sequence number " + ack.getSeqNum() + " to MAC address "
                                    + ack.getDestAddr());
                        }
                    }

                    // Receive an ACK for us
                    else if ((destAddr & 0xffff) == theLink.ourMAC && packet.getFrameType() == 1) {

                        theLink.output.println("Got a valid ACK: " + packet.toString());

                        // The link contains the address of the sender of this ACK
                        if (theLink.receivedACKS.containsKey(packet.getSenderAddr())) {

                            // And if this ACK sequence number is in the queue, it's a duplicate
                            if (theLink.receivedACKS.get(packet.getSenderAddr()).contains(packet.getSeqNum())) {
                                theLink.output.println("Received duplicate ACK for sequence number " + packet.getSeqNum() + " from " + packet.getSenderAddr());
                            } else {
                                // otherwise add to the hash
                                theLink.receivedACKS.get(packet.getSenderAddr()).add(packet.getSeqNum());
                            }

                        } else {
                            // Haven't gotten an ACK from this sender before
                            ArrayList<Short> newHost = new ArrayList<Short>();
                            newHost.add(packet.getSeqNum());
                            // Add sender address and the sequence number to hash
                            theLink.receivedACKS.put(packet.getSenderAddr(), newHost);
                        }
                    }
                    // Receive a BEACON packet
                    else if (packet.getFrameType() == 2) {

                        byte[] beacon = packet.getData();
                        ByteBuffer buffer;
                        buffer = ByteBuffer.wrap(beacon);
                        long syncTime = buffer.getLong();

                        long ourTime = getTime();

                        // If the sync time is ahead of us, we adjust it. Otherwise ignore this beacon because it's behind
                        if (syncTime > ourTime) {
                            ourOffset = syncTime - (ourTime + PROCESSING_DELAY);
                            if(theLink.debug == theLink.FULL_DEBUG) {
                                theLink.output.println("Adjusted our clock by " + ourOffset + " due to beacon: \n \t incoming offset was " + syncTime +
                                        " vs. our " + ourTime + ". Time is now: " + getTime());
                            }
                        } else {
                            if(theLink.debug == theLink.FULL_DEBUG) {
                                theLink.output.println("Ignored beacon, incoming time was " + syncTime +
                                        " vs. our " + ourTime + ". Time is now: " + getTime());
                            }
                        }
                    }
                     // Packet is a BROADCAST packet
                    else if((destAddr & 0xffff) == 65535){
                        short nextSeq = theLink.gotSeqNum(packet.getSenderAddr());

                        // Wrong sequence number received
                        if (packet.getSeqNum() > nextSeq) {
                            // need to print to output stream
                            theLink.output.println("Broadcast packet had wrong sequence number. Expected: "
                                    + nextSeq + ". Received: "
                                    + packet.getSeqNum() + ".");
                        }

                        // Put packet in Link Layer's incoming queue
                        try {
                            theLink.getIncomingBlock().put(packet);
                        } catch (InterruptedException e) {
                            status = theLink.UNSPECIFIED_ERROR;
                            e.printStackTrace();
                        }
                    }
                    else{
                        //Frame type other than DATA, ACK, or BEACON
                        theLink.output.println("Unexpected frame type");
                        status = theLink.BAD_MAC_ADDRESS;
                    }
                }
             // Bad CRC on incoming packet.
            }else{
                if(theLink.debug == theLink.FULL_DEBUG) {
                    theLink.output.println("Ignored packet with a BAD CRC: " + packet.toString());
                }
            }
        }
    }

    /**
     * Helper method used in clock synchronization
     * @return current time including our offset
     */
    private long getTime(){
        return theRF.clock() + ourOffset;
    }
}
