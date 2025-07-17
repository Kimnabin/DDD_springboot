package com.ddd.demo.repository;

import com.ddd.demo.entity.board.FreeBoardReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreeBoardReplyRepository extends JpaRepository<FreeBoardReply, Long> {
}
