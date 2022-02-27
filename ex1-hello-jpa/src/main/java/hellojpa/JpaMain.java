package hellojpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello"); // webserver 올라오는 시점에 1개만 생성
        EntityManager em = emf.createEntityManager(); // 고객 요청시마다 생성했다가 close (쓰레드간 공유 x)
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 등록
        // try {
        //     Member member = new Member();
        //     member.setId(1L);
        //     member.setName("hello A");
        //     em.persist(member);

        //     tx.commit();
        // } catch (Exception e) {
        //     tx.rollback();
        // } finally {
        //     em.close();
        // }

        // 수정
        // try {
        //     Member findMember = em.find(Member.class, 1L);
        //     findMember.setName("hello B");

        //     tx.commit();
        // } catch (Exception e) {
        //     tx.rollback();
        // } finally {
        //     em.close();
        // }

        // 삭제
        // try {
        //     Member findMember = em.find(Member.class, 1L);
        //     em.remove(findMember);

        //     tx.commit();
        // } catch (Exception e) {
        //     tx.rollback();
        // } finally {
        //     em.close();
        // }

        
        // 조회
        try {
            // Member findMember = em.find(Member.class, 1L);  
            List<Member> result = em.createQuery("select m from Member as m", Member.class).getResultList();
            for (Member member : result) {
                System.out.println(("member.name = " +  member.getName()));
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }


        emf.close();

    }
}
