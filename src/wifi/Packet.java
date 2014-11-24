package wifi;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * For now, create a frame with type Data (000), a sequence number of zero, and
 * with the retry bit set to zero (off). The source and destination addresses
 * must be filled in correctly, and the checksum field filled with all 1s
 *
 *  @author Adam Myers
 *  @author Kaylene Barber
 */
public class Packet {
<<<<<<< HEAD
    byte[] data;
=======

>>>>>>> origin/adamCheck2
    ByteBuffer packet;

	public Packet(byte[] frame) {
        if(frame == null) {
            throw new IllegalArgumentException("Packet cannot be null.");
        }else if(frame.length > 2038) {
            throw new IllegalArgumentException("Packet is way too big.");
        }
        packet = ByteBuffer.allocate(frame.length);
        packet = ByteBuffer.wrap(frame);
	}

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

    public void setFrameType(int type){
        // 0 = index  and what bytes to put in there
        // shifts
        packet.put(0, (byte) (packet.get(0) | (type << 5)));

    }
    public int getFrameType() {
        // 0xE0 = first 3 bits (left most)
        // shifts
        return ((packet.get(0) & 0xE0) >>> 5);
    }

    public void setRetry(boolean retry) {
        if (retry){
            // 0x10 = 0000 0000 0001 0000
            packet.put(0, (byte) (packet.get(0) | 0x10));
<<<<<<< HEAD
=======
        }else{
            packet.put(0, (byte) (packet.get(0) | 0x10));
>>>>>>> origin/adamCheck2
        }
    }

    public void setSeqNum(short seqNum) {
        //Set bits
        // fills first byte with zeroes or shifts
        packet.put(0, (byte) (packet.get(0) | (seqNum >>> 8)));
        // 0xFF = mask all but last 8 bits
        packet.put(1, (byte) (seqNum & 0xFF));
    }

    public short getSeqNum() {
        // 0xFFF = last 12 bits
        return (short)(packet.getShort(0) & 0xFFF);
    }

	public void setDestAddr(short destAddr) {
//		// A mask to isolate each byte
//		byte mask = (byte) 0xFF00;
//
//		// Begin from 2, so we don't overwrite the control bytes
//		for (int i = 2; i < 4; i++) {
//			packet[i] = (byte) ((destAddr & mask) >> 8);
//			mask >>>= 8;
//		}
        // Puts desination Address starting at 3 byte (0, 1, 2 <----- = 3th byte)
        packet.putShort(2, destAddr);
	}

	public short getDestAddr() {
        return packet.getShort(2);
	}

	public void setSenderAddr(short senderAddr) {
//
//		// A mask to isolate each byte
//		byte mask = (byte) 0xFF00;
//
//		// Begin from 4, so we don't overwrite the control bytes
//		for (int i = 4; i < 6; i++) {
//			packet[i] = (byte) ((destAddr & mask) >> 8);
//			mask >>>= 8;
//		}
        // Puts sender Address starting at 5 byte (0, 1, 2, 3, 4 <----- = 5th byte)
        packet.putShort(4, senderAddr);
	}

	public short getSenderAddr() {
		return packet.getShort(4);
	}

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
//				// Start from 6 so we don't overwrite other packet parts
//				for (int i = 0; i < data.length; i++) {
//					// put data bytes into packet!
//					packet[i + 6] = data[i];
//				}
//			}
		}
	}

	public byte[] getData() {
        // creates array the size of packet minus header info
        byte[] data = new byte[packet.limit() - 10];

        // So we don't overwrite other packet parts
        for(int i = 0; i < data.length; i++){
            data[i] = packet.get(i + 6);
        }
        return data;
	}

	public void setCRC() {
        //byte[] input;
		// Checksum begins at the end of the data
//		for (int i = 6 + data.length; i < packet.length; i++) {
//			packet[i] = (byte) 0xFF;
//		}
        CRC32 crc32 = new CRC32();
        // the array backing packet, the offset, and length
        crc32.update(packet.array(), 0, packet.limit()-4);
        // casts as int
        int crc = (int)crc32.getValue();
        // puts the CRC in the last 4 bits of the packet
        packet.putInt(packet.limit() - 4, crc);
	}

	public int getCRC() {
        // last 4 bytes of the packet = CRC
        int crc = packet.getInt(packet.limit() - 4);
        return crc;
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
