package com.worldlink.ruleseditor.model;

public record TemplateCell(
    int columnIndex,
    ColumnType type,
    String template
) {}
