package com.worldlink.ruleseditor.model;

import java.util.List;

public record TableRow(
    String name,
    List<Object> values
) {}
