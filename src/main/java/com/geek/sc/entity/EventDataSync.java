package com.geek.sc.entity;

import java.sql.Timestamp;
import java.util.UUID;

public class EventDataSync {
    private UUID eventId;

    private String eventType;

    private UUID userId;

    private String sourceService;

    private String destinationService;

    private String featureKey;

    private String content;

    private String acceptStatus;

    private String processStatus;

    private Timestamp createTime;

    private Timestamp updateTime;

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType == null ? null : eventType.trim();
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService == null ? null : sourceService.trim();
    }

    public String getDestinationService() {
        return destinationService;
    }

    public void setDestinationService(String destinationService) {
        this.destinationService = destinationService == null ? null : destinationService.trim();
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey == null ? null : featureKey.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getAcceptStatus() {
        return acceptStatus;
    }

    public void setAcceptStatus(String acceptStatus) {
        this.acceptStatus = acceptStatus == null ? null : acceptStatus.trim();
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus == null ? null : processStatus.trim();
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}