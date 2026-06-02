package mentoring.acomi.loanservice.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.infrastructure.dto.AddLoanRequest;
import mentoring.acomi.loanservice.infrastructure.dto.LoanDto;
import mentoring.acomi.loanservice.infrastructure.dto.LoanResponse;
import mentoring.acomi.loanservice.infrastructure.dto.LoansResponse;
import mentoring.acomi.sharedlibrary.model.UserStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LoanApiIntegrationTest {

	private static final String USER_1 = "user-1";
	private static final String USER_2 = "user-2";
	private static final String ADMIN_ROLE = "ADMIN";
	private static final String LIBRARIAN_ROLE = "LIBRARIAN";
	private static final String READER_ROLE = "READER";
	private static final String CONFIRM_LOAN_ENDPOINT = "/%s/confirm";
	private static final String CANCEL_LOAN_ENDPOINT = "/%s/reject";
	private static final String RETURN_LOAN_ENDPOINT = "/%s/return";

	@LocalServerPort
	int port;

	private RestClient client;
	
	@Autowired
	private UserViewRepository userViewRepository;

	@BeforeEach
	void setup() {
		this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d", port)).build();
		createUser(USER_1);
		createUser(USER_2);
	}

	@Test
	public void readerCanCreateLoan() {
		ResponseEntity<String> response = addLoan(USER_2, READER_ROLE);
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	public void librarianCanCreateLoan() {
		ResponseEntity<String> response = addLoan(USER_1, LIBRARIAN_ROLE);
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	public void adminCanCreateLoan() {
		ResponseEntity<String> response = addLoan(USER_1, ADMIN_ROLE);
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	public void readerCannotConfirmLoan() {
		String loanId = setupLoan();
		ResponseEntity<String> response = confirmLoan(loanId, READER_ROLE);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Disabled("Move to integration-test module")
	@Test
	public void librarianCanConfirmLoan() {
		String loanId = setupLoan();
		ResponseEntity<String> response = confirmLoan(loanId, LIBRARIAN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Disabled("Move to integration-test module")
	@Test
	public void adminCanConfirmLoan() {
		String loanId = setupLoan();
		ResponseEntity<String> response = confirmLoan(loanId, ADMIN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	public void readerCannotCancelLoan() {
		String loanId = setupLoan();
		ResponseEntity<String> response = cancelLoan(loanId, READER_ROLE);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Disabled("Move to integration-test module")
	@Test
	public void librarianCanCancelLoan() {
		String loanId = setupLoan();
		ResponseEntity<String> response = cancelLoan(loanId, LIBRARIAN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Disabled("Move to integration-test module")
	@Test
	public void adminCanCancelLoan() {
		String loanId = setupLoan();
		ResponseEntity<String> response = cancelLoan(loanId, ADMIN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	public void readerCannotReturnLoan() {
		String loanId = setupLoan();
		ResponseEntity<String> response = returnLoan(loanId, READER_ROLE);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Disabled("Move to integration-test module")
	@Test
	public void librarianCanReturnLoan() {
		String loanId = setupLoan();
		confirmLoan(loanId);
		ResponseEntity<String> response = returnLoan(loanId, LIBRARIAN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Disabled("Move to integration-test module")
	@Test
	public void adminCanReturnLoan() {
		String loanId = setupLoan();
		confirmLoan(loanId);
		ResponseEntity<String> response = returnLoan(loanId, ADMIN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	public void readerSeesOnlyOwnLoans() throws Exception {

		setupLoan(USER_1);
		setupLoan(USER_2);

		ResponseEntity<LoansResponse> response = client.get()
				.uri(uriBuilder -> uriBuilder.path("").queryParam("userId", "user-2").build())
				.headers(h -> addHeaders(h, "READER", "user-1")).retrieve().toEntity(LoansResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		List<LoanDto> loans = response.getBody().loans();
		Assertions.assertEquals(1, loans.size());

		String returnedUserId = loans.get(0).userId();

		Assertions.assertEquals(USER_1, returnedUserId);
	}

	@Test
	public void librarianCanFilterAnyUser() throws Exception {

		setupLoan(USER_1);
		setupLoan(USER_2);

		ResponseEntity<LoansResponse> response = client.get()
				.uri(uriBuilder -> uriBuilder.path("").queryParam("userId", USER_2).build())
				.headers(h -> addHeaders(h, LIBRARIAN_ROLE, "lib-1")).retrieve().toEntity(LoansResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		List<LoanDto> loans = response.getBody().loans();

		Assertions.assertEquals(1, loans.size());
		Assertions.assertEquals(USER_2, loans.get(0).userId());
	}

	@Test
	public void adminCanFilterAnyUser() throws Exception {

		setupLoan(USER_1);
		setupLoan(USER_2);

		ResponseEntity<LoansResponse> response = client.get()
				.uri(uriBuilder -> uriBuilder.path("").queryParam("userId", USER_2).build())
				.headers(h -> addHeaders(h, ADMIN_ROLE, "admin-1")).retrieve().toEntity(LoansResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		List<LoanDto> loans = response.getBody().loans();

		Assertions.assertEquals(1, loans.size());
		Assertions.assertEquals(USER_2, loans.get(0).userId());
	}

	private String setupLoan() {
		return setupLoan(USER_1);
	}

	private String setupLoan(String userId) {

		AddLoanRequest request = new AddLoanRequest("9788804336327", userId, LocalDate.now(), null);
		ResponseEntity<LoanResponse> response = client.post().uri("/").contentType(MediaType.APPLICATION_JSON)
				.headers(h -> addHeaders(h, ADMIN_ROLE, USER_1)).body(request).retrieve().toEntity(LoanResponse.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());

		return response.getBody().loanId();

	}

	private ResponseEntity<String> addLoan(String userId, String role) {
		AddLoanRequest request = new AddLoanRequest("9788804336327", userId, LocalDate.now(), null);
		return client.post().uri("/").contentType(MediaType.APPLICATION_JSON).headers(h -> addHeaders(h, role, userId))
				.body(request).exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> confirmLoan(String loanId, String role) {
		return client.post().uri(String.format(CONFIRM_LOAN_ENDPOINT, loanId)).headers(h -> addHeaders(h, role, loanId))
				.exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> cancelLoan(String loanId, String role) {
		return client.post().uri(String.format(CANCEL_LOAN_ENDPOINT, loanId)).headers(h -> addHeaders(h, role, loanId))
				.exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> returnLoan(String loanId, String role) {
		return client.post().uri(String.format(RETURN_LOAN_ENDPOINT, loanId)).headers(h -> addHeaders(h, role, USER_1))
				.exchange((req, res) -> toEntity(res));
	}

	private void addHeaders(HttpHeaders headers, String role, String userId) {
		headers.set("X-User-Id", userId);
		headers.set("X-User-Email", "test@mail.com");
		headers.set("X-User-Role", role);
	}

	private ResponseEntity<String> toEntity(ClientHttpResponse response) throws IOException {
		String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

		return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(body);
	}

	private void confirmLoan(String loanId) {
		ResponseEntity<String> response = confirmLoan(loanId, LIBRARIAN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

	}

	private void createUser(String userId) {
		userViewRepository.add(new UserView(userId, String.format("test%s@gmail.com", userId), UserStatus.ACTIVE));
	}

}