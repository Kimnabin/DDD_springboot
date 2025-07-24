package com.ddd.demo.service;

import com.ddd.demo.domain.MemberVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface SampleService {
    MemberVO getSampleMember();
    List<MemberVO> get10Members();
    Page<MemberVO> getMembersByPageable(int page, int size, String sort, String direction);
    Page<MemberVO> getMembersByPageable(int page, int size, String sort, String direction, String search);
    List<MemberVO> getAllMembers(); // Helper method
}