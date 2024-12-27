
package com.gsmarket.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "member")
@Getter
@Setter
public class Board extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mno;
	private String title;
	private String content;
	private String category;
	private int price;
	private String location;

	@ManyToOne(fetch = FetchType.LAZY) //무조건 lazy로
	@JoinColumn(name = "member_id")
	@JsonIgnore
	private MemberEntity member;

	//찜하기 기능 추가
	@ManyToMany(mappedBy = "likedBoards", cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<MemberEntity> likedMembers = new HashSet<>();

	//	//movie와 movieimage 엔티티 OneToMany(1:N(다)) 속성 cascade 하여 글삭제시 자동으로 첨부파일(이미지)도 삭제함
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<BoardImage> imageList = new ArrayList<>();


	public void changeTitle(String title) {
		this.title = title;
	}

	public void changeContent(String content) {
		this.content = content;
	}

}
