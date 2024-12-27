package com.gsmarket.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gsmarket.dto.BoardDTO;
import com.gsmarket.dto.MemberDTO;
import com.gsmarket.dto.NoteDTO;
import com.gsmarket.dto.PageRequestDTO;
import com.gsmarket.service.BoardService;
import com.gsmarket.service.MemberService;
import com.gsmarket.service.NoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("note")
@Log4j2
@RequiredArgsConstructor
public class NoteController {

	@Autowired
	private final BoardService boardService;
	private final NoteService noteService;
	private final MemberService memberService;
	
	
	
	@GetMapping("/list")
	public void list(PageRequestDTO pageRequestDTO, Model model) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@#!@#!#@!#!@#@!#@!#@!#");
		model.addAttribute("result", boardService.getList(pageRequestDTO));
	}

	// 게시판에서 쪽지 보내기
	@PostMapping("/{mno}/notes")
	public ResponseEntity<String> sendNote(@PathVariable("mno") Long mno, @RequestBody NoteDTO noteDTO,
			HttpSession session) {

		String memberId = (String) session.getAttribute("loginId"); // 로그인한 회원의 memberId 가져오기

		BoardDTO dto = boardService.getBoard(mno);// 게시물 정보 가져오기
		MemberDTO senderDTO = noteService.getMemberInfo(memberId); // 작성자 정보 가져오기
		MemberDTO receiverDTO = noteService.getMemberInfo(dto.getMemberId()); // 작성자 정보 가져오기

		if (senderDTO.getMemberId().equals(receiverDTO.getMemberId())) { // 자신에게 쪽지를 보낼 수 없음
			return new ResponseEntity<>("본인에게는 쪽지를 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
		}

		String content = noteDTO.getContent();
		noteService.sendNote(senderDTO.getMemberId(), receiverDTO.getMemberId(), content); // 쪽지 전송

		return new ResponseEntity<>("쪽지를 보냈습니다.", HttpStatus.OK);
	}
	
	@PostMapping("/notes")
	public ResponseEntity<String> sendNote2(@RequestBody NoteDTO noteDTO, HttpSession session) {
	    String sender = noteDTO.getSender(); // 쪽지를 보내는 회원
	    String receiver = noteDTO.getReceiver(); // 쪽지를 받는 회원
	    String content = noteDTO.getContent(); // 쪽지 내용

	    // sender와 receiver 정보를 이용해 회원 ID 가져오기
	    MemberDTO senderDTO = noteService.getMemberInfo(sender);
	    MemberDTO receiverDTO = noteService.getMemberInfo(receiver);

	    if (senderDTO.getMemberId().equals(receiverDTO.getMemberId())) { // 자신에게 쪽지를 보낼 수 없음
	        return new ResponseEntity<>("본인에게는 쪽지를 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
	    }

	    noteService.sendNote(senderDTO.getMemberId(), receiverDTO.getMemberId(), content); // 쪽지 전송
	    System.out.println("@@@@@@@@@@@@@@@@@@"+sender);
	    System.out.println("@@@@@@@@@@@@@@@@@@@@"+receiver);

	    return new ResponseEntity<>("쪽지를 보냈습니다.", HttpStatus.OK);
	}

	 
	//내 대화창 열기
	@GetMapping("/mynotes")
	public String myNotes(Model model, HttpSession session,PageRequestDTO pageRequestDTO) {
		String userId = (String) session.getAttribute("loginId");
		 if (userId == null) {
		        return "redirect:/member/login";
		    }
		List<List<NoteDTO>> conversationList = noteService.getConversationList(userId);
		//날짜 순서대로 정렬
		conversationList.forEach(conversation -> conversation.sort(Comparator.comparing(NoteDTO::getSendDate)));
		model.addAttribute("result", boardService.getList(pageRequestDTO));
		model.addAttribute("conversationList", conversationList);
		return "note/mynotes";
	}

	@ModelAttribute("sessionMember")
	public MemberDTO getUpdateMember(HttpSession session) {
		String myId = (String) session.getAttribute("loginId");
		MemberDTO memberDTO = memberService.updateForm(myId);
		return memberDTO;
	}


	//쪽지 확인 메서드(15초~(mynotes에서 수정가능)마다 새로운 쪽지가 왔는지 확인)
	@GetMapping("/check")
	public ResponseEntity<Boolean> checkNewNote(HttpSession session) {
		String loginId = (String) session.getAttribute("loginId");
		List<List<NoteDTO>> conversationList = noteService.getConversationList(loginId);
		boolean hasNewNote = false;
		for (List<NoteDTO> conversation : conversationList) {
			for (NoteDTO note : conversation) {
				if (note.getReceiver().equals(loginId) && !note.isChecked()) {
					hasNewNote = true;
					break;
				}
			}
			if (hasNewNote) {
				break;
			}
		}
		session.setAttribute("notes", conversationList);
		return ResponseEntity.ok(hasNewNote);
	}


	//대화 리스트를 눌렀을시 쪽지 확인 메서드
	@PutMapping("/check/{sender}-{receiver}")
	public Map<String, Object> checkNoteBySenderAndReceiver(@PathVariable("sender") String sender,
	        @PathVariable("receiver") String receiver, HttpSession session) {
	    
	    String userId = (String) session.getAttribute("loginId");
	    if (sender.equals(userId)) {
	    	noteService.checkNoteBySenderAndReceiver(receiver, sender);
	    	
	    } else {
	    	noteService.checkNoteBySenderAndReceiver(sender, receiver);
	    }
	    
	    Map<String, Object> resultMap = new HashMap<>();
	    resultMap.put("result", "success");
	    return resultMap;
	}
}
