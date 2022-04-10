package study.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTest {

    @Autowired EntityManager em;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

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
    public void startJPQL() {
        //member1 찾기
        String qlString = "select m from Member m" +
                " where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
//        QMember m = new QMember("m"); // 같은 테이블 조인시에만 alias때문에 사용
//        QMember m = QMember.member;
//        QMember m = member; // 추천

        //member1 찾기
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))//파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
//                .where(member.username.eq("member1"), (member.age.eq(10))) // ,은 and로 null은 무시해서 동적쿼리에 좋음
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    public void fetch() {
//        List<Member> members = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        Member fetchOne = queryFactory
//                .selectFrom(QMember.member)
//                .fetchOne();
//
//        Member fetchFirst = queryFactory
//                .selectFrom(QMember.member)
//                .fetchFirst();//.limit(1).fetch()

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults(); // total count + Paging data

        results.getTotal();
        List<Member> resultsResults = results.getResults();

//        long fetchCount = queryFactory
//                .selectFrom(member)
//                .fetchCount();
    }

    /*
    * 회원 정렬 순서
    * 1. 회원 나이 내림차순
    * 2. 회원 이름 올림차순
    * 단 2에서 회원 이름 없으면 마지막에 출력
    * */
    @Test
    public void sort() throws Exception {
    	//given
    	em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

    	//when
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        //then
        assertThat(result.get(0).getUsername()).isEqualTo("member5");
        assertThat(result.get(1).getUsername()).isEqualTo("member6");
        assertThat(result.get(2).getUsername()).isNull();
    }

    @Test
    public void paging() {
        // when
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults(); // count + select all

        // then
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() throws Exception {
    	//when
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

    	//then
        assertThat(result.get(0).get(member.count())).isEqualTo(4);
        assertThat(result.get(0).get(member.age.sum())).isEqualTo(100);
        assertThat(result.get(0).get(member.age.avg())).isEqualTo(25);
        assertThat(result.get(0).get(member.age.max())).isEqualTo(40);
        assertThat(result.get(0).get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구하기
     */
    @Test
    public void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); //(10+20) /2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); //(30+40) /2
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() {
        List<Member> members = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(members.size()).isEqualTo(2);
        assertThat(members)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /*
    * 세타조인
    * 연관관계 없이 조인
    * 회원의 이름이 팀 이름과 같은 회원 조회
    */
    @Test
    public void theta_join() throws Exception {
    	//given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

    	//when
        List<Member> thetaJoinMembers = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        //then
        assertThat(thetaJoinMembers)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /*
    * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
    * JPQL : select m, t from Member m left join m.tead t on t.name = 'teamA'
    **/
    @Test
    public void join_on_filtering() throws Exception {
    	//when
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        //then
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
//        assertThat(result.size()).isEqualTo(4);
    }

    @Test
    public void join_on_no_relation() throws Exception {
    	//given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

    	//when
        List<Tuple> leftJoinMembers = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

    	//then
        for (Tuple tuple : leftJoinMembers) {
            System.out.println("tuple = " + tuple.toString());
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();

        Member fetchOne = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
    
    	//then
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(fetchOne.getTeam());
        assertThat(loaded).as("페치조인 미적용").isFalse();
    }

    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치조인 적용").isTrue();
    }

    /*
    * 나이가 가장 많은 회원 조회
    * */
    @Test
    public void subQuery() throws Exception {
    	//given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> maxAge = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        Expressions.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "maxAge")
                ))
                .fetch();
    
    	//then
        assertThat(maxAge).extracting("age").containsExactly(40);
    }

    /*
    * 나이가 평균 이상인 회원
    * */
    @Test
    public void subQueryGoe() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> maxAge = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                                Expressions.as(JPAExpressions
                                        .select(memberSub.age.avg())
                                        .from(memberSub), "memberAgeAvg")
                ))
                .fetch();

        //then
        assertThat(maxAge).extracting("age").containsExactly(30, 40);
    }

    /*
     * 나이가 평균 이상인 회원
     * */
    @Test
    public void subQueryIn() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> maxAge = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                                Expressions.as(JPAExpressions
                                        .select(memberSub.age)
                                        .from(memberSub)
                                        .where(memberSub.age.gt(20)), "memberAgeGt20")
                ))
                .fetch();

        //then
        assertThat(maxAge).extracting("age").containsExactly(30, 40);
    }

    /*
     * 나이가 평균 이상인 회원
     * */
    @Test
    public void subQuerySelect() throws Exception {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Tuple> result = queryFactory
                .select(member,
                        Expressions.as(JPAExpressions
                                        .select(memberSub.age.avg())
                                        .from(memberSub)
                                        , "memberAgeAvg"))
                .from(member)
                .fetch();

        //then
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
            System.out.println("tuple.get(member) = " + tuple.get(member));
        }
    }

    /*
    * 간단한 case 문
    * */
    @Test
    public void basicCase() throws Exception {
    	//when
        List<Tuple> results = queryFactory
                .select(member, member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        //then
        for (Tuple result : results) {
            System.out.println("result = " + result);
        }
    }

    /*
     * 복잡한 case 문
     * */
    @Test
    public void complexTest() throws Exception {
    	//when
        List<Tuple> results = queryFactory
                .select(member, new CaseBuilder()
                        .when(member.age.between(0, 20)).then("미성년")
                        .when(member.age.gt(20)).then("성인")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        //then
        for (Tuple result : results) {
            System.out.println("result = " + result);
        }
    }

    /*
    * 상수 조회 (쿼리상 들어가는게 아니라 어플리케이션 단에서 상수 추가됨)
    * */
    @Test
    public void constant() throws Exception {
    	//when
        List<Tuple> results = queryFactory
                .select(member.username, Expressions.constant("test"))
                .from(member)
                .fetch();

        //then
        for (Tuple result : results) {
            System.out.println("result = " + result);
        }
    }

    /*
    * 문자열 합치기
        member0_.username as col_0_0_,
        ((member0_.username||?)||cast(member0_.age as char)) as col_1_0_
    from
        member member0_;
    * {username}_{age}
    * */
    @Test
    public void concatString() throws Exception {
        //when
        List<Tuple> results = queryFactory
                .select(member.username, member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        //then
        for (Tuple result : results) {
            System.out.println("result = " + result);
        }
    }

   /* eq - equal ( = )
    ne - not equal ( <> )
    lt - little ( < )
    le - little or equal ( <= )
    gt - greater ( > )
    ge - greater or equal ( >= )*/

}