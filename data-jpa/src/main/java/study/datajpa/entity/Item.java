package study.datajpa.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item implements Persistable<String> {
    /*@GeneratedValue면 SimpleJpaRepository.save시점에 식별자 없어 정상 동작함.
    * 직접 @Id만 사용해서 할당하면 이미 식별자가 있어 merge 수행함(select & insert)
    * Persistable 사용해 새 엔티티 확인 여부인 isNew를 overriding 함
    * */
    @Id
    //    @GeneratedValue
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    public Item(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        // isNew overriding해서 객체 null인지 체크하는게 아닌 createdDate를 체크하게 함
        return createdDate == null;
    }
}
