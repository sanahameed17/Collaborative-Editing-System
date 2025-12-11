package com.collaborativeediting.versioncontrol;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VersionControlServiceTest {

    @Autowired
    private VersionControlService versionControlService;

    @Test
    public void testSaveVersion() {
        versionControlService.saveVersion(1L, "Content", "user");
        assertFalse(versionControlService.getVersionHistory(1L).isEmpty());
    }
}
