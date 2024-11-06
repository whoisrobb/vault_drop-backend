package com.muchiri.vault_drop.service;

import com.muchiri.vault_drop.dto.FileDTO;
import com.muchiri.vault_drop.dto.FolderDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    List<FileDTO> create(
        final FolderDTO parent,
        List<MultipartFile> files
    );

    List<FileDTO> findFolderFiles(String folderId);
    
}
