package com.ddd.demo;

import com.ddd.demo.entity.feed.Feed;
import com.ddd.demo.entity.user.User;
import com.ddd.demo.repository.FeedRepositoty;
import com.ddd.demo.repository.UserRepositoty;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

@SpringBootTest
public class UserTest {

    @Autowired
    private FeedRepositoty feedRepositoty;

    @Autowired
    private UserRepositoty userRepositoty;

    // Test methods can be added here to test the functionality of UserRepositoty and FeedRepositoty

    @Test
    @Transactional
    @Rollback(false)
    void oneToManyTest() {
        // 1. Create a user and save it
        User user = new User();
        Feed feed = new Feed();

        user.setUserName("testUser");
        user.setPassword("testPassword");

        feed.setTitle("testFeedTitle1");
        feed.setDescription("testFeedDescription1");
        user.setFeedList(List.of(feed));
        feed.setUser(user);

        userRepositoty.save(user);
        feedRepositoty.save(feed);
    }

    @Test
    @Transactional
    void selectOneToManyTest() {
        // 2. Retrieve the user and their feeds
        User user = userRepositoty.findById(1L).orElse(null);
        if (user != null) {
            System.out.println("User: " + user.getUserName());
            user.getFeedList().forEach(feed -> {
                System.out.println("Feed Title: " + feed.getTitle());
                System.out.println("Feed Description: " + feed.getDescription());
            });
        } else {
            System.out.println("User not found");
        }
    }
}
