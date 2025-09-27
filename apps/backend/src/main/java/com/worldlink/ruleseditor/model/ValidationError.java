package com.worldlink.ruleseditor.model;

public record ValidationError(
    Integer row,
    Integer col,
    String message
) {}
