package io.haitaoc.service.impl;

import io.haitaoc.dao.SysBusinessDao;
import io.haitaoc.dao.SysDeviceItemsDao;
import io.haitaoc.dao.WarnDao;
import io.haitaoc.model.SysBusiness;
import io.haitaoc.model.SysDeviceItems;
import io.haitaoc.model.Warn;
import io.haitaoc.service.WarnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WarnServiceImpl implements WarnService {

    @Autowired
    private WarnDao warnDao;

    @Autowired
    private SysDeviceItemsDao sysDeviceItemsDao;

    @Autowired
    private SysBusinessDao sysBusinessDao;

    @Override
    public Map<String, Integer> deviceWarnCounts() {
        List<Warn> warnDeviceList = warnDao.findDistinctIP();
        Map<String,Integer> deviceCountMap = new HashMap<>();
        for (Warn warn: warnDeviceList){
            String deviceIp = warn.getDeviceIP();
            int count = warnDao.warnCount(deviceIp);
            deviceCountMap.put(deviceIp,count);
        }
        return deviceCountMap;
    }

    @Override
    public Map<Integer, Integer> businessWarnCounts() {
        List<Warn> warnDeviceList = warnDao.findDistinctSysId();
        Map<Integer,Integer> countMap = new HashMap<>();
        for (Warn warn: warnDeviceList){
            int sysId = warn.getSysId();
            int count = warnDao.businessWarnCount(sysId);
            countMap.put(sysId,count);
        }
        return countMap;
    }

    @Override
    public List<Warn> findIpWarns(String deviceIp) {
        List<Warn> IpWarnList = warnDao.findIpWarns(deviceIp);
        return IpWarnList;
    }

    @Override
    public List<Warn> findIdWarns(int sysId) {
        List<Warn> IdWarnList = warnDao.findIdWarns(sysId);
        return IdWarnList;
    }

    @Override
    public List<Warn> findConfirmedIpWarns(String deviceIp) {
        List<Warn> confirmedIpWarns = warnDao.findConfirmedIpWarns(deviceIp);
        return confirmedIpWarns;
    }

    @Override
    public List<Warn> findConfirmedIdWarns(int sysId) {
        List<Warn> confirmedIdWarns = warnDao.findConfirmedIdWarns(sysId);
        return confirmedIdWarns;
    }

    /**
     * 1. 根据要更新的warn对应的ip,warn_type,find_time找到sys_device_item表中的记录
     *    更新告警内容
     *    插入新的更新过的sys_device_item记录, 未警告内容状态保持不变
     * 2. 根据warn的id更新对应记录为修复状态, 填入更新时间updateTime
     * @param id
     * @param fixType
     */
    @Override
    @Transactional
    public void updateConfirm(long id,String fixType) {
        Warn oldWarn = warnDao.findById(id);
        LocalDateTime now = LocalDateTime.now();
        SysDeviceItems sysDeviceItems = sysDeviceItemsDao.findOne(oldWarn.getDeviceIP(),oldWarn.getWarnType(),oldWarn.getFindTime());
        SysDeviceItems record = sysDeviceItems;
        record.setDateTime(now);
        sysDeviceItemsDao.insert(record);
        record = sysDeviceItemsDao.findOne(oldWarn.getDeviceIP(),oldWarn.getWarnType(),now);
        sysDeviceItemsDao.updateOne(record.getId(),oldWarn.getWarnType(),now);
        warnDao.updateConfirm(id,now,fixType);
    }

    @Override
    public void updateBusinessConfirm(long id, String fixType) {
        Warn oldWarn = warnDao.findById(id);
        LocalDateTime now = LocalDateTime.now();
        SysBusiness sysBusiness = sysBusinessDao.findOne(oldWarn.getSysId(),oldWarn.getWarnType(),oldWarn.getFindTime());
        SysBusiness record = sysBusiness;
        record.setScanTime(now);
        sysBusinessDao.insert(record);
        record = sysBusinessDao.findOne(oldWarn.getSysId(),oldWarn.getWarnType(),now);
        sysBusinessDao.updateOne(record.getId(),oldWarn.getWarnType(),now);
        warnDao.updateConfirm(id,now,fixType);
    }

    @Override
    @Transactional
    public void insertBatch(List<Warn> warns) {
        warnDao.insertBatch(warns);
    }
}
