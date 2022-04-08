package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {
    @Autowired 
    private MemberRepository memberRepository;

    @Autowired 
    private TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get(); //optional로 가져와 없으면 null exception 발생
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen() {
        // given
        Member m1 = new Member("member", 10);
        Member m2 = new Member("member", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("member", 15);

        // then
        assertThat(result.get(0).getUsername()).isEqualTo("member");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() {
        // given
        Member m1 = new Member("member", 10);
        Member m2 = new Member("member", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<Member> result = memberRepository.findByUsername("member");

        // then
        assertThat(result.get(0).getUsername()).isEqualTo("member");
        assertThat(result.get(1).getUsername()).isEqualTo("member");
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void testQuery() {
        // given
        Member m1 = new Member("m1", 10);
        Member m2 = new Member("m2", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<Member> result = memberRepository.findUser("m2", 20);

        // then
        assertThat(result.get(0)).isEqualTo(m2);
    }

    @Test
    public void findMemberDTO() {
        // given
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("m1", 10);
        m1.changeTeam(team);
        memberRepository.save(m1);

        // when
        List<MemberDTO> memberDTO = memberRepository.findMemberDTO();
        
        // then
        assertThat(memberDTO.size()).isEqualTo(2);
    }

    @Test
    public void findMemberCollectionBinding() {
        // given
        Member m1 = new Member("m1", 10);
        Member m2 = new Member("m2", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<Member> members = memberRepository.findByNames(Arrays.asList("m1", "m2"));

        // then
        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    public void findMemberReturnType() {
        Member m1 = new Member("m1", 10);
        Member m2 = new Member("m2", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // 다건 조회
        List<Member> members = memberRepository.findListByUsername("m1");
        assertThat(members.get(0).getUsername()).isEqualTo("m1");

        // 단건 조회
        Member member = memberRepository.findCollectionByUsername("m1");
        assertThat(member.getUsername()).isEqualTo("m1");

        // 단건 조회
        Optional<Member> memberOptional = memberRepository.findOptionalByUsername("m1");
        assertThat(memberOptional.get().getUsername()).isEqualTo("m1");

        // 결과 2건 이상이여서 Optional 오류 발생
        Member otherM1 = new Member("m1", 10);
        memberRepository.save(otherM1);
        assertThatThrownBy(() -> memberRepository.findOptionalByUsername("m1"))
            .isInstanceOf(org.springframework.dao.IncorrectResultSizeDataAccessException.class);
    }

    @Test
    public void paging() {
        //given
        memberRepository.save(new Member("member1", 1));
        memberRepository.save(new Member("member1", 2));
        memberRepository.save(new Member("member1", 3));
        memberRepository.save(new Member("member1", 4));
        memberRepository.save(new Member("member1", 5));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username")); // 페이징

        //when
        Page<Member> page = memberRepository.findPageByUsername("member1", pageRequest);
        Page<MemberDTO> toMap = page.map(m -> new MemberDTO(m.getId(), m.getUsername(), null));

        //then
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3);            //현재 갯수
        assertThat(page.getTotalElements()).isEqualTo(5);   //총 갯수
        assertThat(page.getNumber()).isEqualTo(0);          //현재 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2);      //전체 페이지 갯수
        assertThat(page.isFirst()).isTrue();                //첫번째 페이지인가
        assertThat(page.hasNext()).isTrue();                //다음 페이지가 존재하는가
        assertThat(page.getPageable().getPageNumber()).isEqualTo(0);  //현재 페이지 객체 몇번
        assertThat(page.nextPageable().getPageNumber()).isEqualTo(1); //다음 페이지 객체 몇번
    }

    @Test
    public void pagingSeperatedCount() throws Exception {
    	//given
        memberRepository.save(new Member("member1", 1));
        memberRepository.save(new Member("member1", 2));
        memberRepository.save(new Member("member1", 3));
        memberRepository.save(new Member("member1", 4));
        memberRepository.save(new Member("member1", 5));

    	//when
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<Member> content = memberRepository.findSeperatedCountByUsername(pageRequest);
        Page<MemberDTO> toMap = content.map(m -> new MemberDTO(m.getId(), m.getUsername(), null));

    	//then
        assertThat(content.getNumberOfElements()).isEqualTo(105); //갯수
        assertThat(content.getTotalElements()).isEqualTo(105);   //총 갯수
    }
    @Test
    public void bulkUpdate() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 41));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);
//        em.flush();
//        em.clear();

        List<Member> members = memberRepository.findByUsername("member3");
        assertThat(members.get(0).getAge()).isEqualTo(21);
    }

    @Test
    public void findMemberLazy() throws Exception {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when
        List<Member> members = memberRepository.findAll(); // @EntityGraph 없으면 N+1 문제 발생

        //then
        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }

        //when
        em.clear();
        List<Member> members2 = memberRepository.findMemberFetchJoin();

        //then
        for (Member member : members2) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHint() throws Exception {
    	//given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

    	//when
        Member findMember = memberRepository.findReadOnlyByUsername(member1.getUsername()).get(0);
        findMember.changeUsername("member2");
        em.flush();
        em.clear();

    	//then
        assertTrue(memberRepository.findByUsername("member2").isEmpty());
        //스냅샷 생성 안해 변경감지로 수정된게 반영되지 않음
    }

    @Test
    public void lock() throws Exception {
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();
    }

    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }
}