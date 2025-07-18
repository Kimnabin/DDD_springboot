package com.ddd.demo.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlogPost {
    // Getters and Setters
    private String title;
    private String category;
    private String excerpt;
    private String date;
    private String icon;

    public BlogPost() {}

    public BlogPost(String title, String category, String excerpt, String date, String icon) {
        this.title = title;
        this.category = category;
        this.excerpt = excerpt;
        this.date = date;
        this.icon = icon;
    }

    public String getIconEmoji() {
        return switch (icon) {
            case "technology" -> "📱";
            case "design" -> "🎨";
            case "lifestyle" -> "💡";
            default -> "📝";
        };
    }
}
