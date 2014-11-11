package wifi;
import rf.RF;

import java.nio.ByteBuffer;

/**
 * Created by Adam on 11/9/2014.
 */
public class Receiver implements Runnable {
    private RF theRF;
    private final int SIZE_O_PACKET = 10; // Max packet size, yes I meant size o' Packet

    public Receiver(RF theRF){
        this.theRF = theRF;
    }

    // Creates a string representation of the packet that is received.
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

    public void run() {
        System.out.println("Listener is alive and well");
        byte[] packet; // Received packet
        ByteBuffer message; // Byte array from RF layer
        StringBuffer output = new StringBuffer(); // Gets output message

        /***** Infinite loop *****/
        while (true) {
            packet = theRF.receive();
            if (packet.length == SIZE_O_PACKET) { //if the packet is the correct size:
                message = ByteBuffer.wrap(packet);

                //Build output message
                output.append("Host ");
                //output.append((message.getShort(0) & 0xffff)); //Get rid of signed short
                output.append(" says the time is ");
                output.append(message.getLong(2));
                System.out.println("Received: " + createString(packet));
                System.out.println(output.toString());

                // Resets String Buffer
                output.delete(0, output.length());
            }
            else { // Otherwise received packet is not the correct size
                System.err.println("Error: Packet corruption detected due to wrong length");
            }
        }
    }
}
