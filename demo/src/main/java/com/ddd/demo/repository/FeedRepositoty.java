package com.ddd.demo.repository;

import com.ddd.demo.entity.feed.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepositoty extends JpaRepository<Feed, Long> {


}
