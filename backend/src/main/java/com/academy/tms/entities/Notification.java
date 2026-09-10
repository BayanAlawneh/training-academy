package com.academy.tms.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * إشعار داخل النظام لمستخدم واحد.
 *
 * اخترنا الإشعار داخل النظام قبل البريد لسببين: يعمل بلا اتصال ولا إعداد
 * SMTP فلا يفشل أثناء العرض، وهو نفس الطبقة التي سيستدعيها البريد لاحقاً
 * إن أُضيف — فلا يُعاد بناء شيء.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "body", length = 600)
    private String body;

    /** مسار داخل الواجهة يفتحه الضغط على الإشعار، مثل /trainee/exams */
    @Column(name = "link", length = 300)
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Notification() {
    }

    public Notification(User user, String title, String body, String link, NotificationType type) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.link = link;
        this.type = type;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getLink() { return link; }
    public NotificationType getType() { return type; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
