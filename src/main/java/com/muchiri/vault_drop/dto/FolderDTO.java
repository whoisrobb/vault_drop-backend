package com.muchiri.vault_drop.dto;

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

    @Builder.Default
    private Set<FileEntity> files = new HashSet<>();

}
