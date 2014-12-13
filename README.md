WiFiProject
===========
UPS CS325 Course Project

@author Adam Myers and Kaylene Barber
@date 12/12/2014 

Final Checkpoint - 12/12

	Adds debug and status codes
	Full implementation of MAC protocol in Sender
		Creates and sends beacons for clock synchronization
	Receiver handles Data, ACK, and Beacon packets appropriately
	
Checkpoint 3 - 11/24

  	Sender to follow proper MAC protocol including:
  		SIFS waiting
  		Checking network activity
  		Exponential backoff
  		Waiting for ACK and resending if necessary
  	Receiver:
  		Verifying sequence numbers
  		Sending ACK packets
  	LinkLayer:
  		Hash for Received ACKs
  		Tracking of received Packet sequence numbers
	
Checkpoint 2 - 11/10

	Implemented two-threaded approach to simulating Wifi. 
	Threads: Sender and Receiver
	Packet class does dirty work of taking data from Receiver and packaging it appropriately for transmission.	
