package com.collaborativeediting.documentediting;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentExportService documentExportService;

    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody CreateDocumentRequest request, @RequestHeader("Authorization") String token) {
        String owner = extractOwnerFromToken(token);
        Document document = documentService.createDocument(request.getTitle(), request.getContent(), owner);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(document);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);

        if (!documentService.hasPermission(id, username, SharePermission.READ)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Document> document = documentService.getDocument(id);
        return document.map(doc -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(doc))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Document>> getDocuments(@RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        List<Document> documents = documentService.getAllAccessibleDocuments(username);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(documents);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, @RequestBody UpdateDocumentRequest request, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        Document document = documentService.updateDocument(id, request.getContent(), username);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(document);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        documentService.deleteDocument(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<DocumentShare> shareDocument(@PathVariable Long id, @RequestBody ShareDocumentRequest request, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        DocumentShare share = documentService.shareDocument(id, request.getSharedWithUser(), request.getPermission(), username);
        return ResponseEntity.ok(share);
    }

    @DeleteMapping("/{id}/share/{sharedWithUser}")
    public ResponseEntity<Void> revokeShare(@PathVariable Long id, @PathVariable String sharedWithUser, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        documentService.revokeShare(id, sharedWithUser, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/shares")
    public ResponseEntity<List<DocumentShare>> getDocumentShares(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        List<DocumentShare> shares = documentService.getDocumentShares(id, username);
        return ResponseEntity.ok(shares);
    }

    @GetMapping("/{id}/permission")
    public ResponseEntity<SharePermission> getUserPermission(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        SharePermission permission = documentService.getUserPermission(id, username);
        return ResponseEntity.ok(permission);
    }

    // Template Endpoints
    @PostMapping("/templates")
    public ResponseEntity<DocumentTemplate> createTemplate(@RequestBody CreateTemplateRequest request, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        DocumentTemplate template = documentService.createTemplate(
                request.getName(),
                request.getDescription(),
                request.getContent(),
                request.getCategory(),
                username
        );
        return ResponseEntity.ok(template);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<DocumentTemplate>> getTemplates(@RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        List<DocumentTemplate> templates = documentService.getAllTemplates(username);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/category/{category}")
    public ResponseEntity<List<DocumentTemplate>> getTemplatesByCategory(@PathVariable String category, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        List<DocumentTemplate> templates = documentService.getTemplatesByCategory(category, username);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<DocumentTemplate> getTemplate(@PathVariable Long id) {
        Optional<DocumentTemplate> template = documentService.getTemplate(id);
        return template.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<DocumentTemplate> updateTemplate(@PathVariable Long id, @RequestBody UpdateTemplateRequest request, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        DocumentTemplate template = documentService.updateTemplate(
                id,
                request.getName(),
                request.getDescription(),
                request.getContent(),
                request.getCategory(),
                username
        );
        return ResponseEntity.ok(template);
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        documentService.deleteTemplate(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/templates/{id}/create-document")
    public ResponseEntity<Document> createDocumentFromTemplate(@PathVariable Long id, @RequestBody CreateDocumentFromTemplateRequest request, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);
        Document document = documentService.createDocumentFromTemplate(id, request.getTitle(), username);
        return ResponseEntity.ok(document);
    }

    // Export Endpoints
    @GetMapping("/{id}/export/{format}")
    public ResponseEntity<byte[]> exportDocument(@PathVariable Long id, @PathVariable String format, @RequestHeader("Authorization") String token) {
        String username = extractOwnerFromToken(token);

        if (!documentService.hasPermission(id, username, SharePermission.READ)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Document> documentOpt = documentService.getDocument(id);
        if (!documentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Document document = documentOpt.get();
        DocumentExportService.ExportFormat exportFormat;

        try {
            exportFormat = DocumentExportService.ExportFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        byte[] exportedData = documentExportService.exportDocument(document, exportFormat);
        String contentType = documentExportService.getContentType(exportFormat);
        String fileName = document.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + documentExportService.getFileExtension(exportFormat);

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(exportedData);
    }

    private String extractOwnerFromToken(String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Claims claims = Jwts.parser()
                    .setSigningKey("mySecretKeyForJwtTokenGenerationWhichIsLongEnough") // Same key as in UserManagementService
                    .parseClaimsJws(jwtToken)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }

    // DTOs
    public static class CreateDocumentRequest {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class UpdateDocumentRequest {
        private String content;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class ShareDocumentRequest {
        private String sharedWithUser;
        private SharePermission permission;

        public String getSharedWithUser() { return sharedWithUser; }
        public void setSharedWithUser(String sharedWithUser) { this.sharedWithUser = sharedWithUser; }

        public SharePermission getPermission() { return permission; }
        public void setPermission(SharePermission permission) { this.permission = permission; }
    }

    public static class CreateTemplateRequest {
        private String name;
        private String description;
        private String content;
        private String category;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public static class UpdateTemplateRequest {
        private String name;
        private String description;
        private String content;
        private String category;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public static class CreateDocumentFromTemplateRequest {
        private String title;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
