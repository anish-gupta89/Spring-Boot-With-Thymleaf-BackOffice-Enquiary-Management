package com.ag.service;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ag.AppConst;
import com.ag.db.UserDetailsEntity;
import com.ag.db.UserDetailsRepository;
import com.ag.model.EmailDetails;
import com.ag.model.LoginForm;
import com.ag.model.SignUpForm;
import com.ag.model.UnlockForm;
import com.ag.utils.EmailUtils;
import com.ag.utils.TempPswrdGenerator;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserDetailsRepository userRepository;

	@Autowired
	EmailUtils emailUtils;

	@Autowired
	TempPswrdGenerator pswrdGenerator;
	
	@Value("${spring.application.serverURL}")
	String urlForUnlockAccount;
	
	@Autowired
	HttpSession session;

	@Override
	public String login(LoginForm formData) {

		String status;

		Optional<UserDetailsEntity> userData = userRepository.findByUserEmailAndPassword(formData.getEmail(),
				formData.getPassword());
		if (userData.isPresent()) {
			if (userData.get().getAccountStatus().equals(AppConst.ACCOUNT_STATUS_UNLOCKED)) {
				session.setAttribute("userId", userData.get().getUserId());
				System.out.println("User ID"+userData.get().getUserId());
				status = AppConst.LOGIN_SUCCESS;
			} else {
				status = AppConst.ACCOUNT_STATUS_LOCKED;
			}
		} else {
			status = AppConst.LOGIN_FAILURE;
		}
		return status;
	}

	@Override
	public String signUp(SignUpForm formData) {
		String status;
		Optional<UserDetailsEntity> findByUserEmail = userRepository.findByUserEmail(formData.getUserEmail());
		if (findByUserEmail.isPresent()) {
			status = AppConst.SIGNUP_FAILURE;
		} else {
			UserDetailsEntity userData = new UserDetailsEntity();
			BeanUtils.copyProperties(formData, userData);
			userData.setAccountStatus(AppConst.ACCOUNT_STATUS_LOCKED);

			String temporaryPwd = pswrdGenerator.generatePassword(3).toString();

			userData.setPassword(temporaryPwd);

			userRepository.save(userData);

			EmailDetails emailDetails = new EmailDetails();
			emailDetails.setToEmailId(formData.getUserEmail());
			// emailDetails.setToEmailId("anish.thread01@gmail.com");
			emailDetails.setEmailSubject(AppConst.EMAIL_SUBJECT);

			StringBuffer sb = new StringBuffer();
			sb.append("HI " + formData.getUserName());
			sb.append(
					"<h4>Thanks for doing sign-up. Below is the temporary password for your account, could you unlock your account with that.</h4>");
			sb.append("</br>");
			sb.append("</br>");
			sb.append("<b>Your temporary password is: </b><b><i>" + temporaryPwd + "</i></b>");
			sb.append("</br>");
			sb.append("</br>");
			sb.append("<p>Please click here to unlock your account.<p> <a href=\""+ urlForUnlockAccount
					+ formData.getUserEmail() + "\"> Click Here</a></br></br>");
			sb.append("</br>");
			sb.append("</br>");
			sb.append("Thanks");
			sb.append("<b> Ashok IT</b>");
			String message = sb.toString();

			emailDetails.setEmailBody(message);
			emailUtils.sendMailWithTempPassword(emailDetails);

			status = AppConst.SIGNUP_SUCCESS;
		}
		return status;
	}

	@Override
	public String unlockAccount(UnlockForm formData) {
		if (!formData.getPassword().equals(formData.getConfirmPassword())) {
			return AppConst.UNLOCK_ACCOUNT_FAILURE_PASSWORD;
		}
		Optional<UserDetailsEntity> userData = userRepository.findByUserEmail(formData.getEmail());
		if (userData.isPresent()) {
			UserDetailsEntity userEntity = userData.get();
			userEntity.setPassword(formData.getPassword());
			userEntity.setAccountStatus(AppConst.ACCOUNT_STATUS_UNLOCKED);
			userRepository.save(userEntity);
		}
		return AppConst.UNLOCK_ACCOUNT_SUCCESS;
	}

	@Override
	public String forgotPwd(UnlockForm formData) {
		Optional<UserDetailsEntity> userData = userRepository.findByUserEmail(formData.getEmail());	
		String status;
		
		if (userData.isPresent()) {
			UserDetailsEntity userEntity = userData.get();
			
			EmailDetails emailDetails = new EmailDetails();
			emailDetails.setToEmailId(formData.getEmail());
			emailDetails.setEmailSubject(AppConst.EMAIL_SUBJECT);

			StringBuffer sb = new StringBuffer();
			sb.append("HI " + userEntity.getUserName());
			sb.append(
					"<h4>Your password is given below.</h4>");
			sb.append("</br>");
			sb.append("</br>");
			sb.append("<b>Your password is: </b><b><i>" + userEntity.getUserName() + "</i></b>");
			sb.append("</br>");
			sb.append("</br>");
			sb.append("Thanks");
			sb.append("<b> Ashok IT</b>");
			String message = sb.toString();

			emailDetails.setEmailBody(message);
			emailUtils.sendMailWithTempPassword(emailDetails);
			status = AppConst.FORGOT_PASSWORD_SUCCESS;
		} else {
			status = AppConst.FORGOT_PASSWORD_FAILURE;
		}
		return status;
	}

}
