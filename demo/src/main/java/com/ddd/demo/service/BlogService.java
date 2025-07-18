package com.ddd.demo.service;

import com.ddd.demo.model.BlogPost;

import java.util.List;

public interface BlogService {
    List<BlogPost> getFeaturedPosts();

    List<String> getCategories();
}
