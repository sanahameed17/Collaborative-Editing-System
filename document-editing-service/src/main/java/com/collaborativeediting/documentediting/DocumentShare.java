package com.collaborativeediting.documentediting;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_shares")
public class DocumentShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private String sharedWithUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharePermission permission;

    @Column(nullable = false)
    private String sharedByUser;

    private LocalDateTime sharedAt;

    // Constructors
    public DocumentShare() {}

    public DocumentShare(Document document, String sharedWithUser, SharePermission permission, String sharedByUser) {
        this.document = document;
        this.sharedWithUser = sharedWithUser;
        this.permission = permission;
        this.sharedByUser = sharedByUser;
        this.sharedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public String getSharedWithUser() { return sharedWithUser; }
    public void setSharedWithUser(String sharedWithUser) { this.sharedWithUser = sharedWithUser; }

    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }

    public String getSharedByUser() { return sharedByUser; }
    public void setSharedByUser(String sharedByUser) { this.sharedByUser = sharedByUser; }

    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }
}
