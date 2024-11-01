package com.muchiri.vault_drop.service;

import java.util.List;

import com.muchiri.vault_drop.dto.FolderDTO;

public interface FolderService {
    
    FolderDTO create(FolderDTO folder);
    
    FolderDTO findById(String id);

    List<FolderDTO> findAllRootFolders();
}
