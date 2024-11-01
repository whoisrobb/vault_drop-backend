package com.muchiri.vault_drop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.muchiri.vault_drop.domain.FileEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {

    @Query("SELECT f FROM FileEntity f WHERE f.folder.id = :id")
    Optional<List<FileEntity>> findFolderFiles(@PathVariable("id") String id);

}