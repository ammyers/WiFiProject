package wifi;
import rf.RF;

/**
 *  @author Adam Myers
 *  @author Kaylene Barber
 */
public class Receiver implements Runnable {
    private RF theRF;
    private LinkLayer theLink;

    public Receiver(RF theRF, LinkLayer theLink){
        this.theRF = theRF;
        this.theLink = theLink;
    }

    public void run() {
        System.out.println("Listener is alive and well");

        while (true) {
            try {
                //Sleeps each time so we don't destroy the CPU
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Gets data from the RF layer, turns it into packet form
            Packet packet = new Packet(theRF.receive());

            short destAddr = packet.getDestAddr();

<<<<<<< HEAD
            // 0xffff = bottem 16 bits are 1's
            if((destAddr & 0xffff) == theLink.ourMAC || (destAddr & 0xffff) == 65535) {

                // if the destination address is ours and the packet is a data packet
                if((destAddr & 0xffff) == theLink.ourMAC && packet.getFrameType() == 0) {

                    short nextSeqNum = theLink.gotSeqNum(packet.getSenderAddr());

                    // if the packets sequence number is larger than the next one
                    if(packet.getSeqNum() > nextSeqNum){
                        theLink.output.println("Sequence out of order, expected: "+ nextSeqNum+ " got: "+ packet.getSeqNum() );
=======
            // 0xffff = bottom 16 bits are 1's
            if(destAddr == theLink.ourMAC) {

                // if the destination address is ours and the packet is a data packet
                if(packet.getFrameType() == 0) {

                    short nextSeqNum = theLink.gotSeqNum(packet.getSenderAddr());

                    // if the packets sequence number is larger than the expected one
                    if(packet.getSeqNum() > nextSeqNum){
                        theLink.output.println("Packet Sequence out of order, expected: "+ nextSeqNum+ " got: "+ packet.getSeqNum() );
>>>>>>> origin/adamCheck2
                    }
                    try {
                        // Puts the new Packet into the LinkLayer's inbound queue
                        theLink.getIncomingBlock().put(packet);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Packet ack = new Packet(1, packet.getSeqNum(), packet.getSenderAddr(), theLink.ourMAC, null);

                    try {
                        // Sleeps SIFS amount
                        Thread.sleep(RF.aSIFSTime);
<<<<<<< HEAD
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // transmits our ACK
                    theRF.transmit(ack.getFrame());
                }
                // if the destination address is ours and it is an ACK
                else if((destAddr & 0xffff) == theLink.ourMAC && packet.getFrameType() == 1) {
                    /*
                        if link layer received acks hash contains sender address packet number
                        and if ack sequence number is the same as our sent packet, we already got it
                        else add ACK for the packet
                     */
=======
                        // transmits our ACK
                        theRF.transmit(ack.getFrame());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
>>>>>>> origin/adamCheck2
                }
            }
        }
    }
}
