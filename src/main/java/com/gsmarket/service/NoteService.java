package com.gsmarket.service;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.gsmarket.dto.MemberDTO;
import com.gsmarket.dto.NoteDTO;

public interface NoteService {

	public MemberDTO getMemberInfo(String memberId);

	public void sendNote(String senderId, String receiverId, String content);

	public List<NoteDTO> getSentNoteList(String senderId); //보낸사람 쪽지 리스트

	public List<NoteDTO> getReceivedNoteList(String receiverId); //받는사람 쪽지 리스트

	public List<List<NoteDTO>> getConversationList(String userId); //대화형식(날짜순서대로) 바꾸기

	public void checkNoteBySenderAndReceiver(String senderId, String receiverId);//쪽지 확인하기

	public boolean hasNewNote(String loginId, HttpSession session);

}