package com.ddd.demo.controller.board;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/board")
public class WebBoardController {

    @GetMapping("/list")
    public void list() {
        log.info(("list() called"));
    }


}
