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
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private final S3Client s3Client;
//    private final S3Presigner s3Presigner;

    @Value("${aws.region}")
    private String bucketRegion;

    @Value("${aws.bucket}")
    private String bucketName;

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    private static final SecureRandom secureRandom = new SecureRandom();

    public FileServiceImpl(S3Client s3Client, FileRepository fileRepository, FolderRepository folderRepository) {
        this.s3Client = s3Client;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    @Override
    public List<FileDTO> create(
            FolderDTO parent,
            List<MultipartFile> files
    ) {
        List<FileEntity> savedFiles = new ArrayList<>();
        FolderEntity parentFolder = folderDtoToFolderEntity(parent);

        try {
            for (MultipartFile file : files) {
                uploadAndSaveFile(file, parentFolder, savedFiles);
            }

            return savedFiles.stream()
                    .map(this::fileEntityToFileDTO)
                    .toList();
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
        String signedUrl = createPresignedGetUrl(file.getKey());

        return FileDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .key(file.getKey())
                .signedUrl(signedUrl)
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


    private FolderDTO folderEntityToFolderDTO(FolderEntity folder) {
        return FolderDTO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
                .files(folder.getFiles().stream()
                        .map(this::fileEntityToFileDTO)
                        .collect(Collectors.toSet())
                )
                .children(new HashSet<FolderDTO>())
                .build();
    }

    private FolderEntity folderDtoToFolderEntity(FolderDTO folder) {
        return FolderEntity.builder()
                .id(folder.getId())
                .name(folder.getName())
//                .files(folder.getFiles())
                .build();
    }


    private void uploadAndSaveFile(MultipartFile file, FolderEntity parentFolder, List<FileEntity> savedFiles) {
        String keyName = genFilename(32);
        PutObjectRequest request = PutObjectRequest.builder()
                .key(keyName)
                .bucket(bucketName)
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            FileEntity fileEntity = FileEntity.builder()
                    .fileName(file.getOriginalFilename())
                    .type(file.getContentType())
                    .folder(parentFolder)
                    .size(file.getSize())
                    .key(keyName)
                    .build();

            FileEntity savedFile = fileRepository.save(fileEntity);
            savedFiles.add(savedFile);

        } catch (IOException e) {
            throw new UncheckedIOException("Error reading file bytes", e);
        }
    }


    public String createPresignedGetUrl(String keyName) {
        try (S3Presigner presigner = S3Presigner.create()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toExternalForm();
        }
    }

}
