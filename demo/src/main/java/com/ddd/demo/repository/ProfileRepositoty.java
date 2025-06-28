package com.ddd.demo.repository;
import com.ddd.demo.entity.member.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepositoty extends JpaRepository<Profile, Long> {
}
