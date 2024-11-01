package com.muchiri.vault_drop.service.impl;

import java.util.stream.Collectors;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.muchiri.vault_drop.domain.FolderEntity;
import com.muchiri.vault_drop.dto.FolderDTO;
import com.muchiri.vault_drop.repository.FolderRepository;
import com.muchiri.vault_drop.service.FolderService;

@Service
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;

    @Autowired
    public FolderServiceImpl(final FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @Override
    public FolderDTO create(FolderDTO folder) {
        final FolderEntity folderEntity = folderDtoToFolderEntity(folder);

        if (folder.getParentId() != null) {
            FolderEntity parentFolder = folderRepository.findById(folder.getParentId())
                .orElseThrow(() -> new RuntimeException("Folder does not exist"));

            folderEntity.setParent(parentFolder);
        }

        final FolderEntity savedFolder = folderRepository.save(folderEntity);
        return folderEntityToFolderDTO(savedFolder);
    }

    @Override
    public List<FolderDTO> findAllRootFolders() {
        List<FolderEntity> folders = folderRepository.findAllRootFolders();
            
        return folders.stream()
            .map(this::folderEntityToFolderDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public FolderDTO findById(String id) {
        FolderEntity folder = folderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Folder does not exist"));

        List<FolderEntity> childFolders = folderRepository.findChildrenById(id);

        FolderDTO parent = folderEntityToFolderDTO(folder);
        Set<FolderDTO> childDTOs = childFolders.stream()
            .map(this::folderEntityToFolderDTO)
            .collect(Collectors.toSet());

        parent.setChildren(childDTOs);

        return parent;
    }

    private FolderDTO folderEntityToFolderDTO(FolderEntity folder) {
        return FolderDTO.builder()
            .id(folder.getId())
            .name(folder.getName())
            .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
            .files(folder.getFiles())
            .children(new HashSet<FolderDTO>())
            .build();
    }

    private FolderEntity folderDtoToFolderEntity(FolderDTO folder) {
        return FolderEntity.builder()
            .id(folder.getId())
            .name(folder.getName())
            .files(folder.getFiles())
            .build();
    }

}
