
package com.gsmarket.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.gsmarket.dto.BoardDTO;
import com.gsmarket.dto.PageRequestDTO;
import com.gsmarket.dto.PageResultDTO;
import com.gsmarket.entity.Board;
import com.gsmarket.entity.BoardImage;
import com.gsmarket.entity.MemberEntity;
import com.gsmarket.repository.BoardImageRepository;
import com.gsmarket.repository.BoardRepository;
import com.gsmarket.repository.MemberRepository;
import com.gsmarket.repository.NoteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

	private final BoardRepository boardRepository;
	private final BoardImageRepository imageRepository;
	private final MemberRepository memberRepository;
	private final NoteRepository noteRepository;

	@Transactional
	@Override
	public Long register(BoardDTO boardDTO) {

		Map<String, Object> entityMap = BoardDtoToEntity(boardDTO);
		Board board = (Board) entityMap.get("board");

		List<BoardImage> boardImageList = (List<BoardImage>) entityMap.get("imgList");

		// 로그인된 사용자의 회원 정보를 조회
		Optional<MemberEntity> member = memberRepository.findByMemberId(boardDTO.getMemberId());

		// 게시글 작성자 정보를 엔티티에 설정
		board.setMember(member.orElse(null));

		// 게시글 정보와 이미지 정보를 저장
		boardRepository.save(board);

		try {// 이건 이미지없이 글등록시 예외처리

			boardImageList.forEach(boardImage -> {
				if (!boardImage.getUuid().equals("undefined")) {// uuid가 undefined일때
					imageRepository.save(boardImage);
				}

			});
		} catch (Exception e) {

		}

		return board.getMno();

	}

	@Override
	public PageResultDTO<BoardDTO, Object[]> getList(PageRequestDTO requestDTO) {
		Pageable pageable = requestDTO.getPageable(Sort.by("mno").descending());
		Page<Object[]> result;

		if (StringUtils.hasText(requestDTO.getKeyword())) {
			String type = requestDTO.getType();
			String keyword = requestDTO.getKeyword();
			if ("t".equals(type)) { // 제목으로 검색
				result = boardRepository.searchTitle(keyword, pageable);
			} else if ("c".equals(type)) { // 내용으로 검색
				result = boardRepository.searchContent(keyword, pageable);
			} else if ("w".equals(type)) { // 작성자로 검색
				result = boardRepository.searchMemberId(keyword, pageable);
			} else if ("tc".equals(type)) {//제목 + 내용으로 검색
				result = boardRepository.searchTitleContent(keyword, pageable);
			} else { // 모두 검색
				result = boardRepository.searchPage(keyword, pageable);
			}
		} else if (StringUtils.hasText(requestDTO.getCategory())) { // 카테고리로 검색
			String category = requestDTO.getCategory();
			result = boardRepository.searchByCategory(category, pageable);
		} else {
			result = boardRepository.getListPage(pageable);
		}

		Function<Object[], BoardDTO> fn = (arr -> BoardEntitiesToDTO((Board) arr[0],
				(List<BoardImage>) (Arrays.asList((BoardImage) arr[1]))));

		return new PageResultDTO<>(result, fn);
	}

	@Override
	public BoardDTO getBoard(Long mno) {

		Board board = new Board();
		List<BoardImage> boardImageList = new ArrayList<>();
		List<Object[]> result = boardRepository.getBoardWithAll(mno);
		if (!result.isEmpty()) {
			board = (Board) result.get(0)[0];

			result.forEach(arr -> {
				BoardImage boardImage = (BoardImage) arr[1];
				boardImageList.add(boardImage);
			});
		}
		return BoardEntitiesToDTO(board, boardImageList);
	}

	@Override
	@Transactional
	@Modifying
	public void modify(BoardDTO boardDTO) {
		// Entity 리턴됩니다.

		log.info("@@@수정하기@@@@");
		Board board = boardRepository.getById(boardDTO.getMno());

		board.changeTitle(boardDTO.getTitle());
		board.changeContent(boardDTO.getContent());
		// 수정완료되었으니 save() 호출해서 완료 처리합니다.
		boardRepository.save(board);

	}

	@Override
	public Long delete(BoardDTO boardDTO) {
		Board board = boardRepository.getOne(boardDTO.getMno());
		boardRepository.delete(board);
		return board.getMno();
	}

	// 여기서부터 추가

	@Override
	public boolean addLike(Long mno, String memberId) {
		Optional<Board> result = boardRepository.findById(mno);

		if (result.isPresent()) {
			Board board = result.get();
			Optional<MemberEntity> memberResult = memberRepository.findById(memberId);

			if (memberResult.isPresent()) {
				MemberEntity member = memberResult.get();

				if (board.getLikedMembers().contains(member)) {
					return false;
				} else {
					board.getLikedMembers().add(member);
					member.getLikedBoards().add(board);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean removeLike(Long mno, String memberId) {
		Optional<Board> result = boardRepository.findById(mno);

		if (result.isPresent()) {
			Board board = result.get();
			Optional<MemberEntity> memberResult = memberRepository.findById(memberId);

			if (memberResult.isPresent()) {
				MemberEntity member = memberResult.get();

				if (board.getLikedMembers().contains(member)) {
					board.getLikedMembers().remove(member);
					member.getLikedBoards().remove(board);
					return true;
				} else {
					return false;
				}
			}
		}

		return false;
	}

	@Override
	public boolean checkLike(Long mno, String memberId) {
		Optional<Board> result = boardRepository.findById(mno);

		if (result.isPresent()) {
			Board board = result.get();
			Optional<MemberEntity> memberResult = memberRepository.findById(memberId);

			if (memberResult.isPresent()) {
				MemberEntity member = memberResult.get();

				return board.getLikedMembers().contains(member);
			}
		}

		return false;
	}

	@Override
	public PageResultDTO<BoardDTO, Object[]> getMygoods(PageRequestDTO requestDTO, HttpSession session) {
		Pageable pageable = requestDTO.getPageable(Sort.by("mno").descending());
		String memberId = (String) session.getAttribute("loginId");
		Page<Object[]> result = boardRepository.getMyBoardList(memberId, pageable);
		Function<Object[], BoardDTO> fn = (arr -> BoardEntitiesToDTO((Board) arr[0],
				(List<BoardImage>) (Arrays.asList((BoardImage) arr[1]))));

		return new PageResultDTO<>(result, fn);
	}

	@Override
	public PageResultDTO<BoardDTO, Board> getWishList(PageRequestDTO requestDTO, HttpSession session, int page) {

		Pageable pageable = requestDTO.getPageable(Sort.by("mno").descending());
		String loggedInMemberId = (String) session.getAttribute("loginId");
		MemberEntity member = memberRepository.findById(loggedInMemberId)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 ID"));

		int pageSize = 10;
		Page<Board> wishlistPage = boardRepository.findByLikedMembersContainsOrderByRegDateDesc(member, pageable);

		Function<Board, BoardDTO> fn = (board -> BoardEntitiesToDTO(board, board.getImageList()));
		return new PageResultDTO<>(wishlistPage, fn);

	}

	@Override
	public List<Board> getBoardByCategory(String category) {
		return boardRepository.findByCategory(category);
	}



}
