package com.dealerhub.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PhotoResponse {
    private UUID id;
    private String url;
    private boolean primary;
    private int displayOrder;
    private Instant uploadedAt;
}
