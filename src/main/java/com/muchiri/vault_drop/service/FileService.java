package com.muchiri.vault_drop.service;

import com.muchiri.vault_drop.dto.FileDTO;
import com.muchiri.vault_drop.dto.FolderDTO;

import java.util.List;

public interface FileService {

    FileDTO create(
        final FolderDTO parent,
        final String fileName,
        final Long size,
        final String contentType,
        byte[] fileContent
    );

    List<FileDTO> findFolderFiles(String folderId);
    
}
