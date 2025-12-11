package com.collaborativeediting.versioncontrol;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_versions")
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String editedBy;

    private LocalDateTime timestamp;

    // Constructors
    public DocumentVersion() {}

    public DocumentVersion(Long documentId, String content, String editedBy) {
        this.documentId = documentId;
        this.content = content;
        this.editedBy = editedBy;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getEditedBy() { return editedBy; }
    public void setEditedBy(String editedBy) { this.editedBy = editedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
