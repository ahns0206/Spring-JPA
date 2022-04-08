package study.datajpa.repository;

import lombok.RequiredArgsConstructor;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

/*
* 복잡한 쿼리는 사용자 정의 리포지토리 내 정의
* QueryDSL, Spring JDBC Template
* */
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom() {
            return em.createQuery("select m from Member m")
                    .getResultList();
    }
}
