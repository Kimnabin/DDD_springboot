package com.ddd.demo;

import com.ddd.demo.entity.member.Member;
import com.ddd.demo.entity.member.Profile;
import com.ddd.demo.repository.MemberRepository;
import com.ddd.demo.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
@Slf4j
@Commit
public class ProfileTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    public void testInsertMemberAndProfile() {
        IntStream.range(1, 101).forEach(i -> {
            Member member = new Member();
            member.setId("user " + i);
            member.setName("member" + i + "@example.com");
            member.setPwd("password" + i);
            memberRepository.save(member);
        });
    }

    @Test
    public void testInsertProfile() {
        Member member = new Member();
        member.setId("user 1");

        for (int i = 1; i < 5; i++) {
            Profile profile = new Profile();
            profile.setFname("face" + i + ".jpg");

            if (i == 1) {
                profile.setCurrent(true);
            }
            profile.setMember(member);
            profileRepository.save(profile);
        }
    }

    @Test
    public void testFetchJoin1() {
        List<Object[]> result = memberRepository.getMemberWithProfileCount("user 1");

        for (Object[] row : result) {
            String memberId = (String) row[0];
            Long profileCount = (Long) row[1];
            log.info("Member ID: {}, Profile Count: {}", memberId, profileCount);
        }
    }
}