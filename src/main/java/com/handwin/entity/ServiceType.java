package com.handwin.entity;

/**
 * Created by fangliang on 10/12/14.
 */
public enum ServiceType {

    Login("Login"),
    LoginResponse("LoginResponse"),
    Logout("Logout"),
    Heartbeat("Heartbeat"),
    HeartbeatResponse("HeartbeatResponse"),
    Confirm("Confirm"),
    Text("Text"),
    Voice("Voice"),
    Video("Video"),
    Photo("Photo"),
    Contact("Contact"),
    Forward("Forward"),
    Call("Call"),
    Emoji("Emoji"),
    GroupText("Group-Text"),
    GroupPhoto("Group-Photo"),
    GroupVoice("Group-Voice"),
    GroupVideo("Group-Video"),
    GroupAddUser("Group-Add-User"),
    GroupRemoveUser("Group-Remove-User"),
    GroupCreate("Group-Create"),
    GroupDelete("Group-Delete"),


    ChatJoin("/v5/live/join"),
    ChatJoinResponse("/v5/live/join-response"),
    ChatMessage("/v5/live/message"),
    ChatMessageResponse("/v5/live/message-response"),
    ChatLeave("/v5/live/leave"),
    ChatLeaveResponse("/v5/live/leave-response"),

    ChatSysMessage("/v5/live/sysmessage");


    private String type;

    private ServiceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
