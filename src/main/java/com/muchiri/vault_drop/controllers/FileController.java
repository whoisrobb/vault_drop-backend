package com.muchiri.vault_drop.controllers;

import java.io.IOException;
import java.util.List;

import com.muchiri.vault_drop.dto.FileDTO;
import com.muchiri.vault_drop.dto.FolderDTO;
import com.muchiri.vault_drop.service.FileService;
import com.muchiri.vault_drop.service.FolderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    @Value("${aws.region}")
    private String bucketName;

    private final FileService fileService;
    private final FolderService folderService;

    public FileController(FileService fileService, FolderService folderService) {
        this.fileService = fileService;
        this.folderService = folderService;
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<List<FileDTO>> folderFiles(@PathVariable String folderId) {
        List<FileDTO> folderFiles = fileService.findFolderFiles(folderId);

        return new ResponseEntity<>(folderFiles, HttpStatus.OK);
    }

    @PostMapping("/{folderId}")
    public ResponseEntity<?> saveFile(
            @RequestPart(value = "files") List<MultipartFile> files,
            @PathVariable String folderId
        ) throws IOException {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file selected to upload!");
        }

        FolderDTO parent = folderService.findById(folderId);
        List<FileDTO> createdFiles = fileService.create(parent, files);

        return new ResponseEntity<>(createdFiles, HttpStatus.CREATED);
    }
    
}
