package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    // save, findById, findAll는 Spring Data Jpa가 기본 제공함

    // select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}
