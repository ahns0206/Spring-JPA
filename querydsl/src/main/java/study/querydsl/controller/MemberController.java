package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDTO;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition condition) {
        // http://localhost:8080/v1/members?teamName=teamA&username=member32&ageGoe=31&ageLoe=35
        return memberJpaRepository.searchByBooleanExpression(condition);
    }
    @GetMapping("v2/members")
    public Page<MemberTeamDTO> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        // http://localhost:8080/v2/members?teamName=teamA&username=member32&ageGoe=31&ageLoe=35&page=0&size=100
        // 총 100개가 안되는 데이터에 size 100으로 설정한 경우: count 쿼리 나감
        return memberRepository.searchPageSimple(condition, pageable);
    }
    @GetMapping("v3/members")
    public Page<MemberTeamDTO> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        // http://localhost:8080/v3/members?teamName=teamA&username=member32&ageGoe=31&ageLoe=35&page=0&size=100
        // 총 100개가 안되는 데이터에 size 100으로 설정한 경우: count 쿼리 안나감
        return memberRepository.searchPageComplex(condition, pageable);
    }
}
