package wifi;

import java.nio.ByteBuffer;
import java.util.Random;

import rf.RF;

/**
 *  Thread that takes care of most sending functions of the Link Layer and
 *  implements the entire MAC logic.
 *
 *  @author Adam Myers
 *  @author Kaylene Barber
 */
public class Sender implements Runnable {

    private RF theRF;
    private LinkLayer theLink;
    private int window = RF.aCWmin;
    private Packet packet;
    private int delay = -1;
    private long lastBeacon = 0;
    private int beaconOffset = 1500;// Guesstimate
    public static final int SIFS = RF.aSIFSTime;
    public static final int SLOT = RF.aSlotTime;
    public static final int DIFS = RF.aSIFSTime + (2 * RF.aSlotTime);

    /**
     * Constructor
     * @param theRF Instance of RF layer
     * @param theLink Instance of Link Layer
     */
    public Sender(RF theRF, LinkLayer theLink) {
        this.theRF = theRF;
        this.theLink = theLink;
    }

    @Override
    public void run() {

        while (true) {

            try {
                //Sleeps each time so we don't destroy the CPU
                Thread.sleep(10);

                long time = getTime();

                // Our time is ahead of the last beacon and processing delay
                if(lastBeacon + delay <= time && delay > 0){
                    lastBeacon = time;

                    // Create beacon and send
                    ByteBuffer temp = ByteBuffer.allocate(8);
                    temp.putLong(getTime() + beaconOffset);
                    byte[] timeStamp = temp.array();
                    Packet beacon = new Packet(2, (short) 0, (short)-1, theLink.ourMAC, timeStamp);
                    // we need to send this according to rules
                    ////////////
                    //////////////////
                    /////////////
                }


                // If there are Packets to be sent in the LinkLayer's outgoing queue block
                // and checks the network for activity
                if (!theLink.getOutgoingBlock().isEmpty()) {
                    int counter = 0;
                    packet = new Packet(theLink.getOutgoingBlock().take().getFrame());

                    boolean receivedACK = false;
                    while (receivedACK == false) {
                        while (counter < RF.dot11RetryLimit) {
                            if (!theRF.inUse()) {
                                // Send the first packet out on the RF layer after SIFS
                                Thread.sleep(RF.aSIFSTime);
                                // ideal case
                                if (!theRF.inUse()) {
                                    theRF.transmit(packet.getFrame());
                                    receivedACK = ackWait();
                                    if (receivedACK) {
                                        break;
                                    } else {

                                        counter++;
                                    }
                                    // Was idle, we waited IFS, then wasn't idle anymore
                                } else {
                                    waitBS();
                                    expBackoff();
                                    theRF.transmit(packet.getFrame());
                                    receivedACK = ackWait();
                                    if (receivedACK) {
                                        break;
                                    } else {

                                        counter++;
                                    }
                                }
                                // was never idle
                            } else {
                                waitBS();
                                expBackoff();
                                theRF.transmit(packet.getFrame());
                                receivedACK = ackWait();
                                if (receivedACK) {
                                    break;
                                } else {

                                    counter++;
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitBS() {
        // Not busy wait, initial check
        while (theRF.inUse()) {
            try {
                Thread.sleep(RF.aSlotTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // RF is not in use but we wait IFS again
        try {
            Thread.sleep(RF.aSIFSTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // If RF is in use, recurse and uses counter to avoid infinite recurse
        if (theRF.inUse()) {
            waitBS();
        }
    }

    private void expBackoff() {
        int timer = RF.aSlotTime;
        Random random = new Random();

        // while the timer isn't zero
        if (timer != 0) {
            // check rf
            while (theRF.inUse()) {
                try {
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // need to figure out. Says timer is always not zero
                timer--;
            }

        } else {
            // timer is zero, must wait one last time
            while (theRF.inUse()) {
                try {
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Method that checks for ACKS
     * @return true if received the right ACK, false if not and exponentially
     * increase collision window
     */
    private boolean ackWait() {

        //Average ACK transmission???? + SIFS + SLOT (802.11 IEEE Spec.)
        try {
            Thread.sleep( (long)(2000 + SIFS + SLOT));
        } catch (InterruptedException e) {
            theLink.currentStatus = theLink.UNSPECIFIED_ERROR;
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < 10; i++) {
                if (!theLink.receivedACKS.containsKey(packet.getDestAddr())) {
                    // need to change sleep time to be more appropriate??
                    Thread.sleep(10);
                } else {
                    return true;
                }
            }

            if ((window * 2) > RF.aCWmax) {
                window *= 2;
            }
            packet.setRetry(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Helper method used in clock synchronization
     * @return current time including our offset
     */
    private long getTime(){
        long ourOffset = 0;
        return theRF.clock() + ourOffset;
    }

    /**
     * Rounds a value up to the nearest 50 for timing issues
     * @param input
     * @return input rounded
     */
    private long roundUp(long input){
        //If already multiple of 50
        if (Math.ceil(input % 50L / 50.0D) == 1.0D){
            return input + (50L - input % 50L);
        }else{
            return input;
        }
    }

    /**
     * Waits until the given time according to the current local clock including offset
     * @param time
     * @return does stuff
     */
    private long waitUntil(long time){
        long tempTime = getTime();

        while(tempTime < time){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                theLink.currentStatus = theLink.UNSPECIFIED_ERROR;
                e.printStackTrace();
            }
            tempTime += 10;
        }
        return time;
    }

    /**
     * Aligns the wait time with rounding slots of 50.
     * @param waitTime
     * @return The time to wait
     */
    private long nearWait(long waitTime){
        return roundUp(getTime() + waitTime);
    }

}
