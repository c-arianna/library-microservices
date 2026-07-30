package mentoring.acomi.loanservice.api;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import mentoring.acomi.loanservice.application.dto.LoanOverdueDto;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DashboardApiIntegrationTest {
	
	private static final String ADMIN_1 = "admin-1";
	private static final String USER_1 = "user-1";
	private static final String ADMIN_ROLE = "ADMIN";
	private static final String READER_ROLE = "READER";
	
	private static final String TOKEN_VALUE = "test-token";
	
	private static final String LOANS_OVERDUE_ENDPOINT = "/dashboard/overdue";
	
	@LocalServerPort
	int port;
	
	private RestClient client;
	
	@Autowired
	private UserViewRepository userViewRepository;
	
	@Autowired
	private LoanViewRepository loanViewRepository;
	
	@MockitoBean
	private JwtDecoder jwtDecoder;
	
	@BeforeEach
	void setup() {
		this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d", port)).build();
		createUser(USER_1);
		createUser(ADMIN_1);
	}
	
	@AfterEach
	void clearDb() {
		loanViewRepository.deleteAll();
		userViewRepository.deleteAll();
	}
	
	@Test
	public void readerCannotSeeLoansOverdue() {
		ResponseEntity<String> response = getLoansOverdue(USER_1, READER_ROLE);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	@Test
	public void adminCanSeeLoansOverdue() {
		
		String returnedLoanId = UUID.randomUUID().toString();
		LocalDate startDate = LocalDate.now();
		createLoan(returnedLoanId, startDate, startDate.plusDays(2));
		
		loanViewRepository.returnLoan(returnedLoanId, Instant.now(), LocalDate.now());
		
		String confirmedLoanId = UUID.randomUUID().toString();
		startDate = LocalDate.now();
		createLoan(confirmedLoanId, startDate, startDate.plusDays(2));
		
		loanViewRepository.updateStatus(confirmedLoanId, LoanStatus.CONFIRMED, Instant.now());
		
		String overdueLoanId = UUID.randomUUID().toString();
		startDate = LocalDate.now().minusDays(10);
		createLoan(overdueLoanId, startDate, startDate.plusDays(2));
		
		loanViewRepository.updateStatus(overdueLoanId, LoanStatus.CONFIRMED, Instant.now());
		
		generateToken(ADMIN_ROLE, ADMIN_1);
		
		ResponseEntity<List<LoanOverdueDto>> response = client.get().uri(LOANS_OVERDUE_ENDPOINT)
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE))
				.retrieve().toEntity(new ParameterizedTypeReference<List<LoanOverdueDto>>() {});
		
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		List<LoanOverdueDto> loans = response.getBody();

		Assertions.assertEquals(1, loans.size());
		
		LoanOverdueDto loanOverdue = loans.get(0);
		Assertions.assertEquals(overdueLoanId, loanOverdue.loanId());
		Assertions.assertEquals(8, loanOverdue.daysOverdue());
	}
		
	private void createUser(String userId) {
		userViewRepository.add(new UserView(userId, String.format("test%s@gmail.com", userId), "Harry", "Potter", "LIB-000001", 
				UUID.randomUUID().toString(), UserStatus.ACTIVE), Instant.now());
	}
	
	private void createLoan(String loanId, LocalDate startDate, LocalDate endDate) {
		LoanView loan = new LoanView(loanId, "9788804336327", UUID.randomUUID().toString(), startDate, endDate, LoanStatus.PENDING, null);
		loanViewRepository.insertRequest(loan, Instant.now());
	}
	
	private void generateToken(String role, String userId) {
		
		Jwt jwt = Jwt.withTokenValue(TOKEN_VALUE).header("alg", "none")
				.claim("sub", UUID.randomUUID().toString())
				.claim("email", String.format("test%s@gmail.com", userId))
				.claim("realm_access", Map.of("roles", List.of(role))).build();

		when(jwtDecoder.decode(TOKEN_VALUE)).thenReturn(jwt);
	}
	
	private ResponseEntity<String> getLoansOverdue(String userId, String role) {
		generateToken(role, userId);
				
		return client.get().uri(LOANS_OVERDUE_ENDPOINT).header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE))
				.exchange((req, res) -> toEntity(res));
	}
	
	private ResponseEntity<String> toEntity(ClientHttpResponse response) throws IOException {
		String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

		return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(body);
	}

}
