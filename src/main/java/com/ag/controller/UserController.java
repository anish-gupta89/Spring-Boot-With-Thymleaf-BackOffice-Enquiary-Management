package com.ag.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.ag.AppConst;
import com.ag.model.LoginForm;
import com.ag.model.SignUpForm;
import com.ag.model.UnlockForm;
import com.ag.service.UserService;

@Controller
public class UserController {

	@Autowired
	UserService userService;
	
	
	@PostMapping(value = "/login")
	public String doLogin(@Valid @ModelAttribute("logindata") LoginForm loginRqst, 
			BindingResult result, Model model) {
		System.out.println(loginRqst);
		if (result.hasErrors()) {
			return AppConst.LOGIN;
		}
		
		String isSuccess = userService.login(loginRqst);
		
		if(isSuccess.equalsIgnoreCase(AppConst.LOGIN_SUCCESS)) {
			return"redirect:"+AppConst.DASHBOARD;
		}
		model.addAttribute("errMsg", isSuccess);
		return AppConst.LOGIN;
	}
	
	
	@PostMapping(value = "/signup")
	public String signUp(@Valid @ModelAttribute("signupdata") SignUpForm signUpRqst, 
			BindingResult result, Model model) {
		System.out.println(signUpRqst);
		if (result.hasErrors()) {
			return AppConst.SIGN_UP;
		}
		String signUp = userService.signUp(signUpRqst);
		if(signUp.equalsIgnoreCase(AppConst.SIGNUP_SUCCESS)) {
			model.addAttribute("msg", AppConst.SIGNUP_SUCCESS);
		} else {
			model.addAttribute("msg", AppConst.SIGNUP_FAILURE);
		}
		return AppConst.SIGN_UP;
	}
	
	
	@PostMapping(value = "/unlock")
	public String signUp(@Valid @ModelAttribute("unlockacc") UnlockForm unlockRqst, 
			BindingResult result, Model model) {
		System.out.println(unlockRqst);
		if (result.hasErrors()) {
			return AppConst.UNLOCK_ACC;
		}
		String unlockAcc = userService.unlockAccount(unlockRqst);
		if(unlockAcc.equalsIgnoreCase(AppConst.UNLOCK_ACCOUNT_FAILURE_PASSWORD)) {
			model.addAttribute("msg", AppConst.UNLOCK_ACCOUNT_FAILURE_PASSWORD);
			return AppConst.UNLOCK_ACC;
		}
		return "redirect:/"+AppConst.LOGIN;
	}

}
