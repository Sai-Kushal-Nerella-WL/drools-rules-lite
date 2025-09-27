package com.worldlink.ruleseditor;

import com.worldlink.ruleseditor.model.DecisionTable;
import com.worldlink.ruleseditor.model.MetaBlock;
import com.worldlink.ruleseditor.service.ExcelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RoundTripTest {

    @Autowired
    private ExcelService excelService;

    @TempDir
    Path tempDir;

    @Test
    public void testRoundTrip() throws Exception {
        Path originalFile = Path.of("../../rules/DiscountRules.xlsx");
        if (!originalFile.toFile().exists()) {
            return;
        }
        
        DecisionTable originalTable = excelService.read(originalFile);
        
        Path tempFile = tempDir.resolve("test.xlsx");
        MetaBlock emptyMeta = new MetaBlock(List.of());
        excelService.write(originalTable, emptyMeta, tempFile);
        
        DecisionTable roundTripTable = excelService.read(tempFile);
        
        assertEquals(originalTable.headers().size(), roundTripTable.headers().size());
        for (int i = 0; i < originalTable.headers().size(); i++) {
            assertEquals(originalTable.headers().get(i), roundTripTable.headers().get(i));
        }
        
        assertEquals(originalTable.templates().size(), roundTripTable.templates().size());
        
        assertEquals(originalTable.rows().size(), roundTripTable.rows().size());
    }
}
