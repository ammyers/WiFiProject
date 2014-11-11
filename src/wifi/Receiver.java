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

    public Receiver(RF theRF){
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
                Thread.sleep(10); //Sleeps each time through, in order to not monopolize the CPU
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Packet p = new Packet(theRF.receive()); // Gets data from the RF layer, turns it into packet form
            try {
                theLink.getIn().put(p); // Puts the new Packet into the LinkLayer's inbound queue
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
