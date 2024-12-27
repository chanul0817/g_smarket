
package com.gsmarket.dto;

import java.util.List;

import java.time.LocalDateTime;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {

	private Long mno;
	private String title;
	private String content;
	private String memberId;
	private String category;
	private int price;
	private boolean liked;
	private String location;


	@Builder.Default
	private List<BoardImageDTO> imageDTOList = new ArrayList<>();

	private int replyCnt;

	private LocalDateTime regDate, modDate;
}
