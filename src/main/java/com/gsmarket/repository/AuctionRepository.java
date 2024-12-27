package com.gsmarket.repository;

import com.gsmarket.entity.AuctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionRepository extends JpaRepository<AuctionEntity, Long> {
    List<AuctionEntity> findByIsEndedFalse(); // 진행 중인 경매 목록 조회
}
