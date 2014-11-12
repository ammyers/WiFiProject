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
            // Gets data from the RF layer, turns it into a packet
            Packet p = new Packet(theRF.receive());
            // Puts the new Packet into the LinkLayer's incoming queue block
            try {
                theLink.getIncomingBlock().put(p);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
