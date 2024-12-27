
package com.gsmarket.service;

import com.gsmarket.entity.AuctionEntity;
import com.gsmarket.entity.MemberEntity;
import com.gsmarket.repository.AuctionRepository;
import com.gsmarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;

    private static final String AUCTION_NOT_FOUND = "경매를 찾을       수 없습니다.";
    private static final String MEMBER_NOT_FOUND = "사용자를 찾을 수 없습니다.";
    private static final String AUCTION_ENDED = "경매가 이미 종료되었습니다.";
    private static final String BID_TOO_LOW = "입찰 금액은 현재 입찰가보다 높아야 합니다.";
    private static final String IMAGE_UPLOAD_DIR = "/path/to/upload/";  // 이미지 업로드 경로 설정

    public AuctionEntity createAuction(String sellerId, String itemName, Double startPrice, LocalDateTime endTime, String imagePath) throws IOException {
        MemberEntity seller = findMemberById(sellerId);

        AuctionEntity auction = AuctionEntity.builder()
                .seller(seller)
                .itemName(itemName)
                .startPrice(startPrice)
                .currentBid(startPrice)
                .auctionEndTime(endTime)
                .isEnded(false)
                .imageURL(imagePath)  // 이미지 경로 저장
                .build();

        return auctionRepository.save(auction);
    }

    @Transactional
    public AuctionEntity placeBid(Long auctionId, String bidderId, Double bidAmount) {
        AuctionEntity auction = findAuctionById(auctionId);
        validateAuctionIsActive(auction);

        MemberEntity bidder = findMemberById(bidderId);
        validateBidAmount(auction, bidAmount);

        auction.placeBid(bidder, bidAmount); // 최고 입찰가와 최고 입찰자 업데이트
        return auctionRepository.save(auction); // 변경 사항 저장
    }


    @Transactional
    public void endAuction(Long auctionId) {
        AuctionEntity auction = findAuctionById(auctionId);
        auction.endAuction();
        auctionRepository.save(auction);
    }

    public List<AuctionEntity> getActiveAuctions() {
        return auctionRepository.findByIsEndedFalse();
    }

    public AuctionEntity getAuctionDetails(Long auctionId) {
        return findAuctionById(auctionId);
    }

    private AuctionEntity findAuctionById(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException(AUCTION_NOT_FOUND));
    }

    public MemberEntity findMemberById(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(MEMBER_NOT_FOUND));
    }

    private void validateAuctionIsActive(AuctionEntity auction) {
        if (auction.getIsEnded()) {
            throw new IllegalStateException(AUCTION_ENDED);
        }
    }

    private void validateBidAmount(AuctionEntity auction, Double bidAmount) {
        if (bidAmount <= auction.getCurrentBid()) {
            throw new IllegalArgumentException(BID_TOO_LOW);
        }
    }

    public String saveImage(MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            throw new IllegalArgumentException("이미지가 없습니다.");
        }

        String fileName = UUID.randomUUID().toString() + "-" + image.getOriginalFilename();
        Path path = Paths.get(IMAGE_UPLOAD_DIR + fileName);

        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        Files.copy(image.getInputStream(), path);

        return path.toString();  // 저장된 파일 경로 반환
    }
    @Transactional
    public List<AuctionEntity> checkExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<AuctionEntity> activeAuctions = auctionRepository.findByIsEndedFalse();

        List<AuctionEntity> endedAuctions = new ArrayList<>();
        for (AuctionEntity auction : activeAuctions) {
            if (auction.getAuctionEndTime().isBefore(now)) {
                auction.endAuction(); // 경매 종료 상태 변경
                auctionRepository.save(auction); // 상태 업데이트
                endedAuctions.add(auction);
            }
        }
        return endedAuctions; // 종료된 경매 목록 반환
    }

    @Transactional
    public void cancelAuction(Long auctionId, String sellerId) {
        AuctionEntity auction = findAuctionById(auctionId);
        if (!auction.getSeller().getMemberId().equals(sellerId)) {
            throw new IllegalStateException("본인이 등록한 경매만 취소할 수 있습니다.");
        }
        auctionRepository.delete(auction);
    }
    @Transactional
    public void endAuctionEarly(Long auctionId, String sellerId) {
        AuctionEntity auction = findAuctionById(auctionId);

        // 등록자가 맞는지 확인
        if (!auction.getSeller().getMemberId().equals(sellerId)) {
            throw new IllegalStateException("본인이 등록한 경매만 조기 종료할 수 있습니다.");
        }

        // 경매 종료
        auction.endAuction();
        auctionRepository.save(auction);
    }

}
