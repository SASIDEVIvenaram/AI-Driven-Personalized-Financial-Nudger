package com.team021.financial_nudger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.team021.financial_nudger.domain.IngestedFile;

@Repository
public interface IngestedFileRepository extends JpaRepository<IngestedFile, Integer> {

    List<IngestedFile> findByUserId(Integer userId);

    List<IngestedFile> findByUserIdAndFileType(Integer userId, IngestedFile.FileType fileType);

    List<IngestedFile> findByUserIdAndUploadStatus(Integer userId, IngestedFile.UploadStatus uploadStatus);

    @Query("SELECT f FROM IngestedFile f WHERE f.userId = :userId ORDER BY f.uploadedAt DESC")
    List<IngestedFile> findRecentFilesByUserId(@Param("userId") Integer userId);

    @Query("SELECT f FROM IngestedFile f WHERE f.uploadStatus = 'PENDING' OR f.uploadStatus = 'PROCESSING'")
    List<IngestedFile> findPendingOrProcessingFiles();
}
