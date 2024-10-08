package com.ecom.utils;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.shopping_cart.model.ProductOrder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;
	
	public Boolean sendMail(String url,String reciepentEmail) throws UnsupportedEncodingException, MessagingException {
		MimeMessage mimeMessage=mailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage);
		helper.setFrom("simmi@gmail.com", "Shopping World");
		helper.setTo(reciepentEmail);
		String content="<p>Hello,</p>"+"<p>You are requested to reset your password</p>"+
						"<p>Click on the link below to change your password:</p>"+"<p><a href=\""+url+
						"\">Change my password</a></p>";
		helper.setSubject("Reset Password");
		helper.setText(content,true);
		mailSender.send(mimeMessage);
		return true;
	}

	public String generateUrl(HttpServletRequest request) {
		//http:localhost:8080/forgot-password
		// Extract the base URL (excluding the servlet path) and construct the reset password URL
		String siteUrl = request.getRequestURL().toString();
	    siteUrl = siteUrl.replace(request.getServletPath(), "");  
	    return siteUrl;
	}

	String msg=null;
	public Boolean sendMailForProductOrder(ProductOrder productOrder,String status) throws MessagingException, UnsupportedEncodingException {
		MimeMessage mimeMessage=mailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage);
		helper.setFrom("simmi@gmail.com", "Shopping World");
		helper.setTo(productOrder.getOrderAddress().getEmail());
		msg="<p>Hello [[name]],</p>"
				+ "<p>Thank you order <b>[[orderStatus]]</b>.</p>"
				+ "<p><b>Product Details:</b></p>"
				+ "<p>Name : [[productName]]</p>"
				+ "<p>Category : [[category]]</p>"
				+ "<p>Quantity : [[quantity]]</p>"
				+ "<p>Price : [[price]]</p>"
				+ "<p>Payment Type : [[paymentType]]</p>";
		
		msg=msg.replace("[[orderStatus]]", status);
		msg=msg.replace("[[name]]",productOrder.getOrderAddress().getFirstName());
		msg=msg.replace("[[productName]]", productOrder.getProduct().getTitle());
		msg=msg.replace("[[category]]", productOrder.getProduct().getCategory());
		msg=msg.replace("[[quantity]]", productOrder.getQuantity().toString());
		msg=msg.replace("[[price]]", productOrder.getPrice().toString());
		msg=msg.replace("[[paymentType]]", productOrder.getPaymentType());
		
		helper.setSubject("Product Order Status");
		helper.setText(msg, true);
		mailSender.send(mimeMessage);
		return true;
	}
	
}
