package com.example.device.dtos.builders;

import com.example.device.dtos.DeviceDTO;
import com.example.device.dtos.DeviceDetailsDTO;
import com.example.device.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {}

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(device.getId(), device.getName(), device.getStatus(), device.getUserId());
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(
                device.getId(),
                device.getName(),
                device.getStatus(),
                device.getUserId()
        );
    }

    public static Device toEntity(DeviceDetailsDTO dto) {
        return new Device(dto.getName(), dto.getStatus(), dto.getUserId());
    }
}
