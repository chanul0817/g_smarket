package com.gsmarket.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "note")
public class Note {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long noteId;
	
	@Column(nullable = false)
	private String content;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id")
	private MemberEntity sender;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id")
	private MemberEntity receiver;
	
	@Column(nullable = false)
	private LocalDateTime sendDate;
	
	@Column(nullable = false)
	private boolean checked;

}
