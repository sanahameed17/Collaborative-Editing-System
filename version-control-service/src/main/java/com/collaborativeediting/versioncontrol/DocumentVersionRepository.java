package com.collaborativeediting.versioncontrol;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentIdOrderByTimestampDesc(Long documentId);
}
