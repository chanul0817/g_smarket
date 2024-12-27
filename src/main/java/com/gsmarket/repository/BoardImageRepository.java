
package com.gsmarket.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gsmarket.entity.BoardImage;

public interface BoardImageRepository extends JpaRepository<BoardImage, Long>{

	//글수정시 이미지파일 삭제(x버튼)을 누를경우 DB에서도 삭제하는 기능(uuid가 맞으면 삭제)
	@Transactional
	@Modifying
	@Query("DELETE FROM BoardImage bi WHERE bi.uuid = :uuid")
	int deleteByUuid(@Param("uuid") String uuid);

}
