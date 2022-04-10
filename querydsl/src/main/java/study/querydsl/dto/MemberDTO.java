package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDTO {

    private String username;
    private int age;

    @QueryProjection //DTO도 Q 파일이 생성되는 annotation
    public MemberDTO(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
