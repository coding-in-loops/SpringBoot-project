package com.ecom.shopping_cart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecom.shopping_cart.model.UserDtls;
import com.ecom.shopping_cart.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserDtls user=userRepository.findByEmail(username);
		
		if(user==null) {
			throw new UsernameNotFoundException("user not found!");
		}
		return new CustomUser(user);
	}

}
