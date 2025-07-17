package com.ddd.demo.repository;

import com.ddd.demo.entity.board.PDSBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PDSBoardRepositoy extends JpaRepository<PDSBoard, Long> {

    @Modifying
    @Query("update PDSFile f set f.pdsFile = ?2 where f.fno = ?1")
    int updatePDSFile(Long fno, String pdsFile);

    @Modifying
    @Query("delete from PDSFile f where f.fno = ?1")
    int deletePDSFile(Long fno);

    @Query("select p, count(f) from PDSBoard p left outer join p.files f where p.pid > 0 group by p order by p.pid desc")
    public List<Object[]> getSummary();
}

