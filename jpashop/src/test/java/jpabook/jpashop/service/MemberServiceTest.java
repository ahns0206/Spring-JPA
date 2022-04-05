package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

@ExtendWith(SpringExtension.class) //JUnit 실행시 스프링이랑 엮어 실행
@SpringBootTest //SpringBoot 컨테이너 내에서 테스트 실행
@Transactional //테스트 끝나면 롤백 위함
public class MemberServiceTest {

    @Autowired EntityManager em;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    // @Rollback(false) // DB에 강제 commit
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("sehee");

        //when
        // persist만 하고 flush(commit) 안하고 롤백됨
        // -> 테스트 코드에서 @Transactional은 커밋안하고 자동 롤백하기 때문
        Long saveId = memberService.join(member);

        //then
        assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("sehee1");

        Member member2 = new Member();
        member2.setName("sehee1");

        //when
        memberService.join(member1);

        //then
        // memberService.join(member2) 하면 중복 오류 발생
        assertThrows(IllegalStateException.class, () -> memberService.join(member2));
    }
}