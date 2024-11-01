package com.muchiri.vault_drop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.muchiri.vault_drop.domain.FolderEntity;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, String> {
    
    @Query("SELECT f FROM FolderEntity f WHERE f.parent.id = :id")
    List<FolderEntity> findChildrenById(@Param("id") String id);
    
    @Query("SELECT f FROM FolderEntity f WHERE f.parent.id IS NULL")
    List<FolderEntity> findAllRootFolders();

}
