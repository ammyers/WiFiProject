package wifi;

import java.util.Random;

import rf.RF;
import sun.awt.image.ImageWatched;

/**
 * Created by Adam on 11/9/2014.
 */
public class Sender implements Runnable {
	private final int MAX_WAIT_MILLI = 7000; // Max wait in milliseconds
	private Random random; // Random Number Generator
	private int senderMAC;	// stores MAC address of sender
	private final int SIZE_O_PACKET = 10; // Max packet size, yes I meant size o' Packet
	private RF theRF;

    private LinkLayer theLink;

	// MAC is the MAC address of this sender
	public Sender(int MAC, RF theRF, LinkLayer theLink){
		senderMAC = MAC;
		random = new Random();
		this.theRF = theRF;
        this.theLink = theLink;
	}


	@Override
	public void run(){
		System.out.println("Sender is alive and well");
		int byteSent = 0; // Gets number of bytes in transmission


        while (true) {


            try {
                //Sleeps each time through, in order to not monopolize the CPU
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // If there are Packets to be sent in the LinkLayer's outbound queue
            //Also makes sure the RF is not in use
            if (theLink.getOut().isEmpty() == false && theRF.inUse() == false) {

                // Send the first packet out on the RF layer
                try {
                    theRF.transmit(theLink.getOut().take().getFrame());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
	}
}

