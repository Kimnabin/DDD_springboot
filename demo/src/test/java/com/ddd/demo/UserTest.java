package com.ddd.demo;

import com.ddd.demo.entity.feed.Feed;
import com.ddd.demo.entity.user.User;
import com.ddd.demo.repository.FeedRepository;
import com.ddd.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

@SpringBootTest
public class UserTest {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    @Rollback(false)
    void oneToManyTest() {
        User user = new User();
        Feed feed = new Feed();

        user.setUsername("testUser");
        user.setPassword("testPassword");

        feed.setTitle("testFeedTitle1");
        feed.setDescription("testFeedDescription1");
        user.setFeeds(List.of(feed));
        feed.setUser(user);

        userRepository.save(user);
        feedRepository.save(feed);
    }

    @Test
    @Transactional
    void selectOneToManyTest() {
        User user = userRepository.findById(1L).orElse(null);
        if (user != null) {
            System.out.println("User: " + user.getUsername());
            user.getFeeds().forEach(feed -> {
                System.out.println("Feed Title: " + feed.getTitle());
                System.out.println("Feed Description: " + feed.getDescription());
            });
        } else {
            System.out.println("User not found");
        }
    }
}