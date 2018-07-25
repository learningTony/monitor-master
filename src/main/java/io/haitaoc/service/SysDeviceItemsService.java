package io.haitaoc.service;

import io.haitaoc.model.SysDeviceItems;

import java.time.LocalDateTime;
import java.util.List;

public interface SysDeviceItemsService {

    List<SysDeviceItems> findDeviceItemsBySysId(int sysId);

    List<SysDeviceItems> getDeviceItemsByIp(String device_ip);

    void insertBarch(List<SysDeviceItems> sysDeviceItems);

}
