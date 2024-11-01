package com.muchiri.vault_drop.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.muchiri.vault_drop.dto.FolderDTO;
import com.muchiri.vault_drop.service.FolderService;

@RestController
@RequestMapping("/api/v1/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(final FolderService folderService) {
        this.folderService = folderService;
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
    public ResponseEntity<FolderDTO> createFolder(@RequestBody FolderDTO folderDTO) {
        FolderDTO createdFolder = folderService.create(folderDTO);
        return new ResponseEntity<>(createdFolder, HttpStatus.CREATED);
    }
    
}
