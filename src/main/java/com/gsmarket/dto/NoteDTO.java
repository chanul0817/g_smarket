package com.gsmarket.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.gsmarket.entity.MemberEntity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NoteDTO implements Serializable {
	private Long noteId;
	private String content;
	private String sender;
	private String receiver;
	private LocalDateTime sendDate;
	private boolean checked;

	
	public NoteDTO(Long noteId, String content, MemberEntity sender, MemberEntity receiver, LocalDateTime sendDate, boolean checked) {
        this.noteId = noteId;
        this.content = content;
        this.sender = sender.toString();
        this.receiver = receiver.toString();
        this.sendDate = sendDate;
        this.checked = checked;
    }
}
