package wifi;
import rf.RF;

import java.nio.ByteBuffer;

/**
 * Created by Adam on 11/9/2014.
 */
public class Receiver implements Runnable {
    private RF theRF;
    private final int SIZE_O_PACKET = 10; // Max packet size, yes I meant size o' Packet
    private LinkLayer theLink;

    public Receiver(RF theRF, LinkLayer theLink){
        this.theRF = theRF;
        this.theLink = theLink;
    }

    public void run() {
        System.out.println("Listener is alive and well");
        byte[] packet; // Received packet
        ByteBuffer message; // Byte array from RF layer
        StringBuffer output = new StringBuffer(); // Gets output message

        while (true) {

            try {
                //Sleeps each time through, in order to not monopolize the CPU
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Gets data from the RF layer, turns it into packet form
            Packet p = new Packet(theRF.receive());
            // Puts the new Packet into the LinkLayer's inbound queue
            try {
                theLink.getIn().put(p);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
