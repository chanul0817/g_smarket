package com.gsmarket.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.gsmarket.dto.MemberDTO;
import com.gsmarket.entity.MemberEntity;
import com.gsmarket.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

//import javax.mail.MessagingException;
//import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class MemberService {

	@Autowired
//	private JavaMailSender mailSender;
	private final MemberRepository memberRepository;
	private final JavaMailSender mailSender;

	public MemberDTO save(MemberDTO memberDTO) {
		// 중복 체크
		if (memberRepository.findByMemberId(memberDTO.getMemberId()).isPresent()) {
			throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
		}

		if (memberRepository.findByMemberEmail(memberDTO.getMemberEmail()).isPresent()) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}

		// DTO -> Entity 변환 및 저장
		MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);
		memberRepository.save(memberEntity);

		return memberDTO;
	}



	public MemberDTO login(MemberDTO memberDTO) {
		/*
		 * 1. 회원이 입력한 아이디로 DB 에서 조회를 함 2. DB 에서 조회한 비밀번호와 사용자가 입력한 비밀번호가 일치하는지 판단
		 */
		Optional<MemberEntity> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
		if (byMemberId.isPresent()) {
			// 조회 결과가 있다 ( 해당 아이디를 가진 회원 정보가 있다.)
			MemberEntity memberEntity = byMemberId.get();
			if (memberEntity.getMemberPassword().equals(memberDTO.getMemberPassword())) {
				// 비밀번호 일치 하는 경우
				// entity -> dto 변환 후 리턴
				MemberDTO dto = MemberDTO.toMemberDTO(memberEntity);
				return dto;
			} else {
				// 비밀번호 불일치(로그인실패)
				return null;
			}
		} else {
			// 조회 결과가 없다 ( 해당 아이디 가진 회원이 없다)
			return null;
		}
	}

	// updateForm 메서드는 입력받은 회원 ID(myId)를 사용하여 회원 정보를 조회하고, 결과를 MemberDTO 객체로 반환합니다.
	public MemberDTO updateForm(String myId) {
		// 회원 저장소에서 입력받은 회원 ID를 사용하여 회원 정보를 조회합니다.
		// 조회 결과는 Optional<MemberEntity> 타입으로 반환되어 optionalMemberEntity 변수에 저장됩니다.
		Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberId(myId);

		// optionalMemberEntity가 존재하는지 확인합니다.
		if (optionalMemberEntity.isPresent()) {
			// 존재한다면, optionalMemberEntity의 값을 가져와 MemberDTO로 변환한 후 반환합니다.
			// 이를 위해 MemberDTO 클래스의 toMemberDTO 메서드를 사용합니다.
			return MemberDTO.toMemberDTO(optionalMemberEntity.get());
		} else {
			// 존재하지 않는다면, null 값을 반환합니다.
			return null;
		}
	}

	// 회원 정보를 업데이트하는 메서드입니다.
	public void update(MemberDTO memberDTO) {
		// memberDTO 객체를 MemberEntity 객체로 변환한 후, 저장소에 저장합니다.
		memberRepository.save(MemberEntity.toMemberEntity((memberDTO)));
	}

	public void updatePassword(String memberId, String newPassword) {
		// memberId에 해당하는 회원을 찾아서 비밀번호를 변경합니다.

		Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberId(memberId);

		if (optionalMemberEntity.isPresent()) {
			MemberEntity memberEntity = optionalMemberEntity.get();
			memberEntity.setMemberPassword(newPassword);

			// 변경된 비밀번호를 저장합니다.
			memberRepository.save(memberEntity);
		} else {
			// memberId에 해당하는 회원이 없을 경우 에러 처리를 수행합니다.
			// 이 부분은 필요에 따라 적절한 에러 처리를 구현하세요.
			throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
		}
	}

	// 저장소에 저장된 모든 회원 정보를 조회하는 메서드입니다.
	public List<MemberDTO> findAll() {
		// 저장소에서 모든 회원 정보를 조회한 후, MemberEntity 객체로 구성된 리스트를 반환합니다.
		List<MemberEntity> memberEntityList = memberRepository.findAll();

		// MemberEntity 객체를 MemberDTO 객체로 변환한 후 저장할 리스트를 생성합니다.
		List<MemberDTO> memberDTOList = new ArrayList<>();

		// memberEntityList에 저장된 각 회원 정보를 MemberDTO 객체로 변환하여 memberDTOList에 추가합니다.
		for (MemberEntity memberEntity : memberEntityList) {
			memberDTOList.add(MemberDTO.toMemberDTO(memberEntity));
		}

		// memberDTOList를 반환합니다.
		return memberDTOList;
	}
	public boolean isDuplicateMember(String memberId, String memberEmail) {
		return memberRepository.findByMemberId(memberId).isPresent()
				|| memberRepository.findByMemberEmail(memberEmail).isPresent();
	}

	// 주어진 회원 ID(memberId)에 해당하는 회원 정보를 조회하는 메서드입니다.
	public MemberDTO findByMemberId(String memberId) {
		// 저장소에서 주어진 회원 ID로 회원 정보를 조회한 후, Optional<MemberEntity> 객체로 반환합니다.
		Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberId(memberId);

		// optionalMemberEntity가 존재하는지 확인합니다.
		if (optionalMemberEntity.isPresent()) {
			// 존재한다면, optionalMemberEntity의 값을 가져와 MemberDTO로 변환한 후 반환합니다.
			return MemberDTO.toMemberDTO(optionalMemberEntity.get());
		} else {
			// 존재하지 않는다면, null 값을 반환합니다.
			return null;
		}
	}

	// 회원 탈퇴 메서드
	public boolean deleteMember(String memberId, String memberPassword) {
		Optional<MemberEntity> memberOptional = memberRepository.findById(memberId);
		if (memberOptional.isPresent()) {
			MemberEntity member = memberOptional.get();
			if (member.getMemberPassword().equals(memberPassword)) {
				memberRepository.deleteById(memberId);
				return true;
			}
		}
		return false;
	}

	// 여기서부터 추가
	// 대화 신청을 위해 로그인한사람인지 확인하기
	public MemberEntity getCurrentUser(HttpSession session) {
		String loginId = (String) session.getAttribute("loginId");
		if (loginId == null) {
			return null;
		}
		return memberRepository.findByMemberId(loginId).orElse(null);
	}

	public MemberEntity getMemberById(String memberId) {
		// memberId에 해당하는 MemberEntity 객체를 데이터베이스에서 조회하여 반환하는 코드를 작성합니다.
		// 이 부분은 MemberRepository를 이용하여 구현할 수 있습니다.
		return memberRepository.findById(memberId).orElse(null);
	}

	// 회원중복검사
	public boolean overlappedID(String memberId) {
		Optional<MemberEntity> memberOptional = memberRepository.findByMemberId(memberId);
		return memberOptional.isPresent();
	}

	// Email중복검사
	public boolean overlappedEmail(String memberEmail) {
		Optional<MemberEntity> memberOptional = memberRepository.findByMemberEmail(memberEmail);
		return memberOptional.isPresent();
	}

	public boolean checkInformationDuplication(String memberId, String memberEmail) {
		Optional<MemberEntity> optionalMember = memberRepository.findByMemberIdAndMemberEmail(memberId, memberEmail);
		return optionalMember.isPresent();
	}

	public void resetPassword(String userEmail) {
		String tempPassword = generateTempPassword(); // 임시 비밀번호 생성
		System.out.println("생성된 임시 비밀번호: " + tempPassword); // 로그 추가

		Optional<MemberEntity> member = memberRepository.findByMemberEmail(userEmail);
		if (member.isPresent()) {
			MemberEntity m = member.get();
			// 비밀번호 변경 로직
			updateTPassword(m.getMemberId(), tempPassword);
			System.out.println("비밀번호 변경 완료: " + m.getMemberId()); // 로그 추가

			// 이메일 발송 로직
			String message = "임시 비밀번호는 " + tempPassword + "입니다. 로그인 후 새로운 비밀번호로 변경해주세요.";
			try {
				sendResetPasswordEmail(m.getMemberEmail(), "임시 비밀번호 발급 안내", message);
				System.out.println("이메일 전송 성공: " + m.getMemberEmail()); // 로그 추가
			} catch (MessagingException e) {
				System.err.println("이메일 전송 실패: " + e.getMessage()); // 예외 로그
			}
		} else {
			System.out.println("해당 이메일로 회원을 찾을 수 없습니다: " + userEmail);
		}
	}

	public void sendResetPasswordEmail(String userEmail, String title, String message) throws MessagingException {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

		helper.setTo(userEmail);
		helper.setSubject(title);
		helper.setText(message, true); // HTML 포맷으로 메세지 전송

		mailSender.send(mimeMessage);
		System.out.println("이메일이 성공적으로 발송되었습니다: " + userEmail); // 로그 추가
	}



	private String generateTempPassword() {
		char[] charSet = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
				'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
				'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
		StringBuilder sb = new StringBuilder();
		Random random = new Random();

		// 10자리 임시 비밀번호 생성
		for (int i = 0; i < 10; i++) {
			int idx = random.nextInt(charSet.length);
			sb.append(charSet[idx]);
		}

		return sb.toString();
	}

	private void updateTPassword(String memberId, String memberPassword) {
		Optional<MemberEntity> member = memberRepository.findById(memberId);
		member.ifPresent(m -> {
			m.setMemberPassword(memberPassword);
			memberRepository.save(m);
		});
	}

}