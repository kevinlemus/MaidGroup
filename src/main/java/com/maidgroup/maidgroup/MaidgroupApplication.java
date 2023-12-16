package com.maidgroup.maidgroup;


import com.maidgroup.maidgroup.util.square.SignatureGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MaidgroupApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaidgroupApplication.class, args);
		// Your Square signature key
		String key = "VJj7brPdM3ukD_Q6QyIYLQ";

		// Your webhook's URL
		String url = "https://plain-steaks-sip.loca.lt/maidgroup/invoices/webhook";

		// Your webhook's JSON payload
		String payload = "{\n" +
				"    \"street\": \"123 Main St\",\n" +
				"    \"city\": \"Anytown\",\n" +
				"    \"state\": \"Anystate\",\n" +
				"    \"zipcode\": 12345,\n" +
				"    \"date\": \"2023-11-03\",\n" +
				"    \"firstName\": \"Kevin\",\n" +
				"    \"lastName\": \"Doe\",\n" +
				"    \"clientEmail\": \"kevinlemus@gmail.com\",\n" +
				"    \"phoneNumber\": \"123-456-7890\",\n" +
				"    \"totalPrice\": 0.02,\n" +
				"    \"status\": \"UNPAID\",\n" +
				"    \"items\": [\n" +
				"        {\n" +
				"            \"name\": \"Item1\",\n" +
				"            \"price\": 0.01,\n" +
				"            \"type\": \"Product\"\n" +
				"        },\n" +
				"        {\n" +
				"            \"name\": \"Item2\",\n" +
				"            \"price\": 0.01,\n" +
				"            \"type\": \"Service\"\n" +
				"        }\n" +
				"    ]\n" +
				"}";

		// Generate the signature
		String signature = SignatureGenerator.generateSignature(url, payload, key);

		// Print the signature
		System.out.println(signature);
	}
}

