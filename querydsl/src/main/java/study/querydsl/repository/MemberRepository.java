package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
    // save, findById, findAll는 Spring Data Jpa가 기본 제공함
    // QuerydslPredicateExecutor는 실무에서 사용하기엔 한계가 있기에 상속받을지 고려 필수

    // select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}
