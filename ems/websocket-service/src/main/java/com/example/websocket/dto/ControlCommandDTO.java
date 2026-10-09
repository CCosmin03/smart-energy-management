package com.example.websocket.dto;

public class ControlCommandDTO {
    private String deviceId;
    private String command; // ex: "STOP"

    public ControlCommandDTO() {}

    public ControlCommandDTO(String deviceId, String command) {
        this.deviceId = deviceId;
        this.command = command;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
}
