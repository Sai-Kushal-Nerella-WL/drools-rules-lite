package com.worldlink.ruleseditor.model;

import java.util.List;

public record DecisionTableMeta(
    String ruleSet,
    List<String> importTypes,
    String ruleTableName
) {}
