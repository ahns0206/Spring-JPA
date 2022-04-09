package study.datajpa.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;
import study.datajpa.entity.NestedClosedMemberProjection;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/*
 * JpaRepository interface 사용 JPQL
 * */
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // NamedQuery
    // @Query 주석해도 Member에서 findByUsername 먼저 찾고,
    // 없으면 Spring Data JPA에서 위 쿼리처럼 쿼리 생성 및 실행함
    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username=:username and m.age=:age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    // new operation으로 Dto 반환
    @Query("select new study.datajpa.dto.MemberDTO(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDTO> findMemberDTO();

    // collection 파라미터 바인딩
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    // 반환타입 종류
    Member findCollectionByUsername(String username);            // 단건
    Optional<Member> findOptionalByUsername(String username);    // 단건
    List<Member> findListByUsername(String username);            // 컬렉션

    // 페이징
    Page<Member> findPageByUsername(String username, Pageable pageable);     // count 쿼리 결과 포함
    Slice<Member> findSliceByUsername(String username, Pageable pageable);   // count 쿼리 없이 limit+1 까지만 가져옴
    List<Member> findListByUsername(String username, Pageable pageable);    // cont 쿼리 사용안함

    // count 쿼리에서 team join 하지 않도록 재지정
    @Query(value = "select m from Member m left join m.team t",
        countQuery = "select count(m.username) from Member m")
    Page<Member> findSeperatedCountByUsername(Pageable pageable);

    // bulk 연산
    @Modifying(clearAutomatically = true) // executeUpdate() & em.clear() 실행
    @Query("update Member m set m.age = m.age+1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    // team eager join (비추)
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    // team eager join (추)
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    // NamedEntityGraph
//    @EntityGraph(attributePaths = {"team"})
    @EntityGraph("Member.all")
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    // query hint (단순 조회용이기에 스냅샷 생성 안함, @Transactional(readOnly = true)와 비슷한 개념임)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Member> findReadOnlyByUsername(String username);

    // 배타 lock (수정전까지 select 못하게 막음)
    @Lock(LockModeType.WRITE)
    List<Member> findLockByUsername(String username);

    // Projection (Generic type줘서, 동적으로 프로젝션 데이터 번경)
    <T> List<T> findProjectionsByUsername(String username, Class<T> type);

    // Native sql
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    List<Member> findByNativeQuery(String username);

    // Native sql (paging)
    @Query(value = "SELECT m.member_id as id, m.username, t.name as teamName " +
            "FROM member m left join team t",
            countQuery = "SELECT count(*) from member",
            nativeQuery = true)
    Page<NestedClosedMemberProjection> findByNativeQueryPaging(Pageable pageable);


}
