package com.gsmarket.controller;

import com.gsmarket.service.MailService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gsmarket.dto.MemberDTO;
import com.gsmarket.dto.PageRequestDTO;
import com.gsmarket.entity.MemberEntity;
import com.gsmarket.repository.MemberRepository;
import com.gsmarket.service.BoardService;
import com.gsmarket.service.MemberService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("member")
@RequiredArgsConstructor
public class MemberController {
	// MemberService 인스턴스 생성자 주입
	private final MemberRepository memberRepository;
	private final MemberService memberService;
	private final BoardService boardService;
	private final MailService mailService;

	@GetMapping("/index")
	public void list(PageRequestDTO pageRequestDTO, Model model) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@#!@#!#@!#!@#@!#@!#@!#");
		model.addAttribute("result", boardService.getList(pageRequestDTO));
	}

	// 회원가입 페이지 출력 요청처리 GET 맵핑
	@GetMapping("/signup")
	public void saveForm() {
		// save.html 뷰 페이지 출력
	}
	// 회원가입 페이지 출력 요청처리 GET 맵핑
	@GetMapping("/signup2")
	public void saveForm2() {
		// save.html 뷰 페이지 출력
	}

	@ModelAttribute("sessionMember")
	public MemberDTO getUpdateMember(HttpSession session) {
		String myId = (String) session.getAttribute("loginId");
		MemberDTO memberDTO = memberService.updateForm(myId);
		return memberDTO;
	}
	@PostMapping("/signup")
	public ResponseEntity<Map<String, Object>> save(@RequestBody MemberDTO memberDTO) {
		Map<String, Object> response = new HashMap<>();

		// 1. 입력값 검증
		if (memberDTO.getMemberId() == null || memberDTO.getMemberId().isEmpty() ||
				memberDTO.getMemberEmail() == null || memberDTO.getMemberEmail().isEmpty() ||
				memberDTO.getMemberPassword() == null || memberDTO.getMemberPassword().isEmpty()) {
			response.put("success", false);
			response.put("message", "필수 입력값이 누락되었습니다.");
			return ResponseEntity.badRequest().body(response);
		}

		// 2. 이메일 인증 여부 확인
		if (!mailService.isEmailVerified(memberDTO.getMemberEmail())) {
			response.put("success", false);
			response.put("message", "이메일 인증이 완료되지 않았습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		// 3. 중복 회원 확인 (아이디 및 이메일)
		if (memberService.isDuplicateMember(memberDTO.getMemberId(), memberDTO.getMemberEmail())) {
			response.put("success", false);
			response.put("message", "이미 존재하는 아이디 또는 이메일입니다.");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}

		// 4. 회원 저장
		try {
			memberService.save(memberDTO);
			response.put("success", true);
			response.put("message", "가입이 완료되었습니다. 로그인 하세요!");
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (Exception e) {
			// 상세 에러 메시지 로깅
			e.printStackTrace(); // 서버 디버깅 용도
			response.put("success", false);
			response.put("message", "회원가입 중 오류가 발생했습니다. 관리자에게 문의하세요.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// 로그인 페이지 출력 요청처리 GET 맵핑
	@GetMapping("/login")
	public void loginForm() {
	}

	// 로그인 요청 처리 POST 맵핑
	@ResponseBody
	@PostMapping("/login")
	public String login(@ModelAttribute MemberDTO memberDTO, HttpSession session) {

		String message = "";
		MemberDTO loginResult = memberService.login(memberDTO);
		if (loginResult != null) {
			// login 성공
			session.setAttribute("loginId", loginResult.getMemberId());
			message = "<script>alert('환영합니다!');location.href='/';</script>";
			return message;
		} else {
			// login 실패
			message = "<script>alert('아이디와 비밀번호를 확인해주세요');location.href='/member/login';</script>";
			return message;
		}

	}

	@ResponseBody
	@GetMapping("/logout")
	public String logout(HttpServletRequest httpServletRequest) {
		HttpSession httpSession = httpServletRequest.getSession(false);
		String message = "";
		if (httpSession != null) {
			httpSession.invalidate();
		}
		message = "<script>alert('Good Bye!');location.href='/';</script>";
		return message;
	}

	@ResponseBody
	@PostMapping("/logout")
	public String postLogout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		HttpSession httpSession = httpServletRequest.getSession(false);
		String message = "";
		if (httpSession != null) {
			httpSession.invalidate();
		}
		expiredCookie(httpServletResponse, "loginId");// 하단의 쿠키 지우는 메서드 활용
		message = "<script>alert('Good Bye!');location.href='/';</script>";
		return message;
	}

	// 쿠키 지우는 메서드
	private void expiredCookie(HttpServletResponse response, String cookieName) {
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	@GetMapping("/mypage")
	public void updateForm(HttpSession session, Model model) {
		// 현재 로그인한 사용자의 아이디를 세션에서 가져옵니다.
		String myId = (String) session.getAttribute("loginId");

		// 아이디를 통해 회원 정보를 조회합니다.
		MemberDTO memberDTO = memberService.updateForm(myId);

		// 조회된 회원 정보를 모델에 추가합니다.
		model.addAttribute("sessionMember", memberDTO);

	}

	@ResponseBody
	@PostMapping("/mypage")
	public String update(@ModelAttribute MemberDTO memberDTO) {

		String message = "";
		// 받아온 회원 정보를 업데이트 합니다.
		memberService.update(memberDTO);

		// 업데이트된 회원의 상세 정보 페이지로 리다이렉트 합니다.
		message = "<script>alert('회원 정보가 수정되었습니다!');location.href='/member/mypage';</script>";
		return message;
	}

	// HTTP GET 요청을 처리하는 메서드를 정의합니다. 요청 경로는 "/updatePassword"입니다.
	@GetMapping("/updatePassword")
	public void updatePassword(HttpSession session, Model model) {
		// 현재 세션에 저장된 "loginId" 속성 값을 가져와 myId 변수에 저장합니다.
		String myId = (String) session.getAttribute("loginId");

		// 회원 서비스를 사용하여 myId에 해당하는 회원 정보를 가져온 후, MemberDTO 객체로 반환합니다.
		MemberDTO memberDTO = memberService.updateForm(myId);

		// 가져온 회원 정보(MemberDTO 객체)를 모델에 "sessionMember"라는 이름으로 추가합니다.
		// 이를 통해 뷰에서 해당 정보를 사용할 수 있게 됩니다.
		model.addAttribute("sessionMember", memberDTO);

	}

	// HTTP POST 요청을 처리하는 메서드를 정의합니다. 요청 경로는 "/updatePassword"입니다.
	@ResponseBody
	@PostMapping("/updatePassword")
	public String updatePasswordProcess(MemberDTO memberDTO, String newPassword, String newPasswordCheck,
										String currentPassword) {
		String message = "";
		Optional<MemberEntity> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
		MemberEntity memberEntity = byMemberId.get();
		if (!currentPassword.equals(memberDTO.getMemberPassword())) {
			message = "<script>alert('틀린 비밀번호 입니다.');location.href='/member/updatePassword';</script>";
			return message;
		}
		if (!newPassword.equals(newPasswordCheck)) {
			// 새 비밀번호와 확인용 비밀번호가 일치하지 않으면 에러 메시지를 반환합니다.
			message = "<script>alert('신규 비밀번호와 비밀번호 확인이 일치하지 않습니다.');location.href='/member/updatePassword';</script>";
			return message;
		}

		// 비밀번호 변경 로직을 수행하는 서비스 메서드를 호출합니다.
		memberService.updatePassword(memberDTO.getMemberId(), newPassword);

		// 변경이 완료되면 리다이렉트를 통해 원하는 경로로 이동합니다. (예: 메인 페이지)
		message = "<script>alert('비밀번호 변경이 완료되었습니다.');location.href='/member/mypage';</script>";
		return message;
	}

	// 회원 탈퇴 화면 보여주기
	@GetMapping("/delete")
	public String delete(Model model, HttpSession session) {
		String member = (String) session.getAttribute("loginId");
		MemberDTO memberDTO = memberService.findByMemberId(member);
		model.addAttribute("member", memberDTO);
		return "member/delete";
	}

	@DeleteMapping("/member/{memberId}")
	public ResponseEntity<?> deleteMember(@PathVariable String memberId, @RequestBody MemberDTO memberDTO,
										  HttpServletRequest request) {
		boolean isDeleted = memberService.deleteMember(memberId, memberDTO.getMemberPassword());
		if (isDeleted) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	// 아이디 중복검사
	@GetMapping("/idCheck")
	@ResponseBody
	public ResponseEntity<?> overlappedID(@RequestParam String id) {
		boolean isOverlapped = memberService.overlappedID(id);
		return ResponseEntity.ok(isOverlapped);
	}

	// 이메일 중복검사
	@GetMapping("/emailCheck")
	@ResponseBody
	public ResponseEntity<?> overlappedEmail(@RequestParam String email) {
		boolean isOverlapped = memberService.overlappedEmail(email);
		return ResponseEntity.ok(isOverlapped);
	}

	@GetMapping("/findPassword")
	public void findPassword() {

	}

	@PostMapping("/emailDuplication")
	@ResponseBody
	public String emailDuplication(@RequestParam String memberId, @RequestParam String memberEmail) {
		if (memberService.checkInformationDuplication(memberId, memberEmail)) {
			return "no";
		} else {
			return "yes";
		}
	}

//	 이메일 보내기
	@PostMapping("/sendEmail")
	public String sendEmail(@RequestParam("memberEmail") String memberEmail) {
		memberService.resetPassword(memberEmail);
		return "/member/login";
	}

}