package com.collaborativeediting.documentediting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentShareRepository documentShareRepository;

    @Autowired
    private DocumentTemplateRepository documentTemplateRepository;

    public Document createDocument(String title, String content, String owner) {
        Document document = new Document(title, content, owner);
        return documentRepository.save(document);
    }

    public Optional<Document> getDocument(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> getDocumentsByOwner(String owner) {
        return documentRepository.findByOwner(owner);
    }

    public List<Document> getSharedDocuments(String username) {
        List<DocumentShare> shares = documentShareRepository.findBySharedWithUser(username);
        return shares.stream()
                .map(DocumentShare::getDocument)
                .collect(Collectors.toList());
    }

    public List<Document> getAllAccessibleDocuments(String username) {
        List<Document> ownedDocuments = getDocumentsByOwner(username);
        List<Document> sharedDocuments = getSharedDocuments(username);
        ownedDocuments.addAll(sharedDocuments);
        return ownedDocuments.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public Document updateDocument(Long id, String content, String username) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));

        if (!hasPermission(id, username, SharePermission.WRITE)) {
            throw new RuntimeException("Unauthorized");
        }

        document.setContent(content);
        document.setUpdatedAt(java.time.LocalDateTime.now());
        return documentRepository.save(document);
    }

    public void deleteDocument(Long id, String username) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));

        if (!hasPermission(id, username, SharePermission.ADMIN) && !document.getOwner().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        documentRepository.delete(document);
    }

    public DocumentShare shareDocument(Long documentId, String sharedWithUser, SharePermission permission, String sharedByUser) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOwner().equals(sharedByUser)) {
            throw new RuntimeException("Only document owner can share");
        }

        // Check if already shared
        Optional<DocumentShare> existingShare = documentShareRepository.findByDocumentIdAndSharedWithUser(documentId, sharedWithUser);
        if (existingShare.isPresent()) {
            throw new RuntimeException("Document already shared with this user");
        }

        DocumentShare share = new DocumentShare(document, sharedWithUser, permission, sharedByUser);
        return documentShareRepository.save(share);
    }

    public void revokeShare(Long documentId, String sharedWithUser, String requestingUser) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOwner().equals(requestingUser)) {
            throw new RuntimeException("Only document owner can revoke sharing");
        }

        documentShareRepository.deleteByDocumentIdAndSharedWithUser(documentId, sharedWithUser);
    }

    public List<DocumentShare> getDocumentShares(Long documentId, String requestingUser) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOwner().equals(requestingUser)) {
            throw new RuntimeException("Unauthorized");
        }

        return documentShareRepository.findByDocumentId(documentId);
    }

    public boolean hasPermission(Long documentId, String username, SharePermission requiredPermission) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found"));

        // Owner has all permissions
        if (document.getOwner().equals(username)) {
            return true;
        }

        // Check sharing permissions
        Optional<DocumentShare> share = documentShareRepository.findByDocumentIdAndSharedWithUser(documentId, username);
        if (share.isPresent()) {
            SharePermission userPermission = share.get().getPermission();
            return hasRequiredPermission(userPermission, requiredPermission);
        }

        return false;
    }

    private boolean hasRequiredPermission(SharePermission userPermission, SharePermission requiredPermission) {
        switch (requiredPermission) {
            case READ:
                return userPermission == SharePermission.READ ||
                       userPermission == SharePermission.WRITE ||
                       userPermission == SharePermission.ADMIN;
            case WRITE:
                return userPermission == SharePermission.WRITE ||
                       userPermission == SharePermission.ADMIN;
            case ADMIN:
                return userPermission == SharePermission.ADMIN;
            default:
                return false;
        }
    }

    public SharePermission getUserPermission(Long documentId, String username) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found"));

        // Owner has admin permission
        if (document.getOwner().equals(username)) {
            return SharePermission.ADMIN;
        }

        // Check sharing permissions
        Optional<DocumentShare> share = documentShareRepository.findByDocumentIdAndSharedWithUser(documentId, username);
        return share.map(DocumentShare::getPermission).orElse(null);
    }

    // Template Management Methods
    public DocumentTemplate createTemplate(String name, String description, String content, String category, String createdBy) {
        DocumentTemplate template = new DocumentTemplate(name, description, content, category, createdBy);
        return documentTemplateRepository.save(template);
    }

    public List<DocumentTemplate> getAllTemplates(String username) {
        return documentTemplateRepository.findAccessibleTemplates(username);
    }

    public List<DocumentTemplate> getTemplatesByCategory(String category, String username) {
        List<DocumentTemplate> publicTemplates = documentTemplateRepository.findByCategory(category);
        List<DocumentTemplate> userTemplates = documentTemplateRepository.findByCreatedByAndCategory(username, category);

        publicTemplates.addAll(userTemplates);
        return publicTemplates.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public Optional<DocumentTemplate> getTemplate(Long id) {
        return documentTemplateRepository.findById(id);
    }

    public DocumentTemplate updateTemplate(Long id, String name, String description, String content, String category, String username) {
        DocumentTemplate template = documentTemplateRepository.findById(id).orElseThrow(() -> new RuntimeException("Template not found"));

        if (!template.getCreatedBy().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        template.setName(name);
        template.setDescription(description);
        template.setContent(content);
        template.setCategory(category);
        template.setUpdatedAt(java.time.LocalDateTime.now());

        return documentTemplateRepository.save(template);
    }

    public void deleteTemplate(Long id, String username) {
        DocumentTemplate template = documentTemplateRepository.findById(id).orElseThrow(() -> new RuntimeException("Template not found"));

        if (!template.getCreatedBy().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        documentTemplateRepository.delete(template);
    }

    public Document createDocumentFromTemplate(Long templateId, String title, String owner) {
        DocumentTemplate template = documentTemplateRepository.findById(templateId).orElseThrow(() -> new RuntimeException("Template not found"));
        Document document = new Document(title, template.getContent(), owner);
        return documentRepository.save(document);
    }
}
