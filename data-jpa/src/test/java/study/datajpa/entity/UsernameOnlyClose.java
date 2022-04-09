package study.datajpa.entity;

// 인터페이스 기반 Closed Projections (원하는 값만 가져옴)
public interface UsernameOnlyClose {
    String getUsername();
}
