package com.ddd.demo.repository;

import com.ddd.demo.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, String> {

    @Query("SELECT m.id, COUNT(p) FROM Member m LEFT JOIN Profile p ON m = p.member WHERE m.id = ?1 GROUP BY m.id")
    List<Object[]> getMemberWithProfileCount(String memberId);

}
