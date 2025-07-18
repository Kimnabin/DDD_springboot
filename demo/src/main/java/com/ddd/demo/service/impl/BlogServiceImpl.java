package com.ddd.demo.service.impl;

import com.ddd.demo.model.BlogPost;
import com.ddd.demo.service.BlogService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class BlogServiceImpl implements BlogService {
    @Override
    public List<BlogPost> getFeaturedPosts() {
        return Arrays.asList(
                new BlogPost(
                        "Xu hướng phát triển web 2025",
                        "Công nghệ",
                        "Khám phá những công nghệ mới nhất và xu hướng phát triển web trong năm 2025. Từ AI integration đến performance optimization...",
                        "15 Tháng 7, 2025",
                        "technology"
                ),
                new BlogPost(
                        "UI/UX Design Trends 2025",
                        "Thiết kế",
                        "Những xu hướng thiết kế giao diện người dùng mới nhất, từ minimalism đến interactive experiences...",
                        "12 Tháng 7, 2025",
                        "design"
                ),
                new BlogPost(
                        "Work-Life Balance trong thời đại số",
                        "Cuộc sống",
                        "Làm thế nào để cân bằng công việc và cuộc sống cá nhân trong thời đại công nghệ phát triển...",
                        "10 Tháng 7, 2025",
                        "lifestyle"
                )
        );
    }

    @Override
    public List<String> getCategories() {
        return Arrays.asList(
                "Công nghệ", "Thiết kế", "Cuộc sống",
                "Du lịch", "Học tập", "Sức khỏe"
        );
    }
}
