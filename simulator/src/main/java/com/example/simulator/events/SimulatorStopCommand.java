package com.example.simulator.events;

public class SimulatorStopCommand {
    private String deviceId;
    private String command;

    public SimulatorStopCommand() {}

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
}
