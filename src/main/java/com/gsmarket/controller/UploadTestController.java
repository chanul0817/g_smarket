package com.gsmarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller //웹문서 전체를 주고받을때 사용 데이터만 주고받을떄는 @RestController
public class UploadTestController {
	
	@GetMapping("/uploadEx")
	public void uploadEx() {
		
	}
}
