package com.gsmarket.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gsmarket.dto.BoardDTO;
import com.gsmarket.dto.MemberDTO;
import com.gsmarket.dto.NoteDTO;
import com.gsmarket.dto.PageRequestDTO;
import com.gsmarket.dto.PageResultDTO;
import com.gsmarket.entity.Board;
import com.gsmarket.service.BoardService;
import com.gsmarket.service.MemberService;
import com.gsmarket.service.NoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("board")
@Log4j2
@RequiredArgsConstructor
public class BoardController {

	private final BoardService boardService;
	private final MemberService memberService;
	private final NoteService noteService;

	@GetMapping("/register")
	public String getRegister(HttpSession session) {
		String loginId = (String) session.getAttribute("loginId");
		if (loginId == null) { // 로그인되어 있지 않은 경우
			return "redirect:/member/login"; // 로그인 페이지로 이동
		}
		return "board/register"; // 게시판 등록 페이지로 이동
	}

	@PostMapping("/register")
	public String register(BoardDTO boardDTO, RedirectAttributes redirectAttributes, HttpSession session, Model model) {
		log.info("boardDTO : " + boardDTO);

		String loginId = (String) session.getAttribute("loginId");
		if (loginId == null) { // 로그인되어 있지 않은 경우
			return "redirect:/member/login"; // 로그인 페이지로 이동
		}

		boardDTO.setMemberId(loginId);

		Long mno = boardService.register(boardDTO);

		redirectAttributes.addFlashAttribute("msg", mno);

		return "redirect:/";
	}

	@ModelAttribute("sessionMember")
	public MemberDTO getUpdateMember(HttpSession session) {
		String myId = (String) session.getAttribute("loginId");
		MemberDTO memberDTO = memberService.updateForm(myId);
		return memberDTO;
	}

	@GetMapping("/list")
	public void list(PageRequestDTO pageRequestDTO, Model model) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@#!@#!#@!#!@#@!#@!#@!#");
		model.addAttribute("result", boardService.getList(pageRequestDTO));
	}

	@GetMapping({ "/read", "/modify" })
	public void read(Long mno, @ModelAttribute("requestDTO") PageRequestDTO pageRequestDTO, Model model) {

		log.info("글읽기 요청@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		BoardDTO dto = boardService.getBoard(mno);
		model.addAttribute("dto", dto);

	}

	@PostMapping("/modify")
	public String modify(BoardDTO dto, @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
						 RedirectAttributes redirectAttributes, HttpSession session) {
		log.info("@@@@@@@@@@@@@@@Post 게시글 수정 요청--@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		log.info("dto" + dto);

		String message = "";
		// 작성자 검증
		String memberId = (String) session.getAttribute("loginId");
		if (dto.getMemberId().equals(memberId) || memberId.equals("admin")) {

			boardService.modify(dto);
			Long mno = boardService.register(dto);

			redirectAttributes.addAttribute("page", requestDTO.getPage());
			redirectAttributes.addAttribute("mno", dto.getMno());

		} else if (!dto.getMemberId().equals(memberId)) {

			redirectAttributes.addFlashAttribute("message", "작성자만 수정 가능합니다.");
			return "redirect:/board/read?mno=" + dto.getMno();

		}

		return "redirect:/";
	}

	@GetMapping("/mygoods") // 내글보기
	public String mygoods(PageRequestDTO pageRequestDTO, HttpSession session, Model model) {

		String loginId = (String) session.getAttribute("loginId");
		if (loginId == null) { // 로그인되어 있지 않은 경우
			return "redirect:/member/login"; // 로그인 페이지로 이동
		} else {
			model.addAttribute("result", boardService.getMygoods(pageRequestDTO, session));

			return "board/mygoods";
		}
	}

	@PostMapping("/remove")
	public String remove(BoardDTO dto) {
		boardService.delete(dto);
		return "redirect:/";
	}

	@GetMapping("/map")
	public void test(PageRequestDTO pageRequestDTO, Model model) {
		model.addAttribute("result", boardService.getList(pageRequestDTO));
	}

	// 찜하기 기능
	@PostMapping("/like/{mno}")
	public ResponseEntity<String> like(@PathVariable("mno") Long mno, HttpSession session) {

		String member = (String) session.getAttribute("loginId");
		boolean result = boardService.addLike(mno, member);

		if (result) {
			return new ResponseEntity<>("success", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("fail", HttpStatus.OK);
		}
	}

	// 찜취소 기능
	@DeleteMapping("/like/{mno}")
	public ResponseEntity<String> cancelLike(@PathVariable("mno") Long mno, HttpSession session) {
		// 세션에서 현재 사용자 정보를 가져옴
		String member = (String) session.getAttribute("loginId");
		if (member == null) {
			return new ResponseEntity<>("로그인 해주세요", HttpStatus.UNAUTHORIZED);
		}

		// 찜목록에서 해당 글 번호를 삭제하는 로직
		boardService.removeLike(mno, member);

		return new ResponseEntity<>("success", HttpStatus.OK);
	}

	// 찜하기 체크 메서드
	@GetMapping("/checklike/{mno}/{memberId}")
	@ResponseBody
	public boolean checkLike(@PathVariable("mno") Long mno, @PathVariable("memberId") String memberId) {
		boolean result = boardService.checkLike(mno, memberId);
		return result;
	}

	// 찜한물품 리스트
	@GetMapping("/wishlist")
	public String showWishlist(PageRequestDTO requestDTO, Model model, HttpSession session,
							   @RequestParam(defaultValue = "0") int page) {
		int pageSize = 10;
		PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.Direction.DESC, "regDate");
		PageResultDTO<BoardDTO, Board> result = boardService.getWishList(requestDTO, session, page);

		// 각 게시물에 대한 이미지 중 첫 번째 이미지만 가져오도록 수정
		for (BoardDTO boardDTO : result.getDtoList()) {
			if (boardDTO.getImageDTOList() != null && boardDTO.getImageDTOList().size() > 1) {
				boardDTO.setImageDTOList(boardDTO.getImageDTOList().subList(0, 1));
			}
		}
		model.addAttribute("result", result);
		return "board/wishlist";
	}

	// 게시판에서 쪽지 보내기 추가
	@PostMapping("/{mno}/notes")
	public ResponseEntity<String> sendNote(@PathVariable("mno") Long mno, @RequestBody NoteDTO noteDTO,
										   HttpSession session) {

		String memberId = (String) session.getAttribute("loginId"); // 로그인한 회원의 memberId 가져오기

		BoardDTO dto = boardService.getBoard(mno);
		MemberDTO senderDTO = noteService.getMemberInfo(memberId); // 작성자 정보 가져오기
		MemberDTO receiverDTO = noteService.getMemberInfo(dto.getMemberId()); // 작성자 정보 가져오기

		if (senderDTO.getMemberId().equals(receiverDTO.getMemberId())) { // 자신에게 쪽지를 보낼 수 없음
			return new ResponseEntity<>("본인에게는 쪽지를 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
		}

		String content = noteDTO.getContent();
		noteService.sendNote(senderDTO.getMemberId(), receiverDTO.getMemberId(), content); // 쪽지 전송

		return new ResponseEntity<>("쪽지를 보냈습니다.", HttpStatus.OK);
	}

	// 카테고리 추가
	@GetMapping("category/{category}")
	public String getBoardsByCategory(@PathVariable String category, Model model) {
		List<Board> boardList = boardService.getBoardByCategory(category);
		model.addAttribute("boardList", boardList);
		return "board/list";
	}

}