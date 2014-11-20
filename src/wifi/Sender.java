package wifi;

import rf.RF;


/**
 *  @author Adam Myers
 *  @author Kaylene Barber
 */
public class Sender implements Runnable {

	private RF theRF;
    private LinkLayer theLink;

	public Sender(RF theRF, LinkLayer theLink) {
		this.theRF = theRF;
        this.theLink = theLink;
    }


	@Override
	public void run(){
		System.out.println("Sender is alive and well");

        while (true) {

            try {
                //Sleeps each time so we don't destroy the CPU
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // If there are Packets to be sent in the LinkLayer's outgoing queue block
            // and checks the network for activity
            if (theLink.getOutgoingBlock().isEmpty() == false && theRF.inUse() == false) {

                // Send the first packet out on the RF layer
                try {
                    theRF.transmit(theLink.getOutgoingBlock().take().getFrame());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Packet packet = null;
                int counter = 0;

                // while counter isn't the limit of retries
                // and the received ACK is from destination and contains wrong sequence number
                while((counter < RF.dot11RetryLimit) && theLink.receivedACKS.containsKey(packet.getDestAddr())
                        && theLink.receivedACKS.get(packet.getDestAddr()).contains(packet.getSeqNum()) == false) {

                    // create new packet
                    Packet retryPacket = new Packet(packet.getFrameType(),packet.getSeqNum(),packet.getDestAddr(), packet.getSenderAddr(), packet.getData());
                    // but set retry bit to 1 (true)
                    retryPacket.setRetry(true);

                    // Send the first packet out on the RF layer
                    theRF.transmit(retryPacket.getFrame());

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    counter++;
                }

            }
        }
	}
}

