package com.handwin.entity;

import com.handwin.packet.UDPServerPacket;

/**
 * 
 * @author fangliang
 *
 */
public class UDPServerPacket2 {
	
	private String id1 ;
	private UDPServerPacket udpServerPacket1 ;
	
	private String id2;
	private UDPServerPacket udpServerPacket2 ;
	
	
	public void setId1(String id1) {
		this.id1 = id1;
	}
	
	public void setId2(String id2) {
		this.id2 = id2;
	}
	
	
	public void setUdpServerPacket1(UDPServerPacket udpServerPacket1) {
		this.udpServerPacket1 = udpServerPacket1;
	}
	
	public void setUdpServerPacket2(UDPServerPacket udpServerPacket2) {
		this.udpServerPacket2 = udpServerPacket2;
	}
	
	public UDPServerPacket getUDPServerPacket(String id) {
		if( id1.equals( id ) ) {
			return udpServerPacket1 ;
		}
		if( id2.equals(id) ) {
			return udpServerPacket2 ;
		}
		return udpServerPacket1  ;
	}


    public String getId1() {
        return id1;
    }

    public UDPServerPacket getUdpServerPacket1() {
        return udpServerPacket1;
    }

    public String getId2() {
        return id2;
    }

    public UDPServerPacket getUdpServerPacket2() {
        return udpServerPacket2;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UDPServerPacket2{");
        sb.append("id1='").append(id1).append('\'');
        sb.append(", udpServerPacket1=").append(udpServerPacket1);
        sb.append(", id2='").append(id2).append('\'');
        sb.append(", udpServerPacket2=").append(udpServerPacket2);
        sb.append('}');
        return sb.toString();
    }
}
