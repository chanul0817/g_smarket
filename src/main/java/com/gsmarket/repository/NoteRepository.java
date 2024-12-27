package com.gsmarket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gsmarket.entity.MemberEntity;
import com.gsmarket.entity.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {

	
	List<Note> findAllByOrderBySendDateDesc();

	List<Note> findAllBySenderMemberIdOrderBySendDateDesc(String senderId);

	List<Note> findAllByReceiverMemberIdOrderBySendDateDesc(String receiverId);

	List<Note> findBySenderAndReceiverAndChecked(String senderId, String receiverId, boolean b);

	List<Note> findBySenderAndReceiverAndChecked(MemberEntity senderEntity, MemberEntity receiverEntity, boolean b);

}
