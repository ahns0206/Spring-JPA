package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

//    @PostConstruct
    public void initData() {
        for (int i=0; i<100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }

    @GetMapping("/v1/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    @GetMapping("/v2/members/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        // 도메인 클래스 컨버터 : 트랜잭션 없는 범위이기에 조회용으로만 사용
        return member.getUsername();
    }

    @GetMapping("/v1/members")
    public Page<Member> list(@PageableDefault(size = 5) Pageable pageable) {
        // get : http://localhost:8080/members?page=0&size=3&sort=username,asc
        return memberRepository.findAll(pageable);
    }

    @GetMapping("/v2/members")
    public Page<MemberDTO> list2(@PageableDefault(size = 5) Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberDTO::new);
    }

    @GetMapping("/v3/members")
    public Page<MemberDTO> list3(@PageableDefault(size = 5) @Qualifier("member") Pageable pageable) {
        // @Qualifier : 페이징 정보 둘 이상이면 접두사명 추가해 구분
        // get : http://localhost:8080/v2/members?member_page=0&sort=username,asc
        return memberRepository.findAll(pageable)
                .map(MemberDTO::new);
    }

    @GetMapping("/v4/members")
    public Page<MemberDTO> list4(@PageableDefault(size = 5) Pageable pageable) {
        // get : http://localhost:8080/v2/members?page=0&sort=username,asc
        PageRequest pageRequest = PageRequest.of(1, 2);
        return memberRepository.findAll(pageRequest)
                .map(MemberDTO::new);

    }
}
