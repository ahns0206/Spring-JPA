package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDTO;
import study.querydsl.dto.QMemberDTO;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class QuerydslBasicTest {

    @Autowired EntityManager em;

    JPAQueryFactory queryFactory;

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
    public void simpleProjection() {
        // 프로젝션 대상 하나
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection() {
        // 프로젝션 대상 둘 이상
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " + tuple.get(member.age));
        }
    }

    /*
    * 1. JPQL 사용해 DTO로 결과 조회
    * 비추
    * */
    @Test
    public void findDTOByJPQL() {
        List<MemberDTO> result = em.createQuery(
                "select new study.querydsl.dto.MemberDTO(m.username, m.age) from Member m"
                        , MemberDTO.class)
                .getResultList();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    /*
     * 2. setter 사용해 DTO로 결과 조회 (프로퍼티 접근)
     * 비추, 컴파일때 오류 확인됨
     * */
    @Test
    public void findDTOBySetter() {
        List<MemberDTO> result = queryFactory
                .select(Projections.bean(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    /*
     * 3. 각 변수에 바로 대입해 DTO로 결과 조회 (필드 직접 접근)
     * setter, 생성자 사용 x
     * 비추, 컴파일때 오류 확인됨
     * */
    @Test
    public void findDTOByField() {
        List<MemberDTO> result = queryFactory
                .select(Projections.fields(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    /*
     * 4. 생성자로 DTO로 결과 조회 (생성자 사용)
     * 생성자 외 기본 생성자도 필요 (NoArgsConstructor)
     * 비추, 컴파일때 오류 확인됨
     * */
    @Test
    public void findDTOByConstructor() {
        // 생성자 파라미터 순서별로 값 넣어야 함
        List<MemberDTO> result = queryFactory
                .select(Projections.constructor(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    /*
     * 5. Q파일 생성해 결과 조회 (생성자 + @QueryProjection)
     * 추천, 바로 오류 확인됨
     * 단점: @QueryProjection로 queryDSL 의존성 생김, Q파일 빌드 필요함
     * */
    @Test
    public void findDtoByQueryProjection() throws Exception {
        //when
        List<MemberDTO> result = queryFactory
                .select(new QMemberDTO(member.username, member.age))
                .from(member)
                .fetch();

        //then
        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    /*
     * alias 사용
     * 필드명 다르게 해서 dto 조회하고 싶은 경우
     * username -> name
     * 최대 나이 -> maxAge
     * */
    @Test
    public void findUserDTOByField() {
        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age,
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "maxAge")))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("memberDTO = " + userDto);
        }
    }

    /*
    * 동적 쿼리 (BooleanBuilder)
    * 비추천
    * */
    @Test
    public void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMemberBooleanBuilderUsing(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMemberBooleanBuilderUsing(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();

        if(usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if(ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    /*
     * 동적 쿼리 (BooleanExpression, Where 다중 파라미터 사용)
     * 추천: 쿼리 조합해 재활용 가능, 가독성 좋음
     * */
    @Test
    public void dynamicQuery_WhereParam() {
        String usernameParma = "member1";
        Integer ageParam = null;

        List<Member> result = searchMemberWhereUsing(usernameParma, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMemberWhereUsing(String usernameCod, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCod), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCod) {
        return usernameCod != null ? member.username.eq(usernameCod) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    // 쿼리 조합도 가능
    private BooleanExpression usernameEqAndAgeEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    /*벌크 수정 연산*/
    @Test
    public void bulkUpdate() {
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .set(member.age, member.age.add(1))
                .where(member.age.lt(25))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member : result) {
            System.out.println("member = " + member);
            
        }
    }

    /*벌크 덧셈, 뺄셈, 곱셈 연산*/
    @Test
    public void bulkAdd() {
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1)) // add, multiple, add(-1)
                .where(member.age.lt(25))
                .execute();
        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member : result) {
            System.out.println("member = " + member);

        }
    }

    /*벌크 삭제 연산*/
    @Test
    public void bulkDelete() {
        long count = queryFactory
                .delete(member)
                .where(member.age.lt(25))
                .execute();
        // flush & clear 안하고 select 하면 영속성 컨텍스트에 이미 담겨진 이전 값 가져옴
    }

    /*sql Function 호출*/
    @Test
    public void sqlFunction() throws Exception {
        // username의 member를 M으로 변경
        List<String> result = queryFactory
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})"
                        , member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String MMemberName : result) {
            System.out.println("MMemberName = " + MMemberName);
        }

        // member.username을 소문자로 봐꿔 equal 조회
        List<String> result2 = queryFactory.select(member.username)
                .from(member)
//                .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
                .where(member.username.eq(member.username.lower()))
                .fetch();

        for (String lowerMemberName : result2) {
            System.out.println("lowerMemberName = " + lowerMemberName);
        }
    }

}
