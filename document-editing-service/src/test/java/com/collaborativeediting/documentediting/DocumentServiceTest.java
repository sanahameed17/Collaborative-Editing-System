package com.collaborativeediting.documentediting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    @Test
    public void testCreateDocument() {
        Document doc = documentService.createDocument("Test Doc", "Content", "user");
        assertNotNull(doc);
        assertEquals("Test Doc", doc.getTitle());
    }

    @Test
    public void testGetDocument() {
        Document doc = documentService.createDocument("Test Doc2", "Content", "user");
        assertTrue(documentService.getDocument(doc.getId()).isPresent());
    }
}
