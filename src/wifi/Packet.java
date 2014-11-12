package wifi;

import java.util.Arrays;

/**
 * For now, create a frame with type Data (000), a sequence number of zero, and
 * with the retry bit set to zero (off). The source and destination addresses
 * must be filled in correctly, and the checksum field filled with all 1s
 *
 *  @author Adam Myers
 *  @author Kaylene Barber
 */
public class Packet {

	final byte ZERO = 0;

    byte[] packet;
	// packet attributes
	int frameType;
	short seqNum;
	short destAddr;
	short senderAddr;
	byte[] data;
    byte[] crc;

	public Packet(byte[] frame) {
		packet = frame;
	}

	public Packet(int frameType, short seqNum, short destAddr,
			short senderAddr, byte[] data) {

		if (data != null) {
			packet = new byte[10 + data.length];
		} else {
			packet = new byte[10];
		}

		setData(data); // set data first to short circuit
		setControl(frameType, seqNum);
		setRetry(false);
		setDestAddr(destAddr);
		setSenderAddr(senderAddr);
		setCRC();
	}

	public void setControl(int frameType, short seqNum) {
		// Control bytes are positions 0 and 1 in packet
		packet[0] = ZERO;
		packet[1] = ZERO;
		// Eventually we will have to use bit shifting to get the frame type and
		// retry bit into the correct spots
	}

    public short getSeqNum() { return seqNum; }
	/**
	 * 0 for first time, 1 for retry
	 * @param retry
	 */
	public void setRetry(boolean retry) {

	}

	public int getFrameType() {
		return frameType;
	}

	public void setDestAddr(short destAddr) {
		// A mask to isolate each byte
		byte mask = (byte) 0xFF00;

		// Begin from 2, so we don't overwrite the control bytes
		for (int i = 2; i < 4; i++) {
			packet[i] = (byte) ((destAddr & mask) >> 8);
			mask >>>= 8;
		}
	}

	public short getDestAddr() {
		return destAddr;

	}

	public void setSenderAddr(short senderAddr) {

		// A mask to isolate each byte
		byte mask = (byte) 0xFF00;

		// Begin from 4, so we don't overwrite the control bytes
		for (int i = 4; i < 6; i++) {
			packet[i] = (byte) ((destAddr & mask) >> 8);
			mask >>>= 8;
		}
	}

	public short getSenderAddr() {
		return senderAddr;
	}

	public void setData(byte[] data) {
		if (data != null) {

			// Check data
			if (data.length > 2038) {
				throw new IllegalArgumentException("Invalid data.");
			} else {

				// Start from 6 so we don't overwrite other packet parts
				for (int i = 0; i < data.length; i++) {
					// put data bytes into packet!
					packet[i + 6] = data[i];
				}
			}
		}
	}

	public byte[] getData() {
		return data;
	}

	public void setCRC() {
        byte[] input;
		// Checksum begins at the end of the data
//		for (int i = 6 + data.length; i < packet.length; i++) {
//			packet[i] = (byte) 0xFF;
//		}
		// Eventually we will need to calculate the checksum, but for now we
		// will fill it completely with 1's
        input = new byte[4];
        Arrays.fill(input, (byte) 1);
	}

	public byte[] getCRC() {
		return crc;
	}

	public byte[] getFrame() {
		return packet;
	}

	@Override
	public String toString() {
		String type;
		switch (getFrameType()) {
		case 0:
			type = "DATA";
			break;
		case 1:
			type = "ACK";
			break;
		case 2:
			type = "BEACON";
			break;
		case 4:
			type = "CTS";
			break;
		case 5:
			type = "RTS";
			break;
		default:
			type = "UNKNOWN";
		}
		String out = "{" + type + " " + getSeqNum() + " " + getSenderAddr()
				+ ">" + getDestAddr() + " [" + getData().length + " bytes] ("
				+ getCRC() + ")}";

		return out;
	}
}
