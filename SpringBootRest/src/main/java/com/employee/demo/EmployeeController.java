package com.employee.demo;

import static io.restassured.RestAssured.given;
import static javax.mail.internet.InternetAddress.parse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

	@GetMapping("/cowin/bangalore")
	public String getAllEmp() throws InterruptedException {
		Executors.newCachedThreadPool().submit(() -> {
			while (true) {
				checkVaccination("294");
				checkVaccination("353");
				checkVaccination("116");
				checkVaccination("118");
				checkVaccination("664");
				System.out.println("Running");
				Thread.sleep(10000);
			}
		});
		return "Proceess Triggered Successfully";
	}

	@SuppressWarnings("unchecked")
	public void checkVaccination(String pin) {
		try {
			
			Random random = new Random();
			
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
			headers.put("Connection", "keep-alive");
			
			headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0."+random.nextInt(999)+".93 Safari/537.36");
			
			Map centers = (Map<String, Object>) given().baseUri("https://cdn-api.co-vin.in/api/v2/appointment/sessions").headers(headers)
					.when()
					.get("/public/calendarByDistrict?district_id=" + pin + "&date="
							+ DateTimeFormatter.ofPattern("dd-MM-yyyy").format(LocalDate.now()))
					.then().extract().response().body().jsonPath().getJsonObject("$");
			
			List<Map<String, Object>> arr = (List<Map<String, Object>>) centers.get("centers");
			for (Object ele : arr) {
				List<Map<String, Object>> sessions = (List<Map<String, Object>>) ((Map<String, Object>) ele)
						.get("sessions");
				String pincode = ((Map<?, ?>) ele).get("pincode").toString();
				for (Object session : sessions) {
					String cap = ((Map<String, Object>) session).get("available_capacity").toString();
					String age = ((Map<String, Object>) session).get("min_age_limit").toString();
					if (Float.parseFloat(cap) > 1 && Integer.parseInt(age) == 18) {
						sendMail("Available Capacity - " + ((Map<String, Object>) session).get("available_capacity").toString()
								+ ", Date - " + ((Map<String, Object>) session).get("date").toString() + ", Pincode -"
								+ pincode);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(900000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.out.println("Wait over after error");
		}
	}

	public static void send(String from, String password, String to, String sub) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.fallback", "false");

		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, password);
			}
		});
		try {
			Message message = new MimeMessage(session);
			message.setRecipients(Message.RecipientType.TO, parse(to));
			message.setSubject(sub);
			message.setText(sub);
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Email Sent");
	}

	public static void sendMail(String content) {
		if(content.contains("560")) {
			send("anurag.singh741992@gmail.com", "sadhvi29712", "lalitamor01@gmail.com,singhanurag66@gmail.com,ravi.daga3425@gmail.com", content);
		}else {
			send("anurag.singh741992@gmail.com", "sadhvi29712", "lalitamor01@gmail.com,singhanurag66@gmail.com", content);
		}
	}
}
