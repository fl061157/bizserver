option java_package = "com.handwin.server.proto";
option java_outer_classname = "ServerProto";

message RequestMessage {
	optional string traceId = 1;
	optional string tcpServerId = 2;
	optional string tcpChannelUuid = 3;
	optional string userIp = 4;
	optional int32  userPort = 5;
	optional string userId = 6;
	optional int32  appId = 7;
	optional int32  tcpChannelId = 8;
	optional string sessionId = 9;
	optional bool   isLocalUser = 10;
	optional string zoneCode = 11;
	optional bytes  msgBody  = 12;
	optional int32  messageType = 13;
}


message ResponseMessage {
	repeated int32 actions = 1;
	optional string tcpChannelUuid = 2;
	optional string tcpChannelId = 3;
	optional string traceId = 4 ;
	optional string userId = 5;
	optional int32 appId = 6;
	optional string sessionId = 7;
	optional bool isLocalUser = 8;
	optional string zoneCode = 9;
	optional bytes msgBody = 10;
	
}