package com.collaborativeediting.documentediting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    List<DocumentTemplate> findByCreatedBy(String createdBy);

    List<DocumentTemplate> findByIsPublicTrue();

    List<DocumentTemplate> findByCategory(String category);

    @Query("SELECT dt FROM DocumentTemplate dt WHERE dt.isPublic = true OR dt.createdBy = :username")
    List<DocumentTemplate> findAccessibleTemplates(@Param("username") String username);

    List<DocumentTemplate> findByCreatedByAndCategory(String createdBy, String category);
}
