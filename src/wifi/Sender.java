package wifi;

import rf.RF;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by Adam on 11/9/2014.
 */
public class Sender {
    private final int MAX_WAIT_MILLI = 7000; // Max wait in milliseconds
    private Random random; // Random Number Generator
    private int senderMAC;	// stores MAC address of sender
    private final int SIZE_O_PACKET = 10; // Max packet size, yes I meant size o' Packet
    private RF theRF;

    // MAC is the MAC address of this sender
    public Sender(int MAC, RF theRF){
        senderMAC = MAC;
        random = new Random();
        this.theRF = theRF;
    }

    // Creates a packet. Address is followed by the data (network byte order)
    private byte[] createPacket(int sendMAC, long data){
        ByteBuffer tempBuffer = ByteBuffer.allocate(SIZE_O_PACKET);

        // Puts address at beginning of packet (2 bytes)
        tempBuffer.putShort((short)sendMAC);
        // Puts data after MAC id (8 bytes)
        tempBuffer.putLong(data);
        // Converts ByteBuffer into an array
        byte[] packet = tempBuffer.array();

        return packet;
    }

    // Creates a string representation of the packet that is sent.
    private String createString(byte[] packet){
        StringBuffer magicString = new StringBuffer();

        // Puts a beginning bracket at beginning of string
        magicString.append('[');
        for(int i = 0; i <= packet.length-1; i++){
            magicString.append((int)packet[i]);
            // If at the end of the packet, puts an ending bracket
            if(i == packet.length-1) {
                magicString.append(']');
            }
            else{
                // Otherwise places spaces between data
                magicString.append(' ');
            }
        }
        return magicString.toString();
    }

    public void run(){
        System.out.println("Creator is alive and well");
        long currentTime;		// Gets time returned by RF layer
        int byteSent = 0; // Gets number of bytes in transmission
        byte[] packet; // Packet to send

        /***** Infinite loop *****/
        while (true){
            currentTime = theRF.clock();
            // Transmits message
            packet = createPacket(senderMAC, currentTime);
            byteSent = theRF.transmit(packet);

            // If packet is not the correct size
            if (byteSent != SIZE_O_PACKET) {
                System.err.println("Error: Packet corruption detected due to wrong length");
            }

            System.out.println("Sent Packet: " + senderMAC + " " + currentTime + "  " + createString(packet));

            // Thread waits a random interval up to 7000 milliseconds before starting again
            try {
                Thread.sleep((long)random.nextInt(MAX_WAIT_MILLI));
            }
            catch (InterruptedException e) {
                System.err.println("Creator thread woke up early!! This is bad.");
            }
        }
    }
}

