package com.ddd.demo.controller.home;

import com.ddd.demo.model.BlogPost;
import com.ddd.demo.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private BlogService blogService;

    @GetMapping("")
    public String home(Model model) {
        // Sử dụng service để lấy dữ liệu
        model.addAttribute("pageTitle", "MyBlog - Chia sẻ kiến thức & trải nghiệm");
        model.addAttribute("blogTitle", "MyBlog");
        model.addAttribute("heroTitle", "Chào mừng đến với MyBlog");
        model.addAttribute("heroSubtitle", "Nơi chia sẻ kiến thức, trải nghiệm và những câu chuyện thú vị về cuộc sống");
        model.addAttribute("featuredPosts", blogService.getFeaturedPosts());
        model.addAttribute("categories", blogService.getCategories());
        model.addAttribute("currentYear", java.time.Year.now().getValue());

        return "blog";
    }
}
