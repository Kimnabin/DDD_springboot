// 1. Updated Controller với phân trang
package com.ddd.demo.controller.sample;

import com.ddd.demo.domain.MemberVO;
import com.ddd.demo.service.SampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sample")
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @GetMapping("/1")
    public String sample(Model model) {
        MemberVO member = sampleService.getSampleMember();
        model.addAttribute("member", member);
        return "sample";
    }

    @GetMapping("/2")
    public String sample2(Model model) {
        model.addAttribute("members", sampleService.get10Members());
        return "sample1";
    }

    // New endpoint cho phân trang
    @GetMapping("/paginated")
    public String samplePaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "mno") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            Model model) {

        // Validate page và size
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Giới hạn max size

        // Lấy data với phân trang
        Page<MemberVO> memberPage = sampleService.getMembersByPageable(
                page - 1, // Spring Data sử dụng 0-based indexing
                size,
                sort,
                direction,
                search
        );

        // Tính toán pagination display range
        int totalPages = memberPage.getTotalPages();
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        // Add attributes cho Thymeleaf
        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", memberPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("search", search);

        // Thông tin hiển thị
        long startItem = (page - 1) * size + 1;
        long endItem = Math.min(page * size, memberPage.getTotalElements());
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);

        // Trả về view
        return "sample2"; // Trả về file templates/sample2.html
    }
}
