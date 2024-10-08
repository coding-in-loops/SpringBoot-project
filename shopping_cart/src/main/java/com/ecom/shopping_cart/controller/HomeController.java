package com.ecom.shopping_cart.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.shopping_cart.model.Category;
import com.ecom.shopping_cart.model.Product;
import com.ecom.shopping_cart.model.UserDtls;
import com.ecom.shopping_cart.service.CartService;
import com.ecom.shopping_cart.service.CategoryService;
import com.ecom.shopping_cart.service.ProductService;
import com.ecom.shopping_cart.service.UserService;
import com.ecom.utils.CommonUtil;
import io.micrometer.common.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	CategoryService categoryService;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	CommonUtil util;
	
	@Autowired
	BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	CartService cartService;
	
	@Autowired
	CommonUtil commonUtil;
	
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
	
	
	@GetMapping("/")
	public String index(Model m) {
		List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
				.sorted(Comparator.comparing(Category::getId).reversed())
				.limit(6).toList();
		List<Product> allActiveProduct = productService.getAllActiveProducts("").stream()
				.sorted(Comparator.comparing(Product::getId).reversed())
				.limit(8).toList();
		m.addAttribute("products", allActiveProduct);
		m.addAttribute("category", allActiveCategory);
		return "index";
	}
	
	@GetMapping("/signin")
	public String login() {
		return "login";
	}
	
	@GetMapping("/register")
	public String register() {
		return "register";
	}
	
	@GetMapping("/products")
	public String products(Model m, 
	        @RequestParam(value = "category", defaultValue = "") String category,
	        @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	        @RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
	        @RequestParam(defaultValue = "") String ch) {

	    Page<Product> page = null;
	    if (StringUtils.isEmpty(ch)) {
	        page = productService.getAllActiveProductsPagination(pageNo, pageSize, category);
	    } else {
	        page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
	    }

	    List<Product> products = page.getContent();
	    m.addAttribute("products", products);
	    m.addAttribute("productSize", products.size());
	    m.addAttribute("pageNo", page.getNumber());
	    m.addAttribute("pageSize", pageSize);
	    m.addAttribute("totalElements", page.getTotalElements());
	    m.addAttribute("totalPages", page.getTotalPages());
	    m.addAttribute("isFirst", page.isFirst());
	    m.addAttribute("isLast", page.isLast());

	    return "products";
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable int id,Model m) {
		Product productById=productService.getProductById(id);
		m.addAttribute("product", productById);
		return "view_product";
	}
	
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user,@RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
		
		Boolean existsEmail=userService.existsEmail(user.getEmail());
		
		
		if(existsEmail) {
			session.setAttribute("errorMessage", "Email already exists!");
		}else {

			String imageName=file.isEmpty()? "default.jpg":file.getOriginalFilename() ;
			user.setProfileImage(imageName);
			UserDtls saveUser=userService.saveUser(user);

			if(!ObjectUtils.isEmpty(saveUser)) {
				if(!file.isEmpty()) {
					File saveFile = new ClassPathResource("static/img").getFile();

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
							+ file.getOriginalFilename());

					 System.out.println(path);
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					
				}session.setAttribute("successMessage", "Saved successfully!");
			}else {
				session.setAttribute("errorMessage", "Something went wrong on server!");
			}
		}
		
		return "redirect:/register";
	}
	
	
	//Forgot password logic
	@GetMapping("/forgot-password")
	public String showForgotPassword() {
		return "forgot_password.html";
	}
	
	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email,HttpSession session,HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		email = email.trim(); 
		UserDtls userEmail=userService.getUserByEmail(email);
		if(ObjectUtils.isEmpty(userEmail)) {
			session.setAttribute("errorMessage", "Invalid Email");
		}else {
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);

			// Generate URL :
			// http://localhost:8080/reset-password?token=sfgdbgfswegfbdgfewgvsrg

			String url = commonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;

			Boolean sendMail = commonUtil.sendMail(url, email);

			if (sendMail) {
				session.setAttribute("successMessage", "Please check your email..Password Reset link sent");
			} else {
				session.setAttribute("errorMessage", "Somethong wrong on server ! Email not send");
			}
		}

		return "redirect:/forgot-password";
	}
	
	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession session,Model m) {
		UserDtls userByToken = userService.getUserByToken(token);

		if (userByToken == null) {
			m.addAttribute("message", "Your link is invalid or expired !!");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
	}
	
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token,@RequestParam String password,HttpSession session,Model m) {
		UserDtls userByToken = userService.getUserByToken(token);
		if (userByToken == null) {
			m.addAttribute("errorMessage", "Your link is invalid or expired !!");
			return "message";
		} else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			// session.setAttribute("succMsg", "Password change successfully");
			m.addAttribute("message", "Password change successfully");

			return "message";
		
	    }
	}
	
	@GetMapping("/search")
	public String searchProduct(@RequestParam String ch,Model m) {
		List<Product> searchProduct=productService.searchProduct(ch);
		m.addAttribute("products", searchProduct);
		List<Category> categories=categoryService.getAllActiveCategory();
		m.addAttribute("categories",categories);
		return "products";
	}
	
}
