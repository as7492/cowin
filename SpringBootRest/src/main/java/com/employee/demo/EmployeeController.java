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
	public static int count = 0;

	@GetMapping("/cowin/bangalore")
	public String getAllEmp() throws InterruptedException {
		Executors.newCachedThreadPool().submit(() -> {
			while (true) {
				checkVaccination("483501");
				checkVaccination("560001");
				checkVaccination("560017");
				checkVaccination("560020");
				checkVaccination("560076");
				checkVaccination("560078");
			}
		});
		return "Proceess Triggered Successfully";
	}

	public void checkVaccination(String pin) throws InterruptedException {
		try {
			Map centers = (Map<String, Object>) given().baseUri("https://cdn-api.co-vin.in/api/v2/appointment/sessions")
					.when()
					.get("/public/calendarByPin?pincode=" + pin + "&date="
							+ DateTimeFormatter.ofPattern("dd-MM-yyyy").format(LocalDate.now()))
					.then().log().all().extract().response().body().jsonPath().getJsonObject("$");
			count = 0;
			List<Map<String, Object>> arr = (List<Map<String, Object>>) centers.get("centers");
			for (Object ele : arr) {
				List<Map<String, Object>> sessions = (List<Map<String, Object>>) ((Map<String, Object>) ele)
						.get("sessions");
				String pincode = ((Map<?, ?>) ele).get("pincode").toString();
				for (Object session : sessions) {
					String cap = ((Map<String, Object>) session).get("available_capacity").toString();
					String age = ((Map<String, Object>) session).get("min_age_limit").toString();
					if (Integer.parseInt(cap) > 0 && Integer.parseInt(age) == 18) {
						sendMail("capacity-" + ((Map<String, Object>) session).get("available_capacity").toString()
								+ "\ndate-" + ((Map<String, Object>) session).get("date").toString() + "\npin-"
								+ pincode);
					}
				}
			}
		} catch (Exception E) {
			Thread.sleep(30000);
			getAllEmp();
			count++;
			if (count > 10) {
				sendMail("something wrong");
			}
		}
	}

	public static void send(String from, String password, String to, String sub, String msg) {
		// Get properties object
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.fallback", "false");
		// get Session
		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, password);
			}
		});
		// compose message
		try {
			Message message = new MimeMessage(session);
			// message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
			message.setRecipients(Message.RecipientType.TO, parse(to));
			message.setSubject(sub);
			message.setText(msg);
			// send message
			Transport.send(message);
			System.out.println("message sent successfully");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}

	}

	public static void sendMail(String content) {
		// from,password,to,subject,message
		send("lalitamor1994@gmail.com", "A@d!ty@m0r",
				"morankita21@gmail.com,lalitamor01@gmail.com,singhanurag66@gmail.com", "Vaccine", content);
		// change from, password and to
	}
}
