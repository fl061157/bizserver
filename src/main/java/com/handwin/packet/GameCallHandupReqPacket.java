package com.handwin.packet;

/**
 * Created by piguangtao on 2014/6/25.
 */
public class GameCallHandupReqPacket extends GameCallReqPacket {
    private DesCode desCode;

    public enum DesCode {

        /**
         * 未接通挂断
         */
        UN_ACCEPT((byte) 0x01),

        /**
         * 通话中挂断
         */
        ACCEPTED((byte) 0x02);

        byte value;

        DesCode(byte value) {
            this.value = value;
        }


        public static DesCode formDesCode(byte value){
            switch (value){
                case 0x01 : return UN_ACCEPT;
                case 0x02: return ACCEPTED;
                default:return null;
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("DesCode{");
            sb.append("value=").append(value);
            sb.append('}');
            return sb.toString();
        }
    }

    public DesCode getDesCode() {
        return desCode;
    }

    public void setDesCode(DesCode desCode) {
        this.desCode = desCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameCallHandupReqPacket{");
        sb.append("desCode=").append(desCode);
        sb.append('}');
        return sb.toString();
    }
}
