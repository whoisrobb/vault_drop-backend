package com.muchiri.vault_drop.service.impl;

import com.muchiri.vault_drop.domain.FileEntity;
import com.muchiri.vault_drop.domain.FolderEntity;
import com.muchiri.vault_drop.dto.FileDTO;
import com.muchiri.vault_drop.dto.FolderDTO;
import com.muchiri.vault_drop.repository.FileRepository;
import com.muchiri.vault_drop.repository.FolderRepository;
import com.muchiri.vault_drop.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// service impl
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private final S3Client s3Client;

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    private static final SecureRandom secureRandom = new SecureRandom();

    public FileServiceImpl(S3Client s3Client, FileRepository fileRepository, FolderRepository folderRepository) {
        this.s3Client = s3Client;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    @Value("${BUCKET_NAME}")
    private String bucketName;

    // fileservice impl
    @Override
    public FileDTO create(
            FolderDTO parent,
            String fileName,
            Long size,
            String contentType,
            byte[] fileContent
    ) {
        FolderEntity parentFolder = folderDtoToFolderEntity(parent);

        String keyName = genFilename(32);
        PutObjectRequest request = PutObjectRequest.builder()
                .key(keyName)
                .bucket(bucketName)
                .build();

        try {
            PutObjectResponse putObjectResponse = s3Client.putObject(request, RequestBody.fromBytes(fileContent));

            FileEntity file = FileEntity.builder()
                    .fileName(fileName)
                    .type(contentType)
                    .folder(parentFolder)
                    .size(size)
                    .key(keyName)
                    .build();

            FileEntity savedFile = fileRepository.save(file);
            return fileEntityToFileDTO(savedFile);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading to s3", e);
        }
    }

    @Override
    public List<FileDTO> findFolderFiles(String folderId) {
        List<FileEntity> folderFiles = fileRepository.findFolderFiles(folderId)
                .orElseThrow(() -> new RuntimeException("Folder or files not available"));

        return folderFiles.stream()
                .map(this::fileEntityToFileDTO)
                .toList();
    }

    public static String genFilename(int bytes) {
        byte[] randomBytes = new byte[bytes];
        secureRandom.nextBytes(randomBytes);

        // Convert the byte array to a positive BigInteger and then to a hex string
        return new BigInteger(1, randomBytes).toString(16);
    }

    private FileDTO fileEntityToFileDTO(FileEntity file) {
        return FileDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .key(file.getKey())
                .size(file.getSize())
                .type(file.getType())
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
