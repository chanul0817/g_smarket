
package com.gsmarket.controller;

import com.gsmarket.dto.UploadResultDTO;
import com.gsmarket.entity.AuctionEntity;
import com.gsmarket.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/auction")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final UploadController uploadController;
    private final SimpMessagingTemplate messagingTemplate;

    // 로그인 검증 공통 메서드
    private String checkLogin(HttpSession session, RedirectAttributes redirectAttributes) {
        String loginId = (String) session.getAttribute("loginId");
        if (loginId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인 후 이용해주세요.");
            return "redirect:/member/index";
        }
        return null; // 로그인 상태라면 null 반환
    }

    // 경매 리스트 페이지
    @GetMapping("/list")
    public String auctionList(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 경매 관련 페이지에서만 로그인 체크
        String redirect = checkLogin(session, redirectAttributes);
        if (redirect != null) return redirect;

        List<AuctionEntity> auctions = auctionService.getActiveAuctions();
        model.addAttribute("auctionList", auctions);
        return "member/auctionList";
    }

    // 경매 등록 페이지 매핑
    @GetMapping("/register")
    public String registerAuction(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String loginId = (String) session.getAttribute("loginId");
        if (loginId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인 후 이용해주세요.");
            return "redirect:/member/index"; // 로그인 페이지로 리디렉트
        }
        model.addAttribute("sellerName", session.getAttribute("loginName"));
        return "member/auctionRegister"; // 경매 등록 템플릿 반환
    }


    // 경매 등록 처리
    @PostMapping("/register")
    public String createAuction(@RequestParam String itemName,
                                @RequestParam Double startPrice,
                                @RequestParam("image") MultipartFile[] images,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        // 경매 관련 페이지에서만 로그인 체크
        String redirect = checkLogin(session, redirectAttributes);
        if (redirect != null) return redirect;

        String sellerId = (String) session.getAttribute("loginId");

        try {
            LocalDateTime auctionEndTime = LocalDateTime.now().plusDays(1);

            // 이미지 업로드 처리
            ResponseEntity<List<UploadResultDTO>> response = uploadController.uploadFile(images);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null || response.getBody().isEmpty()) {
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

            List<UploadResultDTO> uploadResult = response.getBody();
            String imagePath = uploadResult.get(0).getFolderPath() + "/" +
                    uploadResult.get(0).getUuid() + "_" +
                    uploadResult.get(0).getFileName();

            // 경매 생성
            auctionService.createAuction(sellerId, itemName, startPrice, auctionEndTime, imagePath);

            return "redirect:/auction/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "경매 등록 실패: " + e.getMessage());
            return "redirect:/auction/register";
        }
    }

    // 경매 상세 조회 페이지
    @GetMapping("/read")
    public String auctionDetail(@RequestParam Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // 경매 관련 페이지에서만 로그인 체크
        String redirect = checkLogin(session, redirectAttributes);
        if (redirect != null) return redirect;

        AuctionEntity auction = auctionService.getAuctionDetails(id);
        model.addAttribute("auction", auction);
        return "member/auctionRead";
    }

    @PostMapping("/bid")
    public String placeBid(@RequestParam Long auctionId,
                           @RequestParam Double bidAmount,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        String bidderId = (String) session.getAttribute("loginId"); // 로그인된 사용자 ID 가져오기

        if (bidderId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인 후 이용해주세요.");
            return "redirect:/member/index"; // 로그인 페이지로 리디렉트
        }

        try {
            AuctionEntity updatedAuction = auctionService.placeBid(auctionId, bidderId, bidAmount);

            // WebSocket 메시지 전송
            messagingTemplate.convertAndSend(
                    "/topic/auction/" + auctionId,
                    updatedAuction.getCurrentBid()
            );

            return "redirect:/auction/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "입찰 실패: " + e.getMessage());
            return "redirect:/auction/read?id=" + auctionId;
        }
    }


    // 경매 종료 처리
    @PostMapping("/end")
    public String endAuction(@RequestParam Long auctionId, HttpSession session, RedirectAttributes redirectAttributes) {
        // 경매 관련 페이지에서만 로그인 체크
        String redirect = checkLogin(session, redirectAttributes);
        if (redirect != null) return redirect;

        auctionService.endAuction(auctionId);
        return "redirect:/auction/list";
    }

    // 경매 취소 처리
    @PostMapping("/cancel")
    public String cancelAuction(@RequestParam Long auctionId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        // 경매 관련 페이지에서만 로그인 체크
        String redirect = checkLogin(session, redirectAttributes);
        if (redirect != null) return redirect;

        String sellerId = (String) session.getAttribute("loginId");
        try {
            auctionService.cancelAuction(auctionId, sellerId);
            return "redirect:/auction/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "경매 취소 실패: " + e.getMessage());
            return "redirect:/auction/read?id=" + auctionId;
        }
    }

}
