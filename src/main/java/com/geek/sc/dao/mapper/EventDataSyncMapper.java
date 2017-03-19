package com.geek.sc.dao.mapper;

import com.geek.sc.entity.EventDataSync;
import java.util.UUID;

public interface EventDataSyncMapper {
    int deleteByPrimaryKey(UUID eventId);

    int insert(EventDataSync record);

    int insertSelective(EventDataSync record);

    EventDataSync selectByPrimaryKey(UUID eventId);

    int updateByPrimaryKeySelective(EventDataSync record);

    int updateByPrimaryKey(EventDataSync record);
}