package com.worldlink.ruleseditor.service;

import com.worldlink.ruleseditor.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ExcelService {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile(".*[><=]+.*\\$param.*");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile(".*==.*\\$param.*");

    public DecisionTable read(Path path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            int ruleTableRowIndex = findRuleTableRow(sheet);
            if (ruleTableRowIndex == -1) {
                throw new IllegalArgumentException("RuleTable row not found");
            }
            
            List<List<Object>> metaRows = new ArrayList<>();
            for (int i = 0; i < ruleTableRowIndex; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    metaRows.add(getRowValues(row));
                }
            }
            MetaBlock metaBlock = new MetaBlock(metaRows);
            
            Row headerRow = sheet.getRow(ruleTableRowIndex + 1);
            if (headerRow == null) {
                throw new IllegalArgumentException("Header row not found");
            }
            
            List<ColumnType> headers = parseHeaders(headerRow);
            
            Row templateRow = sheet.getRow(ruleTableRowIndex + 2);
            if (templateRow == null) {
                throw new IllegalArgumentException("Template row not found");
            }
            
            List<TemplateCell> templates = parseTemplates(templateRow, headers);
            
            List<TableRow> rows = new ArrayList<>();
            for (int i = ruleTableRowIndex + 3; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow != null && !isRowEmpty(dataRow)) {
                    rows.add(parseDataRow(dataRow, headers.size()));
                }
            }
            
            DecisionTableMeta meta = createMetaFromBlock(metaBlock);
            
            return new DecisionTable(meta, headers, templates, rows);
        }
    }
    
    public void write(DecisionTable table, MetaBlock metaBlock, Path path) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Rules");
            
            int rowIndex = 0;
            
            for (List<Object> metaRow : metaBlock.rows()) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < metaRow.size(); i++) {
                    Cell cell = row.createCell(i);
                    setCellValue(cell, metaRow.get(i));
                }
            }
            
            Row ruleTableRow = sheet.createRow(rowIndex++);
            ruleTableRow.createCell(0).setCellValue("RuleTable");
            
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < table.headers().size(); i++) {
                headerRow.createCell(i).setCellValue(table.headers().get(i).name());
            }
            
            Row templateRow = sheet.createRow(rowIndex++);
            for (TemplateCell template : table.templates()) {
                templateRow.createCell(template.columnIndex()).setCellValue(template.template());
            }
            
            for (TableRow tableRow : table.rows()) {
                Row dataRow = sheet.createRow(rowIndex++);
                dataRow.createCell(0).setCellValue(tableRow.name());
                for (int i = 0; i < tableRow.values().size(); i++) {
                    Cell cell = dataRow.createCell(i + 1);
                    setCellValue(cell, tableRow.values().get(i));
                }
            }
            
            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                workbook.write(fos);
            }
        }
    }
    
    private int findRuleTableRow(Sheet sheet) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell firstCell = row.getCell(0);
                if (firstCell != null && 
                    "RuleTable".equalsIgnoreCase(getCellValueAsString(firstCell))) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private List<ColumnType> parseHeaders(Row row) {
        List<ColumnType> headers = new ArrayList<>();
        for (Cell cell : row) {
            String value = getCellValueAsString(cell).toUpperCase();
            try {
                headers.add(ColumnType.valueOf(value));
            } catch (IllegalArgumentException e) {
            }
        }
        return headers;
    }
    
    private List<TemplateCell> parseTemplates(Row row, List<ColumnType> headers) {
        List<TemplateCell> templates = new ArrayList<>();
        for (int i = 0; i < headers.size() && i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                String template = getCellValueAsString(cell);
                if (!template.trim().isEmpty() && 
                    (headers.get(i) == ColumnType.CONDITION || headers.get(i) == ColumnType.ACTION) &&
                    template.contains("$param")) {
                    templates.add(new TemplateCell(i, headers.get(i), template));
                }
            }
        }
        return templates;
    }
    
    private TableRow parseDataRow(Row row, int expectedColumns) {
        String name = "";
        Cell firstCell = row.getCell(0);
        if (firstCell != null) {
            name = getCellValueAsString(firstCell);
        }
        
        List<Object> values = new ArrayList<>();
        for (int i = 1; i < expectedColumns; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                values.add(getCellValue(cell));
            } else {
                values.add(null);
            }
        }
        
        return new TableRow(name, values);
    }
    
    private DecisionTableMeta createMetaFromBlock(MetaBlock metaBlock) {
        String ruleSet = "DefaultRuleSet";
        List<String> importTypes = new ArrayList<>();
        String ruleTableName = "DiscountRules";
        
        for (List<Object> row : metaBlock.rows()) {
            if (!row.isEmpty() && row.get(0) != null) {
                String firstCell = row.get(0).toString();
                if (firstCell.toLowerCase().contains("ruleset")) {
                    if (row.size() > 1 && row.get(1) != null) {
                        ruleSet = row.get(1).toString();
                    }
                } else if (firstCell.toLowerCase().contains("import")) {
                    if (row.size() > 1 && row.get(1) != null) {
                        importTypes.add(row.get(1).toString());
                    }
                }
            }
        }
        
        return new DecisionTableMeta(ruleSet, importTypes, ruleTableName);
    }
    
    private List<Object> getRowValues(Row row) {
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            values.add(cell != null ? getCellValue(cell) : null);
        }
        return values;
    }
    
    private Object getCellValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return (long) numValue;
                    }
                    return numValue;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        Object value = getCellValue(cell);
        return value != null ? value.toString() : "";
    }
    
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
    
    public CellType inferCellType(String template) {
        if (template == null || template.trim().isEmpty()) {
            return CellType.STRING;
        }
        
        if (NUMERIC_PATTERN.matcher(template).matches()) {
            return CellType.NUMERIC;
        } else if (BOOLEAN_PATTERN.matcher(template).matches()) {
            return CellType.BOOLEAN;
        } else {
            return CellType.STRING;
        }
    }
}
