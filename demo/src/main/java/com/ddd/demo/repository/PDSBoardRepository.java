package com.ddd.demo.repository;

import com.ddd.demo.entity.board.PDSBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PDSBoardRepository extends JpaRepository<PDSBoard, Long> {

    @Modifying
    @Query("UPDATE PDSFile f SET f.pdsFile = :pdsFile WHERE f.fno = :fno")
    int updatePDSFile(@Param("fno") Long fno, @Param("pdsFile") String pdsFile);

    @Modifying
    @Query("DELETE FROM PDSFile f WHERE f.fno = :fno")
    int deletePDSFile(@Param("fno") Long fno);

    @Query("SELECT p, COUNT(f) FROM PDSBoard p LEFT OUTER JOIN p.files f WHERE p.pid > 0 GROUP BY p ORDER BY p.pid DESC")
    List<Object[]> getSummary();
}