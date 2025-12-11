package com.collaborativeediting.versioncontrol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VersionControlService {

    @Autowired
    private DocumentVersionRepository versionRepository;

    public void saveVersion(Long documentId, String content, String editedBy) {
        DocumentVersion version = new DocumentVersion(documentId, content, editedBy);
        versionRepository.save(version);
    }

    public List<DocumentVersion> getVersionHistory(Long documentId) {
        return versionRepository.findByDocumentIdOrderByTimestampDesc(documentId);
    }

    public DocumentVersion revertToVersion(Long versionId) {
        DocumentVersion version = versionRepository.findById(versionId).orElseThrow(() -> new RuntimeException("Version not found"));
        // In real app, update the document service via REST
        return version;
    }

    public List<DocumentVersion> getContributionsByUser(String user) {
        // For simplicity, return all versions by user
        return versionRepository.findAll().stream().filter(v -> v.getEditedBy().equals(user)).collect(Collectors.toList());
    }
}
