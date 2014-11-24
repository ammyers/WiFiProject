package wifi;

import rf.RF;

import java.util.Random;


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
                // If there are Packets to be sent in the LinkLayer's outgoing queue block
                // and checks the network for activity
                if (!theLink.getOutgoingBlock().isEmpty() && !theRF.inUse()) {

                    // Send the first packet out on the RF layer after SIFS
                    Thread.sleep(RF.aSIFSTime);
                    if (!theRF.inUse()){
                        theRF.transmit(theLink.getOutgoingBlock().take().getFrame());
                    }else {
                        waitBS();
                        expBackoff();
                        theRF.transmit(theLink.getOutgoingBlock().take().getFrame());
                    }
                } else {
                    waitBS();
                    expBackoff();
                    theRF.transmit(theLink.getOutgoingBlock().take().getFrame());
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitBS(){
        // Not busy wait, initial check
        while(theRF.inUse()){
            try{
                Thread.sleep(theRF.aSlotTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // RF is not in use but we wait IFS again
        try{
            Thread.sleep(theRF.aSIFSTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // If RF is in use, recurse and uses counter to avoid infinite recurse
        if (theRF.inUse()){
            //recurse?
            waitBS();
        }else {
            return;
        }
    }

    private void expBackoff(){
        int timer = theRF.aSlotTime;
        Random random = new Random();

        // while the timer isn't zero
        if(timer != 0){
            // check rf
            while(theRF.inUse()){
                try{
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            timer--;
        }else{
            // timer is zero, must wait one last time
            while(theRF.inUse()){
                try{
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void ackWait(){

        try{
            Thread.sleep(theRF.aSlotTime);
            //
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

