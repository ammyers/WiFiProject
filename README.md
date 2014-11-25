WiFiProject
===========
UPS CS325 Course Project

@author Adam Myers and Kaylene Barber
@date 11/18/2014

Checkpoint 2 - 11/10

	Implemented two-threaded approach to simulating Wifi. 
	Threads: Sender and Receiver
	Packet class does dirty work of taking data from Receiver and packaging it appropriately for transmission.
	
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
	
