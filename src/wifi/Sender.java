package wifi;

import java.util.Random;

import rf.RF;

/**
 * Created by Adam on 11/9/2014.
 */
public class Sender implements Runnable {
	private final int MAX_WAIT_MILLI = 7000; // Max wait in milliseconds
	private Random random; // Random Number Generator
	private int senderMAC;	// stores MAC address of sender
	private final int SIZE_O_PACKET = 10; // Max packet size, yes I meant size o' Packet
	private RF theRF;
	private Packet packet;

    private LinkLayer link;

	// MAC is the MAC address of this sender
	public Sender(int MAC, RF theRF){
		senderMAC = MAC;
		random = new Random();
		this.theRF = theRF;
	}


	@Override
	public void run(){
		System.out.println("Sender is alive and well");
		int byteSent = 0; // Gets number of bytes in transmission


		while (true){
			// Transmits message
			byteSent = theRF.transmit(packet.getFrame());

			// If packet is not the correct size
			if (byteSent != SIZE_O_PACKET) {
				System.err.println("Error: Packet corruption detected due to wrong length");
			}

			// Thread waits a random interval up to 7000 milliseconds before starting again
			try {
				Thread.sleep(random.nextInt(MAX_WAIT_MILLI));
			}
			catch (InterruptedException e) {
				System.err.println("Creator thread woke up early!! This is bad.");
			}
		}
	}
}

