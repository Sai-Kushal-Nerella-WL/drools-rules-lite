package com.worldlink.ruleseditor.service;

import com.worldlink.ruleseditor.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ValidateService {

    public ValidationResult validate(DecisionTable table) {
        List<ValidationError> errors = new ArrayList<>();
        
        long conditionCount = table.headers().stream()
            .filter(header -> header == ColumnType.CONDITION)
            .count();
        long actionCount = table.headers().stream()
            .filter(header -> header == ColumnType.ACTION)
            .count();
            
        if (conditionCount == 0) {
            errors.add(new ValidationError(null, null, "At least one CONDITION column is required"));
        }
        
        if (actionCount == 0) {
            errors.add(new ValidationError(null, null, "At least one ACTION column is required"));
        }
        
        for (TemplateCell template : table.templates()) {
            if ((template.type() == ColumnType.CONDITION || template.type() == ColumnType.ACTION) &&
                !template.template().contains("$param")) {
                errors.add(new ValidationError(null, template.columnIndex(), 
                    "Template for " + template.type() + " column must contain '$param'"));
            }
        }
        
        for (int rowIndex = 0; rowIndex < table.rows().size(); rowIndex++) {
            TableRow row = table.rows().get(rowIndex);
            validateRowData(row, table.templates(), table.headers(), rowIndex, errors);
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    private void validateRowData(TableRow row, List<TemplateCell> templates, 
                                List<ColumnType> headers, int rowIndex, List<ValidationError> errors) {
        
        for (int colIndex = 0; colIndex < row.values().size() && colIndex < headers.size(); colIndex++) {
            Object value = row.values().get(colIndex);
            ColumnType columnType = headers.get(colIndex);
            
            final int finalColIndex = colIndex;
            TemplateCell template = templates.stream()
                .filter(t -> t.columnIndex() == finalColIndex)
                .findFirst()
                .orElse(null);
                
            if (template != null && value != null) {
                validateCellValue(value, template, rowIndex, colIndex, errors);
            }
        }
    }
    
    private void validateCellValue(Object value, TemplateCell template, 
                                  int rowIndex, int colIndex, List<ValidationError> errors) {
        
        String templateStr = template.template();
        
        if (templateStr.matches(".*[><=]+.*\\$param.*")) {
            if (!(value instanceof Number)) {
                try {
                    Double.parseDouble(value.toString());
                } catch (NumberFormatException e) {
                    errors.add(new ValidationError(rowIndex, colIndex, 
                        "Numeric value expected for template: " + templateStr));
                }
            }
        }
        
        else if (templateStr.matches(".*==.*\\$param.*") && 
                 (templateStr.toLowerCase().contains("true") || templateStr.toLowerCase().contains("false"))) {
            if (!(value instanceof Boolean)) {
                String valueStr = value.toString().toLowerCase();
                if (!valueStr.equals("true") && !valueStr.equals("false")) {
                    errors.add(new ValidationError(rowIndex, colIndex, 
                        "Boolean value (true/false) expected for template: " + templateStr));
                }
            }
        }
    }
}
