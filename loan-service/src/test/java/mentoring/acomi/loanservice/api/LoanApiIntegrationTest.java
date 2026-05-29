package mentoring.acomi.loanservice.api;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import mentoring.acomi.loanservice.infrastructure.dto.AddLoanRequest;
import mentoring.acomi.loanservice.infrastructure.dto.LoansResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoanApiIntegrationTest {

	@LocalServerPort
    int port;
	
	private RestClient client;

	@BeforeEach
	public void setup() {
		 this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d",port)).build();
	}
	
	@Test
	void shouldReturnOkWhenCallingGetLoans() {
		ResponseEntity<LoansResponse> response =  client.get().uri("/loans").retrieve().toEntity(LoansResponse.class);
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
		Assertions.assertNotNull(response.getBody());
	}

	@Test
	void shouldCreateLoan() {
		AddLoanRequest request = new AddLoanRequest("9788804336327", "user01", LocalDate.now(), null);
		ResponseEntity<String> response = client.post().uri("/loans").contentType(MediaType.APPLICATION_JSON)
	            .body(request).retrieve().toEntity(String.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

}