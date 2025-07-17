package com.ddd.demo;

import com.ddd.demo.entity.board.FreeBoardReply;
import com.ddd.demo.entity.board.Freeboard;
import com.ddd.demo.repository.FreeBoardReplyRepository;
import com.ddd.demo.repository.FreeBoardRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest
@Slf4j
public class FreeBoardTests {

    @Autowired
    private FreeBoardRepository boardRepo;

    @Autowired
    private FreeBoardReplyRepository replyRepo;

    @Test
    public void insertDummy() {
        log.info("insert dummy started!!!");

        IntStream.range(0, 200).forEach(idx -> {
            Freeboard board = new Freeboard();
            board.setTitle("Free Board Title " + idx);
            board.setContent("Free Board Content " + idx);
            board.setWriter("user " + idx % 10);

            boardRepo.save(board);
        });
    }

    @Test
    @Transactional
    public void insertReply2Way() {

        Optional<Freeboard> result = boardRepo.findById(199L);

        result.ifPresent(board -> {
            List<FreeBoardReply> replies = board.getReplies();
            FreeBoardReply reply = new FreeBoardReply();
            reply.setReplyText("Reply.........");
            reply.setReplier("replier00");
            reply.setFreeBoard(board);
            replies.add(reply);
            board.setReplies(replies);
            boardRepo.save(board);
        });
    }

    @Test
    public void insertReply1Way() {
        Freeboard board = new Freeboard();
        board.setBno(199L);

        FreeBoardReply reply = new FreeBoardReply();
        reply.setReplyText("Reply.........");
        reply.setReplier("replier00");
        reply.setFreeBoard(board);
        replyRepo.save(reply);
    }

    @Test
    public void testList1() {
//        Pageable page = PageRequest.of(0 , 10, Sort.Direction.DESC, "bno");
//        boardRepo.findByBnoGreaterThan(0L, page).forEach(board -> {
//            log.info(board.getBno() + " : " + board.getTitle() + " : " + board.getWriter());
//        });
//    }
        Pageable pageable = PageRequest.of(0, 10, Sort.by("bno").descending());
        Page<Freeboard> result = boardRepo.findByBnoGreaterThan(100L, pageable);

        log.info("Total elements: " + result.getTotalElements());
        log.info("Total pages: " + result.getTotalPages());
        result.getContent().forEach(board -> log.info(board.toString()));

    }

    @Test
    public void testList2() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("bno").descending());
        Page<Freeboard> result = boardRepo.findByBnoGreaterThan(100L, pageable);

        log.info("Total elements: " + result.getTotalElements());
        log.info("Total pages: " + result.getTotalPages());
        result.getContent().forEach(board -> log.info(board.toString()));
    }

    @Test
    public void testList3() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("bno").descending());
        List<Object[]> list = boardRepo.getPage(pageable);

        list.forEach(arr -> log.info(arr[0] + " : " + arr[1] + " : " + arr[2]));
//        list.forEach(arr -> log.info(Arrays.toString(arr)));
    }
}
