package com.ecom.shopping_cart.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.shopping_cart.model.Category;
import com.ecom.shopping_cart.model.Product;
import com.ecom.shopping_cart.model.ProductOrder;
import com.ecom.shopping_cart.model.UserDtls;
import com.ecom.shopping_cart.service.CartService;
import com.ecom.shopping_cart.service.CategoryService;
import com.ecom.shopping_cart.service.OrderService;
import com.ecom.shopping_cart.service.ProductService;
import com.ecom.shopping_cart.service.UserService;
import com.ecom.utils.CommonUtil;
import com.ecom.utils.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	CartService cartService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	CommonUtil commonUtil;
	
	@GetMapping("/")
	public String index() {
		return "/admin/index";
	}
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void getUserDetails(Principal p,Model m) {
		if(p!=null) {
			String email=p.getName();
			UserDtls user=userService.getUserByEmail(email);
			m.addAttribute("user",user);
		}
		List<Category> allActiveCategory=categoryService.getAllActiveCategory();
		m.addAttribute("categories",allActiveCategory);
	}
	
	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> categories = categoryService.getAllCategories();
		m.addAttribute("categories", categories);
		return "/admin/add_product";
	}
	
	@GetMapping("/category")
	public String category(Model m,@RequestParam(name="pageNo",defaultValue="0")Integer pageNo,
			@RequestParam(name="pageSize",defaultValue="5")Integer pageSize) {
		//m.addAttribute("categories",categoryService.getAllCategories());
		
		Page<Category> page=categoryService.getAllCategoryPagination(pageNo, pageSize);
		 
		List<Category> categories=page.getContent();
		
		m.addAttribute("categories", categories);
		m.addAttribute("category", new Category());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "/admin/category";
	}
	
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category,@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());

		if (existCategory) {
			session.setAttribute("errorMessage", "Category Name already exists");
		} else {

			Category saveCategory = categoryService.saveCategory(category);

			if (ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMessage", "Not saved ! internal server error");
			} else {

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());

				 //System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				session.setAttribute("successMessage", "Saved successfully");
			}
		}

		return "redirect:/admin/category";
	}
	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id,HttpSession session) {
		Boolean deleteCategory=categoryService.deleteCategory(id);
		if(deleteCategory) {
			session.setAttribute("successMessage", "Category deleted successfully!");
		}else {
			session.setAttribute("errorMessage", "Something wrong on server!");
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id,Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}
	
	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		Category oldCategory = categoryService.getCategoryById(category.getId());
	    if (oldCategory == null) {
	        session.setAttribute("errorMessage", "Category not found");
	        return "redirect:/admin/category"; // Add redirect if not found
	    }

	    // Set properties
	    oldCategory.setName(category.getName());
	    oldCategory.setIsActive(category.getIsActive()); 

	    String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
	    oldCategory.setImageName(imageName);

	    // Save the category
	    Category updatedCategory = categoryService.saveCategory(oldCategory);

	    if (!ObjectUtils.isEmpty(updatedCategory)) {
	        if (!file.isEmpty()) {
	            File saveFile = new ClassPathResource("static/img").getFile();
	            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	            System.out.print(path);
	        }
	        session.setAttribute("successMessage", "Category updated successfully");
	    } else {
	        session.setAttribute("errorMessage", "Failed to update category");
	    }

	    return "redirect:/admin/loadEditCategory/" + category.getId();
	}
	
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
	    String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
	    product.setImage(imageName);
	    product.setDiscount(0);
	    product.setDiscountPrice(product.getPrice());

	    Product savedProduct = productService.saveProduct(product);
	    if (!ObjectUtils.isEmpty(savedProduct)) {
	        // Get the target directory
	        File saveFile = new ClassPathResource("static/img").getFile();
	        Path targetDirectory = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img");

	        // Create the directory if it doesn't exist
	        if (!Files.exists(targetDirectory)) {
	            Files.createDirectories(targetDirectory);
	        }

	        Path path = targetDirectory.resolve(imageName);
	        try {
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	            session.setAttribute("successMessage", "Product saved successfully!");
	        } catch (IOException e) {
	            session.setAttribute("errorMessage", "Failed to save product image: " + e.getMessage());
	            e.printStackTrace();
	        }
	    } else {
	        session.setAttribute("errorMessage", "Something went wrong on the server!");
	    }

	    return "redirect:/admin/loadAddProduct";
	}

	
	@GetMapping("/products")
	public String loadViewProduct(Model m,@RequestParam(defaultValue="") String ch,@RequestParam(name="pageNo",defaultValue="0")Integer pageNo,
			@RequestParam(name="pageSize",defaultValue="10")Integer pageSize) {
//		List<Product> products=null;
//		if(ch!=null && ch.length()>0) {
//			products=productService.searchProduct(ch);
//		}else {
//			products=productService.getAllProducts();
//		}
//		m.addAttribute("products",products);
		
		Page<Product> page=null;
		if(ch!=null && ch.length()>0) {
			page=productService.searchProductPagination(pageNo,pageSize,ch);
		}else {
			page=productService.getAllProductsPagination(pageNo,pageSize);
		}
		m.addAttribute("products",page.getContent());
		
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "admin/products";
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id,HttpSession session) {
		Boolean deleteProduct=productService.deleteProduct(id);
		if(deleteProduct) {
			session.setAttribute("successMessage", "Product deleted Successfully!");
		}else {
			session.setAttribute("errorMessage", "Something went wrong on server!");
		}
		return "redirect:/admin/products";
	}
	
	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id,Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories",categoryService.getAllCategories());
		return "admin/edit_product";
	}
	
	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product,@RequestParam("file") MultipartFile file, HttpSession session) {
		
		if(product.getDiscount()<0 || product.getDiscount()>100) {
			session.setAttribute("errorMessage", "Invalid discount!");
		}else {
		
		Product updateProduct=productService.updateProduct(product, file);
		if(!ObjectUtils.isEmpty(updateProduct)) {
			session.setAttribute("successMessage", "Product updated successfully!");
		}else {
			session.setAttribute("errorMessage", "Something went wrong on server!");
		}
		}
		return "redirect:/admin/editProduct/"+product.getId();
	}
	
	@GetMapping("/users")
	public String getAllUsers(Model m,@RequestParam Integer type) {
		List<UserDtls> users=null;
		if(type==1) {
			users=userService.getUsers("ROLE_USER");
		}else {
			users=userService.getUsers("ROLE_ADMIN");
		}
		m.addAttribute("userType", type);
		m.addAttribute("users",users);
		return "/admin/user";
	}
	
	@GetMapping("/updateStatus")
	public String userAccountStatus(@RequestParam Boolean status,@RequestParam Integer id,@RequestParam Integer type,HttpSession session) {
		Boolean f=userService.updateAccountStatus(id,status);
		if(f) {
			session.setAttribute("successMessage", "Account status updated!");
		}else {
			session.setAttribute("errorMessage", "Something went wrong on server!");
		}
		return "redirect:/admin/users?type="+type;
	}
	
	@GetMapping("/orders")
	public String getAllOrders(Model m,@RequestParam(name="pageNo",defaultValue="0")Integer pageNo,
			@RequestParam(name="pageSize",defaultValue="5")Integer pageSize) {
//		List<ProductOrder> allOrders = orderService.getAllOrders();
//		m.addAttribute("orders",allOrders);
//		m.addAttribute("search", false);
		
		Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo,pageSize);
		m.addAttribute("orders",page.getContent());
		m.addAttribute("search", false);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "/admin/orders";
	}
	
	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam(required = false) String id, @RequestParam(required = false) String st, HttpSession session, Model m) {
	    // Check if the order ID and status are valid
	    if (id == null || st == null || st.equals("--Select Status--")) {
	        session.setAttribute("errorMessage", "Please select a valid order and status.");
	        return "redirect:/admin/orders"; // Redirect to the orders page with error message
	    }
	    
	    Integer orderId;
	    Integer statusId;
	    
	    try {
	        orderId = Integer.valueOf(id);
	        statusId = Integer.valueOf(st);  // Try converting status to Integer
	    } catch (NumberFormatException e) {
	        session.setAttribute("errorMessage", "Invalid order ID or status.");
	        return "redirect:/admin/orders"; // Redirect if parsing fails
	    }

	    // Check if a valid status is selected
	    String status = null;
	    OrderStatus[] values = OrderStatus.values();
	    for (OrderStatus orderSt : values) {
	        if (orderSt.getId().equals(statusId)) {
	            status = orderSt.getName();
	            break;
	        }
	    }

	    // Check if the status is still invalid
	    if (status == null || status.equals("--Select Status--")) {
	        session.setAttribute("errorMessage", "Please select a valid status.");
	        return "redirect:/admin/orders"; // Redirect to the orders page
	    }

	    // Update order status
	    ProductOrder updateOrder = orderService.updateOrderStatus(orderId, status);

	    try {
	        // Send confirmation email
	        commonUtil.sendMailForProductOrder(updateOrder, status);
	    } catch (UnsupportedEncodingException | MessagingException e) {
	        e.printStackTrace();
	    }

	    // Set a success message after the update
	    session.setAttribute("successMessage", "Order status updated successfully!");
	    return "redirect:/admin/orders"; // Redirect after successful update
	}

	
	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId,Model m,HttpSession session,@RequestParam(name="pageNo",defaultValue="0")Integer pageNo,
			@RequestParam(name="pageSize",defaultValue="5")Integer pageSize) {
		if(orderId!=null && orderId.length()>0) {
		ProductOrder order=orderService.getOrderByOrderId(orderId.trim());
		if(ObjectUtils.isEmpty(order)) {
			session.setAttribute("errorMessage", "Incorrect Order Id!");
			m.addAttribute("orderDtls", null);
		}else {
			m.addAttribute("orderDtls",order);
		}
		m.addAttribute("search", true);
		}else {
//			List<ProductOrder> allOrders = orderService.getAllOrders();
//			m.addAttribute("orders",allOrders);
//			m.addAttribute("search", false);
			
			Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo,pageSize);
			m.addAttribute("orders",page.getContent());
			m.addAttribute("search", false);
			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());
			
			return "/admin/orders";
		}
		return "/admin/orders";
	}

	@GetMapping("/add-admin")
	public String addAdmin() {
		return "/admin/add_admin";
	}
	
	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user,@RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
		
		String imageName=file.isEmpty()? "default.jpg":file.getOriginalFilename() ;
		user.setProfileImage(imageName);
		UserDtls saveUser=userService.saveAdmin(user);
		
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
		return "redirect:/admin/add-admin";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return "/admin/profile";
	}
	
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user,@RequestParam MultipartFile img,HttpSession session) {
		UserDtls updateUser=userService.updateUserProfile(user, img);
		if(ObjectUtils.isEmpty(updateUser)) {
			session.setAttribute("errorMessage","Profile not updated!");
		}else {
			session.setAttribute("successMessage", "Profile Updated!");
		}
		return "redirect:/admin/profile";
	}
	
	private UserDtls getLoggedInUserDetails(Principal p) {
		String email=p.getName();
		UserDtls userDtls=userService.getUserByEmail(email);
		return userDtls;
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
		return "redirect:/admin/profile";
	}
	
}
