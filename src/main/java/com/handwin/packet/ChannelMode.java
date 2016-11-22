package com.handwin.packet;

public enum ChannelMode {
	
	 FOREGROUND((byte) 0x01),
	 BACKGROUND((byte) 0x02),
	 SUSPEND((byte) 0x03),
	 UNKNOWN((byte) 0x99);
	 private byte value;

     ChannelMode(byte value) {
        this.value = value;
     }

	    public static ChannelMode getInstance(byte value) {
	        switch (value) {
	            case 1:
	                return FOREGROUND;
	            case 2:
	                return BACKGROUND;
	            case 3:
	                return SUSPEND;
	            default:
	                return UNKNOWN;
	        }
	    }
	    
	    
	    public byte getValue() {
			return value;
		}

	    @Override
	    public String toString() {
	        return "ChannelMode{" +
	                "value=" + value +
	                "} " + super.toString();
	    }
	

}
