package wifi;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 *  Packet class that packages all necessary information required for
 *  transmission in 802.11~ specifications.
 *
 *  Includes ability for data, beacon, and ACK packet creation.
 *
 *  @author Adam Myers
 *  @author Kaylene Barber
 */
public class Packet {

	ByteBuffer packet;

    /**
     * Constructor for packet, allocates space in a Byebuffer
     * @param frame the packet
     */
	public Packet(byte[] frame) {
		if(frame == null) {
			throw new IllegalArgumentException("Packet cannot be null.");
		}else if(frame.length > 2038) {
			throw new IllegalArgumentException("Packet is way too big.");
		}
		packet = ByteBuffer.allocate(frame.length);
		packet = ByteBuffer.wrap(frame);
	}

    /**
     * Builds packet!
     * @param frameType Data, ACK, BEACON, CTS, RTS
     * @param seqNum Sequence number of this packet
     * @param destAddr Address of where this packet is going to (destination)
     * @param senderAddr Address of where this packet is coming from (here)
     * @param data Data that is being sent up to 2038 bytes
     */
	public Packet(int frameType, short seqNum, short destAddr, short senderAddr, byte[] data){

		if (data != null) {
			packet = ByteBuffer.allocate(10 + data.length);
		} else {
			packet = ByteBuffer.allocate(10);
		}

		setData(data);
		setFrameType(frameType);
		setRetry(false);
		setSeqNum(seqNum);
		setDestAddr(destAddr);
		setSenderAddr(senderAddr);
	}

    /**
     * Sets the type of the packet
     * @param type Integer that tells us what kind of packet it is
     */
	public void setFrameType(int type){
		// 0 = index  and what bytes to put in there
		// shifts
		packet.put(0, (byte) (packet.get(0) | (type << 5)));
	}

    /**
     * Determines the type of the packet
     * @return bits for type
     */
	public int getFrameType() {
		// 0xE0 = first 3 bits (left most)
		// shifts
		return ((packet.get(0) & 0xE0) >>> 5);
	}

    /**
     * Sets the retry bit
     * @param retry 0 if not a retry, 1 if retry
     */
	public void setRetry(boolean retry) {
		// clears bit to be 0
		packet.put(0,(byte)(packet.get(0) & 0xFFFFFFEF));
		// if retry is true, need to set to 1
		// otherwise still false and zero
		if (retry){
			// 0x10 = 0000 0000 0001 0000
			packet.put(0, (byte) (packet.get(0) | 0x10));
		}
	}

    /**
     * Sets the sequence number of the packet
     * @param seqNum unique ID of packet
     */
	public void setSeqNum(short seqNum) {
		//Set bits
		// fills first byte with zeroes or shifts
		packet.put(0, (byte) (packet.get(0) | (seqNum >>> 8)));
		// 0xFF = mask all but last 8 bits
		packet.put(1, (byte) (seqNum & 0xFF));
	}

    /**
     * Gets sequence number
     * @return unique ID of packet
     */
	public short getSeqNum() {
		// 0xFFF = last 12 bits
		return (short)(packet.getShort(0) & 0xFFF);
	}

    /**
     * Sets the destination address in packet header
     * @param destAddr Address of where this packet is going to (destination)
     */
	public void setDestAddr(short destAddr) {
		// Puts desination Address starting at 3 byte (0, 1, 2 <----- = 3th byte)
		packet.putShort(2, destAddr);
	}

    /**
     * Finds destination address
     * @return destination address
     */
	public short getDestAddr() {
		return packet.getShort(2);
	}

    /**
     * Sets the sender address in packet header
     * @param senderAddr Address of where this packet is coming from (here)
     */
	public void setSenderAddr(short senderAddr) {
		// Puts sender Address starting at 5 byte (0, 1, 2, 3, 4 <----- = 5th byte)
		packet.putShort(4, senderAddr);
	}

    /**
     * Finds sender address
     * @return destination address
     */
	public short getSenderAddr() {
		return packet.getShort(4);
	}

    /**
     * Puts data into packet
     * @param data information to be put into packet
     */
	public void setData(byte[] data) {
		if (data != null) {
			// Check data
			if (data.length > 2038) {
				throw new IllegalArgumentException("Invalid because of too much data.");
			} else {
				// Puts data starting at 7 byte (0, 1, 2, 3, 4, 5, 6 <----- = 7th byte)
				for (int i = 0; i < data.length; i++){
					packet.put(i + 6, data[i]);
				}
			}
		}
	}

    /**
     * Gets data from received packet
     * @return information received
     */
	public byte[] getData() {
		// creates array the size of packet minus header info
		byte[] data = new byte[packet.limit() - 10];

		// So we don't overwrite other packet parts
		for(int i = 0; i < data.length; i++){
			data[i] = packet.get(i + 6);
		}
		return data;
	}

    /**
     * Creates checksum and sets that value in packet header
     */
	public void setCRC() {
		CRC32 crc32 = new CRC32();
		// the array backing packet, the offset, and length
		crc32.update(packet.array(), 0, packet.limit()-4);
		// casts as int
		int crc = (int)crc32.getValue();
		// puts the CRC in the last 4 bits of the packet
		packet.putInt(packet.limit() - 4, crc);
	}

    /**
     * Gets CRC of received packet
     * @return checksum of packet
     */
	public int getCRC() {
		// last 4 bytes of the packet = CRC
		int crc = packet.getInt(packet.limit() - 4);
		return crc;
	}

    /**
     * Determines if packet is not-corrupted
     * @return true if not-corrupt, false if corrupt packet.
     */
    public boolean isValidCRC(){
        int temp = getCRC();

        CRC32 crc = new CRC32();
        crc.update(packet.array(), 0, packet.limit() - 4);
        int validCRC = (int) crc.getValue();

        if(temp == validCRC){
            return true;
        }else{
            return false;
        }
    }

	public byte[] getFrame() {
		setCRC();
		// returns array backing ByteBuffer packet
		return packet.array();
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
		String output = "{" + type + " " + getSeqNum() + " " + getSenderAddr()
				+ ">" + getDestAddr() + " [" + getData().length + " bytes] ("
				+ getCRC() + ")}";

		return output;
	}
}
