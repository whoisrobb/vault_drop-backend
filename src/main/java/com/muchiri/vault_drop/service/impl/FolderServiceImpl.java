package com.muchiri.vault_drop.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.muchiri.vault_drop.domain.FileEntity;
import com.muchiri.vault_drop.dto.FileDTO;
import com.muchiri.vault_drop.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.muchiri.vault_drop.domain.FolderEntity;
import com.muchiri.vault_drop.dto.FolderDTO;
import com.muchiri.vault_drop.repository.FolderRepository;
import com.muchiri.vault_drop.service.FolderService;

@Service
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    @Autowired
    public FolderServiceImpl(final FolderRepository folderRepository, final FileRepository fileRepository) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
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


        Optional<List<FileEntity>> files = fileRepository.findFolderFiles(savedFolder.getId());

        Set<FileEntity> folderFiles = new HashSet<>(files.orElse(Collections.emptyList()));
        savedFolder.setFiles(folderFiles);

        return folderEntityToFolderDTO(savedFolder);
    }

    @Override
    public List<FolderDTO> findAllRootFolders() {
        List<FolderEntity> folders = folderRepository.findAllRootFolders();

        List<FolderDTO> rootFolders = folders.stream()
                .map(this::folderEntityToFolderDTO)
                .toList();

        for (FolderDTO each : rootFolders) {
            Set<FileDTO> folderFiles = getFolderFiles(each.getId()).stream()
                    .map(this::fileEntityToFileDTO)
                    .collect(Collectors.toSet());
            each.setFiles(folderFiles);
        }

        return rootFolders;
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

        Set<FileDTO> folderFiles = getFolderFiles(id).stream()
                .map(this::fileEntityToFileDTO)
                .collect(Collectors.toSet());

        parent.setFiles(folderFiles);

        parent.setChildren(childDTOs);

        return parent;
    }

    @Override
    public void deleteFolder(String id) {
        FolderEntity folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder does not exist"));

        folderRepository.delete(folder);
    }

    private FolderDTO folderEntityToFolderDTO(FolderEntity folder) {
        return FolderDTO.builder()
            .id(folder.getId())
            .name(folder.getName())
            .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
            .files(folder.getFiles().stream()
                    .map(this::fileEntityToFileDTO)
                    .collect(Collectors.toSet())
            )
            .createdAt(folder.getCreatedAt())
            .updatedAt(folder.getUpdatedAt())
            .children(new HashSet<FolderDTO>())
            .build();
    }

    private FolderEntity folderDtoToFolderEntity(FolderDTO folder) {
        return FolderEntity.builder()
            .id(folder.getId())
            .name(folder.getName())
            .build();
    }

    private FileDTO fileEntityToFileDTO(FileEntity file) {
//        String signedUrl = createPresignedGetUrl(file.getKey());

        return FileDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .key(file.getKey())
//                .signedUrl(signedUrl)
                .size(file.getSize())
                .type(file.getType())
                .createdAt(file.getCreatedAt())
                .build();
    }

    private FileEntity fileDTOtoFileEntity(FileDTO file, FolderEntity folder) {
        return FileEntity.builder()
                .fileName(file.getFileName())
                .key(file.getKey())
                .size(file.getSize())
                .type(file.getType())
                .folder(folder) // Associate with the folder
                .build();
    }

    private Set<FileEntity> getFolderFiles(String id) {
        Optional<List<FileEntity>> files = fileRepository.findFolderFiles(id);
        return  new HashSet<>(files.orElse(Collections.emptyList()));
    }

}
