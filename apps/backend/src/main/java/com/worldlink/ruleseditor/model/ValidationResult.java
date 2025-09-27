package com.worldlink.ruleseditor.model;

import java.util.List;

public record ValidationResult(
    boolean ok,
    List<ValidationError> errors
) {}
