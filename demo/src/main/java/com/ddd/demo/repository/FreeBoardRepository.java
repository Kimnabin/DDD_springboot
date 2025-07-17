package com.ddd.demo.repository;

import com.ddd.demo.entity.board.Freeboard;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FreeBoardRepository extends JpaRepository<Freeboard, Long> {

//    public List<Freeboard> findByBnoGreaterThan(Long bno, Pageable page);

    Page<Freeboard> findByBnoGreaterThan(Long bno, Pageable pageable);

    @Query("select b.bno, b.title, count(r) " +
    "from Freeboard  b left outer join b.replies r " +
    "where b.bno > 0 group by b")
    public List<Object[]> getPage(Pageable page);
}
