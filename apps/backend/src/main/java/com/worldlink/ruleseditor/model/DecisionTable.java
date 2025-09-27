package com.worldlink.ruleseditor.model;

import java.util.List;

public record DecisionTable(
    DecisionTableMeta meta,
    List<ColumnType> headers,
    List<TemplateCell> templates,
    List<TableRow> rows
) {}
