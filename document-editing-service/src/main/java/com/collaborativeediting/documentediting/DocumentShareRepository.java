package com.collaborativeediting.documentediting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentShareRepository extends JpaRepository<DocumentShare, Long> {

    List<DocumentShare> findByDocumentId(Long documentId);

    List<DocumentShare> findBySharedWithUser(String sharedWithUser);

    Optional<DocumentShare> findByDocumentIdAndSharedWithUser(Long documentId, String sharedWithUser);

    @Query("SELECT ds FROM DocumentShare ds WHERE ds.document.owner = :owner AND ds.sharedWithUser = :sharedWithUser")
    List<DocumentShare> findSharedDocumentsByOwnerAndUser(@Param("owner") String owner, @Param("sharedWithUser") String sharedWithUser);

    void deleteByDocumentIdAndSharedWithUser(Long documentId, String sharedWithUser);
}
