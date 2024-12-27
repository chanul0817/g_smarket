
package com.gsmarket.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.gsmarket.dto.MemberDTO;
import com.gsmarket.dto.NoteDTO;
import com.gsmarket.entity.MemberEntity;
import com.gsmarket.entity.Note;
import com.gsmarket.repository.MemberRepository;
import com.gsmarket.repository.NoteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class NoteServiceimpl implements NoteService {

	private final MemberRepository memberRepository;
	private final NoteRepository noteRepository;

	@Override
	public MemberDTO getMemberInfo(String memberId) {// 회원조회
		MemberEntity memberEntity = memberRepository.getById(memberId);
		ModelMapper modelMapper = new ModelMapper();
		MemberDTO memberDTO = modelMapper.map(memberEntity, MemberDTO.class);
		return memberDTO;
	}

	private Note toNoteEntity(NoteDTO noteDTO, MemberEntity sender, MemberEntity receiver) {
		Note note = new Note();
		note.setContent(noteDTO.getContent());
		note.setSender(sender);
		note.setReceiver(receiver);
		note.setSendDate(LocalDateTime.now());
		note.setChecked(false);
		return note;
	}

	@Override
	public void sendNote(String senderId, String receiverId, String content) {//게시판에서 쪽지 보내기
		MemberEntity senderEntity = memberRepository.getById(senderId);
		MemberEntity receiverEntity = memberRepository.getById(receiverId);

		NoteDTO noteDTO = new NoteDTO();
		noteDTO.setContent(content);
		Note note = toNoteEntity(noteDTO, senderEntity, receiverEntity);
		noteRepository.save(note);

	}


	@Override
	public List<NoteDTO> getSentNoteList(String senderId) {//보낸사람 쪽지 리스트
		List<Note> sentNoteEntityList = noteRepository.findAllBySenderMemberIdOrderBySendDateDesc(senderId);
		return sentNoteEntityList.stream().map(noteEntity -> {
			NoteDTO noteDTO = new NoteDTO();
			noteDTO.setNoteId(noteEntity.getNoteId());
			noteDTO.setSender(noteEntity.getSender().getMemberId());
			noteDTO.setReceiver(noteEntity.getReceiver().getMemberId());
			noteDTO.setContent(noteEntity.getContent());
			noteDTO.setSendDate(noteEntity.getSendDate());
			noteDTO.setChecked(noteEntity.isChecked());
			return noteDTO;
		}).collect(Collectors.toList());
	}

	@Override
	public List<NoteDTO> getReceivedNoteList(String receiverId) {//받는사람 쪽지 리스트
		List<Note> receivedNoteEntityList = noteRepository.findAllByReceiverMemberIdOrderBySendDateDesc(receiverId);
		return receivedNoteEntityList.stream().map(noteEntity -> {
			NoteDTO noteDTO = new NoteDTO();
			noteDTO.setNoteId(noteEntity.getNoteId());
			noteDTO.setSender(noteEntity.getSender().getMemberId());
			noteDTO.setReceiver(noteEntity.getReceiver().getMemberId());
			noteDTO.setContent(noteEntity.getContent());
			noteDTO.setSendDate(noteEntity.getSendDate());
			noteDTO.setChecked(noteEntity.isChecked());
			return noteDTO;
		}).collect(Collectors.toList());
	}

	private String getConversationKey(NoteDTO note, String userId) {
		String senderId = note.getSender();
		String receiverId = note.getReceiver();

		// 현재 사용자가 발신자인 경우
		if (senderId.equals(userId)) {
			return receiverId + "-" + senderId;
		}
		// 현재 사용자가 수신자인 경우
		else if (receiverId.equals(userId)) {
			return senderId + "-" + receiverId;
		}
		// 현재 사용자와 관련 없는 쪽지인 경우
		else {
			return null;
		}
	}

	@Override
	public List<List<NoteDTO>> getConversationList(String userId) {
		List<NoteDTO> sentNoteList = getSentNoteList(userId);
		List<NoteDTO> receivedNoteList = getReceivedNoteList(userId);

		List<NoteDTO> mergedNoteList = new ArrayList<>(sentNoteList);
		mergedNoteList.addAll(receivedNoteList);

		// 대화별로 구분하기 위한 Map
		Map<String, List<NoteDTO>> conversationMap = new HashMap<>();
		for (NoteDTO note : mergedNoteList) {
			String conversationKey = getConversationKey(note, userId);
			if (conversationMap.containsKey(conversationKey)) {
				conversationMap.get(conversationKey).add(note);
			} else {
				List<NoteDTO> conversationList = new ArrayList<>();
				conversationList.add(note);
				conversationMap.put(conversationKey, conversationList);
			}
		}
		List<List<NoteDTO>> conversationList = new ArrayList<>(conversationMap.values());
		return conversationList;
	}
	@Override
	public boolean hasNewNote(String loginId, HttpSession session) {
		List<NoteDTO> notes = (List<NoteDTO>) session.getAttribute("notes");
		System.out.println("이건서비스!!"+notes);
		if (notes == null) {
			return false;
		}
		for (NoteDTO note : notes) {
			if (note.getReceiver().equals(loginId) && !note.isChecked()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void checkNoteBySenderAndReceiver(String senderId, String receiverId) {
		MemberEntity senderEntity = memberRepository.getById(senderId);
		MemberEntity receiverEntity = memberRepository.getById(receiverId);
		List<Note> noteList = noteRepository.findBySenderAndReceiverAndChecked(senderEntity, receiverEntity, false);
		for (Note note : noteList) {
			note.setChecked(true);
			noteRepository.save(note);
		}
	}


}
