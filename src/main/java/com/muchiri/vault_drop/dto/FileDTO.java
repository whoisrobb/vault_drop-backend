package com.muchiri.vault_drop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDTO {
    
    private String id;
    private String fileName;
    private String type;
    private String key;
    private Long size;
    // private FolderEntity folder;

}
