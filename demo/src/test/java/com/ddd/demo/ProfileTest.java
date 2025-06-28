package com.ddd.demo;

import com.ddd.demo.entity.member.Member;
import com.ddd.demo.entity.member.Profile;
import com.ddd.demo.repository.MemberRepositoty;
import com.ddd.demo.repository.ProfileRepositoty;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
@Log
@Commit
public class ProfileTest {

    @Autowired
    private MemberRepositoty memberRepositoty;

    @Autowired
    private ProfileRepositoty profileRepositoty;

    // Example test method to demonstrate the usage of Member and Profile
    /**
     * This test method inserts multiple members into the database.
     * It creates 100 members with unique IDs and names.
     * Each member is saved using the member repository.
     */
    @Test
    public void testInsertMemberAndProfile() {
        // Create new members
        IntStream.range(1, 101).forEach(i -> {

            Member member = new Member();
            member.setId("user " + i);
            member.setName("member" + i + "@example.com");
            member.setPwd("password" + i);

            // Save the member
            memberRepositoty.save(member);
        });
    }

    /**
     * This test method inserts multiple profiles for a specific member.
     * It creates a member and associates several profiles with it.
     * The first profile is marked as current.
     */
    @Test
    public void testInsertProfile() {
        Member member = new Member();
        member.setId("user 1");

        for (int i = 1; i < 5; i++ ) {
            Profile profile1 = new Profile();
            profile1.setFname("face" + i + ".jpg");

            if (i == 1) {
                profile1.setCurrent(true);
            }
            profile1.setMember(member);
            profileRepositoty.save(profile1);
        }
    }

    /**
     * Fetch join example to get member with profile count
     * This method uses a custom query to fetch the member and the count of their profiles.
     */
    @Test
    public void testFetchJoin1() {
        List<Object[]> result = memberRepositoty.getMemberWithProfileCount("user 1");

        for (Object[] row : result) {
            String memberId = (String) row[0];
            Long profileCount = (Long) row[1];
            System.out.println("Member ID: " + memberId + ", Profile Count: " + profileCount);
        }

    }
}
