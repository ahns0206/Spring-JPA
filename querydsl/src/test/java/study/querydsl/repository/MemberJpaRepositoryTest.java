package study.querydsl.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class MemberJpaRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberJpaRepository memberJpaRepository;

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
        memberJpaRepository.save(member);

    	//when
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).contains(member);

        List<Member> result2 = memberJpaRepository.findByUsername("member5");
        assertThat(result2).contains(member);
    }

    /*
     * Data JPA 로 조회
     * team이 teamB면서 나이가 35~40인 member
     * */
    @Test
    public void searchDataJpaByDto() {
        //given
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //when
        List<MemberTeamDTO> result = memberJpaRepository.searchByDataJPA(condition);

        //then
        assertThat(result).extracting("username").containsExactly("member4");
    }

    /*
    * BooleanBuilder 로 조회
    * team이 teamB면서 나이가 35~40인 member
    * */
    @Test
    public void searchBooleanBuilder() {
        //given
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //when
        List<MemberTeamDTO> result = memberJpaRepository.searchByBooleanBuilder(condition);

        //then
        assertThat(result).extracting("username").containsExactly("member4");
    }

    /*
     * BooleanExpression 로 조회
     * team이 teamB면서 나이가 35~40인 member
     * */
    @Test
    public void searchBooleanExpression() {
        //given
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //when
        List<MemberTeamDTO> result = memberJpaRepository.searchByBooleanExpression(condition);

        //then
        assertThat(result).extracting("username").containsExactly("member4");
    }

    /*
     * BooleanExpression 로 조회, Entity 반환
     * team이 teamB면서 나이가 35~40인 member
     * */
    @Test
    public void searchDataJpaByEntity() {
        //given
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //when
        List<Member> result = memberJpaRepository.searchByBooleanExpressionEntity(condition);

        //then
        assertThat(result).extracting("username").containsExactly("member4");
    }
}