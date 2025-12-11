package com.collaborativeediting.versioncontrol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
public class VersionControlController {

    @Autowired
    private VersionControlService versionControlService;

    @PostMapping("/save")
    public ResponseEntity<Void> saveVersion(@RequestBody SaveVersionRequest request) {
        versionControlService.saveVersion(request.getDocumentId(), request.getContent(), request.getEditedBy());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history/{documentId}")
    public ResponseEntity<List<DocumentVersion>> getVersionHistory(@PathVariable Long documentId) {
        List<DocumentVersion> history = versionControlService.getVersionHistory(documentId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/revert/{versionId}")
    public ResponseEntity<DocumentVersion> revertToVersion(@PathVariable Long versionId) {
        DocumentVersion version = versionControlService.revertToVersion(versionId);
        return ResponseEntity.ok(version);
    }

    @GetMapping("/contributions/{user}")
    public ResponseEntity<List<DocumentVersion>> getContributions(@PathVariable String user) {
        List<DocumentVersion> contributions = versionControlService.getContributionsByUser(user);
        return ResponseEntity.ok(contributions);
    }

    // DTO
    public static class SaveVersionRequest {
        private Long documentId;
        private String content;
        private String editedBy;

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getEditedBy() { return editedBy; }
        public void setEditedBy(String editedBy) { this.editedBy = editedBy; }
    }
}
