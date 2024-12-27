package com.gsmarket.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AuctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private MemberEntity seller; // 경매 등록자

    @ManyToOne
    @JoinColumn(name = "current_bidder_id") // 최고 입찰자
    private MemberEntity currentBidder;

    private String itemName;
    private Double startPrice;
    private Double currentBid; // 현재 입찰가
    private LocalDateTime auctionEndTime;
    private Boolean isEnded;
    private String imageURL;

    @Builder
    public AuctionEntity(MemberEntity seller, String itemName, Double startPrice, Double currentBid,
                         LocalDateTime auctionEndTime, Boolean isEnded, String imageURL, MemberEntity currentBidder) {
        this.seller = seller;
        this.itemName = itemName;
        this.startPrice = startPrice;
        this.currentBid = currentBid;
        this.auctionEndTime = auctionEndTime;
        this.isEnded = isEnded;
        this.imageURL = imageURL;
        this.currentBidder = currentBidder; // 최고 입찰자 설정
    }

    // 입찰 처리 메서드
    public void placeBid(MemberEntity bidder, Double bidAmount) {
        if (bidAmount > this.currentBid) {
            this.currentBid = bidAmount;
            this.currentBidder = bidder; // 최고 입찰자 변경
        } else {
            throw new IllegalArgumentException("입찰 금액은 현재 입찰가보다 높아야 합니다.");
        }
    }

    // 경매 종료 처리
    public void endAuction() {
        this.isEnded = true;
    }
}
