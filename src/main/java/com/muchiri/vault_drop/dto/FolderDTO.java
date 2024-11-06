package com.muchiri.vault_drop.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.muchiri.vault_drop.domain.FileEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FolderDTO {
    
    private String id;
    private String name;
    private String parentId;
    private Set<FolderDTO> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private Set<FileDTO> files = new HashSet<>();

}
