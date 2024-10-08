package com.ecom.shopping_cart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecom.shopping_cart.model.Cart;

@Service
public interface CartService {

	public Cart saveCart(Integer productId,Integer userId);
	
	public List<Cart> getCartsByUser(Integer userId);
	
	public Integer getCountCart(Integer userId);

	public void updateQuantity(String sy, Integer cid);
}
