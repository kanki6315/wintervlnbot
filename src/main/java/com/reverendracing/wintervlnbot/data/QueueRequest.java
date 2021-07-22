package com.reverendracing.wintervlnbot.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "queue_request")
public class QueueRequest {

    @Id
    private String id;

    private String carNumber;

    private long creationTimestamp;

    private String userMentionTag;

    private String driverName;

    public QueueRequest() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getUserMentionTag() {
        return userMentionTag;
    }

    public void setUserMentionTag(String userMentionTag) {
        this.userMentionTag = userMentionTag;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}
