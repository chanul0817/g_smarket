package com.gsmarket.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import com.gsmarket.dto.BoardDTO;
import com.gsmarket.dto.BoardImageDTO;
import com.gsmarket.dto.PageRequestDTO;
import com.gsmarket.dto.PageResultDTO;
import com.gsmarket.entity.Board;
import com.gsmarket.entity.BoardImage;
import com.gsmarket.entity.MemberEntity;

public interface BoardService {

	Long register(BoardDTO boardDTO);

	BoardDTO getBoard(Long mno);

	void modify(BoardDTO boardDTO);

	Long delete(BoardDTO boardDTO);

	PageResultDTO<BoardDTO, Object[]> getList(PageRequestDTO requestDTO); //목록처리

	boolean addLike(Long mno, String memberId);

	boolean removeLike(Long mno, String memberId);

	boolean checkLike(Long mno, String memberId);

	public List<Board> getBoardByCategory(String category);

	public PageResultDTO<BoardDTO, Object[]> getMygoods(PageRequestDTO requestDTO, HttpSession session);

	PageResultDTO<BoardDTO, Board> getWishList(PageRequestDTO requestDTO, HttpSession session, int page);




	default BoardDTO BoardEntitiesToDTO(Board board, List<BoardImage> boardImages) {


		BoardDTO boardDTO = BoardDTO.builder()
				.mno(board.getMno())
				.content(board.getContent())
				.title(board.getTitle())
				.regDate(board.getRegDate())
				.modDate(board.getModDate())
				.memberId(board.getMember().getMemberId())
				.category(board.getCategory())
				.price(board.getPrice())
				.location(board.getLocation())
				.build();

		try {//이미지 업로드가 없을시 목록처리

			List<BoardImageDTO> boardImageDTOList = boardImages.stream().map(boardImage -> {
				return BoardImageDTO.builder().imgName(boardImage.getImgName())
						.path(boardImage.getPath())
						.uuid(boardImage.getUuid())
						.build();
			}).collect(Collectors.toList());

			boardDTO.setImageDTOList(boardImageDTOList);
		} catch (Exception e) {

		}
		return boardDTO;
	}

	default Map<String, Object> BoardDtoToEntity(BoardDTO boardDTO){ //map 타입으로 변환

		Map<String, Object> entityMap = new HashMap<>();

		MemberEntity member = MemberEntity.builder().memberId(boardDTO.getMemberId()).build();

		Board board = Board.builder()
				.mno(boardDTO.getMno())
				.title(boardDTO.getTitle())
				.content(boardDTO.getContent())
				.member(member)
				.category(boardDTO.getCategory())
				.price(boardDTO.getPrice())
				.location(boardDTO.getLocation())
				.build();

		entityMap.put("board",board);

		List<BoardImageDTO> imageDTOList = boardDTO.getImageDTOList();

		//boardImageDTO 처리

		if(imageDTOList != null && imageDTOList.size() > 0) {
			List<BoardImage> boardImageList = imageDTOList.stream().map(boardImageDTO ->{
				BoardImage boardImage = BoardImage.builder()
						.path(boardImageDTO.getPath())
						.imgName(boardImageDTO.getImgName())
						.uuid(boardImageDTO.getUuid())
						.board(board)
						.build();
				return boardImage;

			}).collect(Collectors.toList());

			entityMap.put("imgList", boardImageList);
		}
		return entityMap;


	}



}