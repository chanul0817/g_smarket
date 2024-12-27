
package com.gsmarket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gsmarket.entity.Board;
import com.gsmarket.entity.MemberEntity;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

	@Query("select b, bi from Board b left outer join BoardImage bi on bi.board = b group by b")
	Page<Object[]> getListPage(Pageable pageable);

	@Query("select b, bi" + " from Board b left outer join BoardImage bi on bi.board = b "
			+ " where b.mno = :mno group by bi")
	List<Object[]> getBoardWithAll(@Param("mno") Long mno);

	// 제목으로 검색
	@Query("select b, bi from Board b left outer join BoardImage bi on bi.board = b " + "where b.title like %:keyword% "
			+ "group by b.mno, b.title, b.content, b.regDate, b.modDate, bi")
	Page<Object[]> searchTitle(@Param("keyword") String keyword, Pageable pageable);

	// 작성자로 검색
	@Query("SELECT b, bi FROM Board b " + "JOIN b.member m " + "LEFT OUTER JOIN BoardImage bi ON bi.board = b "
			+ "WHERE m.memberId LIKE %:keyword% " + "GROUP BY b.mno, b.title, b.content, b.regDate, b.modDate, m, bi")
	Page<Object[]> searchMemberId(@Param("keyword") String keyword, Pageable pageable);

	// 내용 필드 검색
	@Query("select b, bi from Board b left outer join BoardImage bi on bi.board = b "
			+ "where b.content like %:keyword% " + "group by b.mno, b.title, b.content, b.regDate, b.modDate, bi")
	Page<Object[]> searchContent(@Param("keyword") String keyword, Pageable pageable);

	// 모든 제목 + 내용 검색
	@Query("select b, bi from Board b left outer join BoardImage bi on bi.board = b "
			+ "where concat(b.title, b.content) like %:keyword% "
			+ "group by b.mno, b.title, b.content, b.regDate, b.modDate, bi")
	Page<Object[]> searchTitleContent(@Param("keyword") String keyword, Pageable pageable);

	// 제목 + 내용 + 작성자
	@Query("SELECT b, bi FROM Board b " + "JOIN b.member m " + "LEFT OUTER JOIN BoardImage bi ON bi.board = b "
			+ "WHERE b.title LIKE %:keyword% OR b.content LIKE %:keyword% OR m.memberId LIKE %:keyword%  "
			+ "GROUP BY b.mno")
	Page<Object[]> searchPage(@Param("keyword") String keyword, Pageable pageable);

	//카테고리로 검색
	@Query("SELECT b, bi FROM Board b LEFT OUTER JOIN BoardImage bi ON bi.board = b " +
			"WHERE b.category = :category " +
			"GROUP BY b.mno")
	Page<Object[]> searchByCategory(@Param("category") String category, Pageable pageable);

	@Query("select b, bi from Board b left join b.imageList bi left join b.member m where m.memberId = :memberId group by b.mno order by b.regDate DESC")
	Page<Object[]> getMyBoardList(@Param("memberId") String memberId, Pageable pageable);

	Page<Board> findByLikedMembersContainsOrderByRegDateDesc(MemberEntity member, Pageable pageable);

	List<Board> findByCategory(String category);

}
