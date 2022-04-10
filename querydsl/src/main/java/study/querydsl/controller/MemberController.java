package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDTO;
import study.querydsl.repository.MemberJpaRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition memberSearchCondition) {
        // http://localhost:8080/v1/members?teamName=teamA&username=member32&ageGoe=31&ageLoe=35
        return memberJpaRepository.searchByBooleanExpression(memberSearchCondition);
    }
}