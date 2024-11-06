package com.muchiri.vault_drop.controllers;

import java.util.List;
import java.util.Set;

import com.muchiri.vault_drop.dto.FileDTO;
import com.muchiri.vault_drop.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.muchiri.vault_drop.dto.FolderDTO;
import com.muchiri.vault_drop.service.FolderService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/folders")
public class FolderController {

    private final FolderService folderService;
    private final FileService fileService;

    public FolderController(final FolderService folderService, final FileService fileService) {
        this.folderService = folderService;
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<List<FolderDTO>> getAllFolders() {
        return new ResponseEntity<>(folderService.findAllRootFolders(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FolderDTO> getFolderById(@PathVariable String id) {
        return new ResponseEntity<>(folderService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<FolderDTO> createFolder(
            @RequestPart("name") String name,
            @RequestPart(value = "parent", required = false) String parentId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        FolderDTO folder = FolderDTO
                .builder()
                .name(name)
                .parentId(parentId)
                .build();

        FolderDTO createdFolder = folderService.create(folder);

        if (files != null && !files.isEmpty()) {
            List<FileDTO> savedFiles = fileService.create(createdFolder, files);
            return new ResponseEntity<>(folderService.findById(createdFolder.getId()), HttpStatus.CREATED);
        }

        return new ResponseEntity<>(createdFolder, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }
    
}
