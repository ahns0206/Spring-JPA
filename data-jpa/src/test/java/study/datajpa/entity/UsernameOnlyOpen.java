package study.datajpa.entity;

import org.springframework.beans.factory.annotation.Value;

// 인터페이스 기반 Open Projection
public interface UsernameOnlyOpen {
    // SpEL 사용해서 db내 엔티티 필드 다 가져와 어플리케이션 레벨에서 계산함
    @Value("#{target.username + ' ' + target.age + ' ' + target.team.name}")
    String getUsername();
}
