package com.ddd.demo.service.impl;

import com.ddd.demo.domain.MemberVO;
import com.ddd.demo.service.SampleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SampleServiceImpl implements SampleService {

    // Cache cho demo data
    private List<MemberVO> allMembers;

    public SampleServiceImpl() {
        // Khởi tạo 100 members mẫu
        initSampleData();
    }

    private void initSampleData() {
        allMembers = new ArrayList<>();
        String[] names = {
                "Nguyễn Văn An", "Trần Thị Bình", "Lê Văn Cường", "Phạm Thị Dung",
                "Hoàng Văn Em", "Vũ Thị Phương", "Đặng Văn Giang", "Bùi Thị Hoa",
                "Lý Văn Inh", "Ngô Thị Kim", "Đinh Văn Long", "Dương Thị Mai",
                "Tô Văn Nam", "Lưu Thị Oanh", "Võ Văn Phú", "Từ Thị Quỳnh"
        };

        for (int i = 1; i <= 100; i++) {
            allMembers.add(new MemberVO(
                    i,
                    "user" + String.format("%03d", i),
                    "pass" + i,
                    names[(i - 1) % names.length] + " " + i,
                    new Timestamp(System.currentTimeMillis() - (long)(Math.random() * 10000000000L))
            ));
        }
    }

    @Override
    public MemberVO getSampleMember() {
        return new MemberVO(
                123,
                "u00",
                "p00",
                "홍길동",
                new Timestamp(System.currentTimeMillis())
        );
    }

    @Override
    public List<MemberVO> get10Members() {
        List<MemberVO> members = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            members.add(new MemberVO(
                    i,
                    "user" + i,
                    "pass" + i,
                    "Thành viên " + i,
                    new Timestamp(System.currentTimeMillis())
            ));
        }
        return members;
    }

    @Override
    public List<MemberVO> getAllMembers() {
        return new ArrayList<>(allMembers);
    }

    @Override
    public Page<MemberVO> getMembersByPageable(int page, int size, String sort, String direction) {
        return getMembersByPageable(page, size, sort, direction, null);
    }

    @Override
    public Page<MemberVO> getMembersByPageable(int page, int size, String sort, String direction, String search) {
        List<MemberVO> filteredMembers = new ArrayList<>(allMembers);

        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            filteredMembers = filteredMembers.stream()
                    .filter(member ->
                            member.getMid().toLowerCase().contains(searchLower) ||
                                    member.getMname().toLowerCase().contains(searchLower) ||
                                    String.valueOf(member.getMno()).contains(searchLower)
                    )
                    .collect(Collectors.toList());
        }

        // Apply sorting
        Comparator<MemberVO> comparator = getComparator(sort, direction);
        filteredMembers.sort(comparator);

        // Calculate pagination
        int total = filteredMembers.size();
        int start = page * size;
        int end = Math.min(start + size, total);

        List<MemberVO> pageContent = start >= total ?
                new ArrayList<>() :
                filteredMembers.subList(start, end);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sort));

        return new PageImpl<>(pageContent, pageable, total);
    }

    private Comparator<MemberVO> getComparator(String sort, String direction) {
        Comparator<MemberVO> comparator;

        switch (sort.toLowerCase()) {
            case "mno":
                comparator = Comparator.comparing(MemberVO::getMno);
                break;
            case "mid":
                comparator = Comparator.comparing(MemberVO::getMid);
                break;
            case "mname":
                comparator = Comparator.comparing(MemberVO::getMname);
                break;
            case "regdate":
                comparator = Comparator.comparing(MemberVO::getRegdate);
                break;
            default:
                comparator = Comparator.comparing(MemberVO::getMno);
        }

        return direction.equalsIgnoreCase("desc") ? comparator.reversed() : comparator;
    }
}