package com.handwin.packet;


public abstract class CommandPacket extends AbstractBasePacket{

    public final static byte COMMAND_PACKET_TYPE = 0x01;

    byte commandType;

    public byte getCommandType() {
        return commandType;
    }

    public void setCommandType(byte commandType) {
        this.commandType = commandType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommandPacket{");
        sb.append("commandType=").append(commandType);
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
