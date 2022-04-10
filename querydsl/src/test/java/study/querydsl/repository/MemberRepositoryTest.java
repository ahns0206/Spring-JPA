package study.querydsl.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDTO;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired EntityManager em;

    @Autowired MemberRepository memberRepository;

    @BeforeEach
    public void before() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void basicTest() throws Exception {
        //given
        Member member = new Member("member5", 50);
        memberRepository.save(member);

        //when
        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).contains(member);

        List<Member> result2 = memberRepository.findByUsername("member5");
        assertThat(result2).contains(member);
    }

    @Test
    public void searchBooleanExpression() {
        //given
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //when
        List<MemberTeamDTO> result = memberRepository.searchByBooleanExpression(condition);

        //then
        assertThat(result).extracting("username").containsExactly("member4");
    }
    
    @Test
    public void searchPageSimple() throws Exception {
        //given
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        PageRequest pageRequest = PageRequest.of(0, 3);

        //when
        Page<MemberTeamDTO> result = memberRepository.searchPageSimple(condition, pageRequest);

        //then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting("username").containsExactly("member4");
    }
    @Test
    public void searchPageComplex() throws Exception {
        //given
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        PageRequest pageRequest = PageRequest.of(0, 3);

        //when
        Page<MemberTeamDTO> result = memberRepository.searchPageComplex(condition, pageRequest);

        //then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting("username").containsExactly("member4");
    }
}