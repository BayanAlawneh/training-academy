package com.academy.tms.dto;

import com.academy.tms.entities.Notification;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String title;
    private String body;
    private String link;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse(Long id, String title, String body, String link,
                                String type, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.link = link;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
    }

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getTitle(), n.getBody(),
                n.getLink(), n.getType().name(), n.isRead(), n.getCreatedAt());
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getLink() { return link; }
    public String getType() { return type; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
