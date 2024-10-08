package com.ecom.shopping_cart.controller;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.shopping_cart.model.Cart;
import com.ecom.shopping_cart.model.Category;
import com.ecom.shopping_cart.model.OrderRequest;
import com.ecom.shopping_cart.model.ProductOrder;
import com.ecom.shopping_cart.model.UserDtls;
import com.ecom.shopping_cart.service.CartService;
import com.ecom.shopping_cart.service.CategoryService;
import com.ecom.shopping_cart.service.OrderService;
import com.ecom.shopping_cart.service.UserService;
import com.ecom.utils.CommonUtil;
import com.ecom.utils.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserService userService;
	
	@Autowired
	CartService cartService;
	
	@Autowired
	CategoryService categoryService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@GetMapping("/")
	public String home() {
		return "user/home";
	}
	
	@ModelAttribute
	public void getUserDetails(Principal p,Model m) {
		if(p!=null) {
			String email=p.getName();
			UserDtls user=userService.getUserByEmail(email);
			m.addAttribute("user",user);
			Integer countCart=cartService.getCountCart(user.getId());
			m.addAttribute("countCart",countCart);
		}
		List<Category> allActiveCategory=categoryService.getAllActiveCategory();
		m.addAttribute("categories",allActiveCategory);
	}
	
	@GetMapping("/addCart")
	public String addTocart(@RequestParam Integer pid,@RequestParam Integer uid,HttpSession session) {
		Cart saveCart=cartService.saveCart(pid, uid);
		if(ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMessage", "Product add to cart failed!");
		}else {
			session.setAttribute("successMessage", "Product added to cart!");
		}
		return "redirect:/product/"+pid;
	}
	
	@GetMapping("/cart")
	public String loadCartPage(Principal p,Model m) {
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if(carts.size()>0) {
		m.addAttribute("totalOrderPrice",carts.get(carts.size()-1).getTotalOrderAmount());
		}
		return "/user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy,@RequestParam Integer cid) {
		cartService.updateQuantity(sy,cid);
		return "redirect:/user/cart";
	}
	
	
	private UserDtls getLoggedInUserDetails(Principal p) {
		String email=p.getName();
		UserDtls userDtls=userService.getUserByEmail(email);
		return userDtls;
	}
	
	@GetMapping("/orders")
	public String orderPage(Principal p,Model m) {
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if(carts.size()>0) {
			Double orderPrice=carts.get(carts.size()-1).getTotalOrderAmount();
			Double totalOrderPrice=carts.get(carts.size()-1).getTotalOrderAmount()+250+100;
			m.addAttribute("orderPrice", orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/order";
	}
	
	@PostMapping("/save-order")
	public String orderPage(@ModelAttribute OrderRequest request,Principal p) {
		System.out.print(request);
		UserDtls user=getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), request);
		return "redirect:/user/success";
	}
	
	@GetMapping("/success")
	public String loadSuccess() {
		return "/user/success";
	}
	
	@GetMapping("/user-orders")
	public String myOrder(Model m,Principal p) {
		UserDtls user =getLoggedInUserDetails(p);
		List<ProductOrder> orders=orderService.getOrderByUser(user.getId());
		m.addAttribute("orders", orders);
		return "/user/my_orders";
	}
	
	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id,@RequestParam Integer st,HttpSession session) {
		String status=null;
		OrderStatus[] values=OrderStatus.values();
		for(OrderStatus orderSt:values) {
			if(orderSt.getId().equals(st)) {
				status=orderSt.getName();
			}
		}
		
		ProductOrder updateOrder=orderService.updateOrderStatus(id, status);
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		if(!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("successMessage", "Status Updated!");
		}else {
			session.setAttribute("errorMessage", "Status not Updated!");
		}
		return "redirect:/user/user-orders";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return "user/profile";
	}
	
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user,@RequestParam MultipartFile img,HttpSession session) {
		UserDtls updateUser=userService.updateUserProfile(user, img);
		if(ObjectUtils.isEmpty(updateUser)) {
			session.setAttribute("errorMessage","Profile not updated!");
		}else {
			session.setAttribute("successMessage", "Profile Updated!");
		}
		return "redirect:/user/profile";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword,@RequestParam String oldPassword,
			Principal p,HttpSession session){
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);
		Boolean f=passwordEncoder.matches(oldPassword, loggedInUserDetails.getPassword());
		if(f) {
			String encodePassword=passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			userService.updateUser(loggedInUserDetails);
			if(ObjectUtils.isEmpty(encodePassword)) {
				session.setAttribute("errorMessage", "Password not Updated!! Error in server");
			}else {
				session.setAttribute("successMessage", "Password Updated Successfully!");
			}
		}else {
			session.setAttribute("errorMessage", "Current password incorrect!");
		}
		return "redirect:/user/profile";
	}
	
}
