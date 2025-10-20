package com.team021.financial_nudger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.team021.financial_nudger.domain.IngestedRow;

@Repository
public interface IngestedRowRepository extends JpaRepository<IngestedRow, Integer> {
    
    List<IngestedRow> findByFileId(Integer fileId);
    
    List<IngestedRow> findByFileIdAndParseStatus(Integer fileId, IngestedRow.ParseStatus parseStatus);
    
    @Query("SELECT r FROM IngestedRow r WHERE r.parseStatus = 'PENDING'")
    List<IngestedRow> findPendingRows();
    
    @Query("SELECT r FROM IngestedRow r WHERE r.parseStatus = 'ERROR'")
    List<IngestedRow> findErrorRows();
    
    @Query("SELECT r FROM IngestedRow r WHERE r.fileId = :fileId ORDER BY r.rowNumber")
    List<IngestedRow> findRowsByFileIdOrdered(@Param("fileId") Integer fileId);
}
