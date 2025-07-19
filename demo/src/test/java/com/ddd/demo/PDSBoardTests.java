package com.ddd.demo;

import com.ddd.demo.entity.board.PDSBoard;
import com.ddd.demo.entity.board.PDSFile;
import com.ddd.demo.repository.PDSBoardRepository;
import com.ddd.demo.repository.pdsBoardRepository;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


@SpringBootTest
@Log
//@Commit  // Commit the transaction after the test
public class PDSBoardTests {
    @Autowired
    private PDSBoardRepository pdsBoardRepository;

    @Test
//    @Transactional
    public void testInsertPDSBoard() {
        PDSBoard pdsBoard = new PDSBoard();
        pdsBoard.setPName("Document");
        pdsBoard.setPWriter("admin");

        PDSFile pdsFile1 = new PDSFile();
        pdsFile1.setPdsFile("file1.txt");
        pdsFile1.setBoard(pdsBoard);


        PDSFile pdsFile2 = new PDSFile();
        pdsFile2.setPdsFile("file2.txt");
        pdsFile2.setBoard(pdsBoard);

        // gán list files
        pdsBoard.setFiles(Arrays.asList(pdsFile1, pdsFile2));

        // save chỉ 1 lần, Hibernate sẽ cascade xuống PDSFile
        pdsBoardRepository.save(pdsBoard);

        log.info("PDSBoard saved!");
    }

    @Test
    @Transactional
    public void testUpdateFileName1() {
        Long fno = 1L; // ID của PDSFile cần cập nhật
        String newFileName = "updated_file1.txt"; // Tên file mới

        int count = pdsBoardRepository.updatePDSFile(fno, newFileName);
        log.info("update count: " + count);
    }

    @Test
    @Transactional
    public void testUpdateFileName2() {
        Long boardId = 1L; // ID của PDSBoard
        Long targetFileId = 2L; // ID của PDSFile
        String newFileName = "updated_file2.txt";

        Optional<PDSBoard> result = pdsBoardRepository.findById(boardId);
        result.ifPresent(board -> {
            log.info("Board found, updating file...");
            board.getFiles().forEach(file -> {
                if (file.getFno().equals(targetFileId)) {
                    file.setPdsFile(newFileName);
                }
            });
            pdsBoardRepository.save(board);
        });
    }

    @Test
    @Transactional
    public void deletePDSFileTest() {
        Long fno = 2L; // ID của PDSFile cần xóa
        int count = pdsBoardRepository.deletePDSFile(fno);
        log.info("delete count: " + count);
    }

    @Test
    public void insertDummies() {
        List<PDSBoard> list = new ArrayList<>();

        IntStream.range(0, 100).forEach(index -> {
            PDSBoard pdsBoard = new PDSBoard();
            pdsBoard.setPName("자료 " + index);
            pdsBoard.setPWriter("admin");

            PDSFile pdsFile1 = new PDSFile();
            pdsFile1.setPdsFile("file" + index + "_1.txt");
            pdsFile1.setBoard(pdsBoard);

            PDSFile pdsFile2 = new PDSFile();
            pdsFile2.setPdsFile("file" + index + "_2.txt");
            pdsFile2.setBoard(pdsBoard);

            // gán list files
            pdsBoard.setFiles(Arrays.asList(pdsFile1, pdsFile2));

            list.add(pdsBoard);
        });
        pdsBoardRepository.saveAll(list);
    }

    @Test
    public void viewSummary() {
        pdsBoardRepository.getSummary().forEach(arr ->
                log.info(Arrays.toString(arr)));
    }

    @Test
    public void viewSummaryUpdate() {
        List<Object[]> summary = pdsBoardRepository.getSummary();
        summary.forEach(row -> {
            PDSBoard board = (PDSBoard) row[0];
            Long fileCount = (Long) row[1];

            System.out.println(board.getPName() + " có " + fileCount + " file");
        });

    }

}
