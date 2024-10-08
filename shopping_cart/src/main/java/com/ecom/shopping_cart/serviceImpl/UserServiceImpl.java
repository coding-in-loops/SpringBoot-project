package com.ecom.shopping_cart.serviceImpl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.shopping_cart.model.UserDtls;
import com.ecom.shopping_cart.repository.UserRepository;
import com.ecom.shopping_cart.service.UserService;
import com.ecom.utils.AppConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService{

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Override
	public UserDtls saveUser(UserDtls user) {
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		user.setLockTime(null);
		
		String encodePassowrd=passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassowrd);
		UserDtls saveUser=userRepository.save(user);
		return saveUser;
	}

	@Override
	public UserDtls getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDtls> getUsers(String role) {
		return userRepository.findByRole(role);
	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		Optional<UserDtls> findByUser=userRepository.findById(id);
		if(findByUser.isPresent()) {
			UserDtls user= findByUser.get();
			user.setIsEnable(status);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void increaseFailedAttempt(UserDtls user) {
		int attempt=user.getFailedAttempt()+1;
		user.setFailedAttempt(attempt);
		userRepository.save(user);
	}

	@Override
	public void userAccountLock(UserDtls user) {
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepository.save(user);
	}

	@Override
	public Boolean unlockAccountTimeExpired(UserDtls user) {
		if (user.getLockTime() == null) {
	        return false;
	    }

	    long lockTime = user.getLockTime().getTime();
	    long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
	    long currTime = System.currentTimeMillis();

	    if (unlockTime < currTime) {
	        user.setAccountNonLocked(true);
	        user.setFailedAttempt(0);
	        user.setLockTime(null);
	        userRepository.save(user);
	        return true;
	    }
	    return false;
	}

	@Override
	public void resetAttempt(int userId) {
		Optional<UserDtls> userOpt = userRepository.findById(userId);
	    if (userOpt.isPresent()) {
	        UserDtls user = userOpt.get();
	        user.setFailedAttempt(0);
	        userRepository.save(user);
	    } else {
	        logger.warn("User with ID {} not found, cannot reset attempt.", userId);
	    }
		
	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {
		UserDtls findByEmail=userRepository.findByEmail(email);
		findByEmail.setResetToken(resetToken);
		userRepository.save(findByEmail);
	}

	@Override
	public UserDtls getUserByToken(String token) {
		return userRepository.findByResetToken(token);
	}

	@Override
	public UserDtls updateUser(UserDtls user) {
		return userRepository.save(user);
	}

	@Override
	public UserDtls updateUserProfile(UserDtls user,MultipartFile img) {
		UserDtls dbUser = userRepository.findById(user.getId()).get();

		if (!img.isEmpty()) {
			dbUser.setProfileImage(img.getOriginalFilename());
		}

		if (!ObjectUtils.isEmpty(dbUser)) {

			dbUser.setName(user.getName());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			dbUser = userRepository.save(dbUser);
		}

		try {
			if (!img.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				java.nio.file.Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ img.getOriginalFilename());

//			System.out.println(path);
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dbUser;
	}

	@Override
	public UserDtls saveAdmin(UserDtls user) {
		user.setRole("ROLE_ADMIN");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		user.setLockTime(null);
		
		String encodePassowrd=passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassowrd);
		UserDtls saveUser=userRepository.save(user);
		return saveUser;
	}

	@Override
	public Boolean existsEmail(String email) {
		return userRepository.existsByEmail(email);
	}
	

}