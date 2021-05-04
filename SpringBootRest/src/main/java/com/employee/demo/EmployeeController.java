package com.employee.demo;

import static io.restassured.RestAssured.given;
import static javax.mail.internet.InternetAddress.parse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
				checkVaccination("560026");
//			while (true) {
//				checkVaccination("560001");
//				checkVaccination("560017");
//				checkVaccination("560020");
//				checkVaccination("560076");
//				checkVaccination("560078");
//				
//				Thread.sleep(10000);
//			}
		});
		return "Proceess Triggered Successfully";
	}

	@SuppressWarnings("unchecked")
	public void checkVaccination(String pin) {
		try {
			Map centers = (Map<String, Object>) given().baseUri("https://cdn-api.co-vin.in/api/v2/appointment/sessions")
					.when()
					.get("/public/calendarByPin?pincode=" + pin + "&date="
							+ DateTimeFormatter.ofPattern("dd-MM-yyyy").format(LocalDate.now().plusDays(1)))
					.then().extract().response().body().jsonPath().getJsonObject("$");
			
			System.out.println(centers);
			List<Map<String, Object>> arr = (List<Map<String, Object>>) centers.get("centers");
			for (Object ele : arr) {
				List<Map<String, Object>> sessions = (List<Map<String, Object>>) ((Map<String, Object>) ele)
						.get("sessions");
				String pincode = ((Map<?, ?>) ele).get("pincode").toString();
				for (Object session : sessions) {
					String cap = ((Map<String, Object>) session).get("available_capacity").toString();
					String age = ((Map<String, Object>) session).get("min_age_limit").toString();
					if (Integer.parseInt(cap) > 0 && Integer.parseInt(age) == 45) {
						sendMail("Available Capacity - " + ((Map<String, Object>) session).get("available_capacity").toString()
								+ ", Date - " + ((Map<String, Object>) session).get("date").toString() + ", Pincode -"
								+ pincode);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
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
		send("lalitamor1994@gmail.com", "A@d!ty@m0r", "lalitamor01@gmail.com,singhanurag66@gmail.com", content);
	}
}
