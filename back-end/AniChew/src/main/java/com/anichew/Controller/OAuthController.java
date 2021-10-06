package com.anichew.Controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anichew.Response.LoginResponse;
import com.anichew.Service.KaKaoAPI;
import com.anichew.Service.UserService;

import io.swagger.annotations.ApiOperation;


@RequestMapping("oauth/")
@RestController
public class OAuthController {
	
	@Autowired
	private KaKaoAPI kakao;
	
	@Autowired
	private UserService userService;	
	
	@ApiOperation("로그인")
	@GetMapping(value="/login")
	public ResponseEntity<LoginResponse> login (@RequestParam("code") String code, HttpServletRequest httpServletReq, HttpServletResponse httpServletRes) {
		
		String access_token = kakao.getAccessToken(code);		
		Map<String,Object> userInfo = kakao.getUserInfo(access_token);
				
		LoginResponse response = new LoginResponse();
		
		if(userService.existsUser(Long.parseLong((String)userInfo.get("id")))) {
			userService.signUp(userInfo);
			response.setNewUser(true);
		}
		
		String jwt = userService.generateToken(httpServletReq, httpServletRes, (String)userInfo.get("id"));
		response.setToken(jwt);
		
		
		return new ResponseEntity<LoginResponse>(response,HttpStatus.OK);
	}
	
	@GetMapping(value="/logout")
	public ResponseEntity<String> logout (HttpServletRequest httpServletReq) {
		
		if(!userService.checkToken(httpServletReq))
			return new ResponseEntity<String>("NOT FOUND TOKEN", HttpStatus.UNAUTHORIZED);
		
		userService.logout(httpServletReq);
		
		return new ResponseEntity<String>("logout",HttpStatus.OK);
	}
	
	
	
}
