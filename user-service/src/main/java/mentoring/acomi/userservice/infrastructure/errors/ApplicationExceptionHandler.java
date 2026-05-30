package mentoring.acomi.userservice.infrastructure.errors;

import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import mentoring.acomi.userservice.application.errors.InvalidLogin;
import mentoring.acomi.userservice.application.errors.InvalidRefreshToken;
import mentoring.acomi.userservice.application.errors.InvalidUserStatus;
import mentoring.acomi.userservice.domain.errors.ApplicationConflict;
import mentoring.acomi.userservice.domain.errors.UserNotExist;
import mentoring.acomi.userservice.domain.errors.ValidationDomain;
import mentoring.acomi.userservice.infrastructure.errors.dto.ErrorResponse;

@RestControllerAdvice
public class ApplicationExceptionHandler {

	private final Logger logger = LogManager.getLogger(ApplicationExceptionHandler.class);;

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidationErorr(MethodArgumentNotValidException e) throws Exception {

		FieldError fieldError = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
		String message = fieldError != null ? fieldError.getDefaultMessage() : "Invalid data input";

		return handleException(e, "VALIDATION_ERROR", message, "VALIDATION_ERROR");

	}

	@ExceptionHandler(ValidationDomain.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidationDomainError(ValidationDomain e) throws Exception {

		return handleException(e, e.getCode(), e.getMessage(), "VALIDATION_ERROR");
	}

	@ExceptionHandler(ApplicationConflict.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleConflictError(ApplicationConflict e) throws Exception {
		return handleException(e, e.getCode(), e.getMessage(), "CONFLICT");
	}

	@ExceptionHandler(UserNotExist.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
	public ErrorResponse handleUserNotCreatedError(UserNotExist e) throws Exception {
		return handleException(e, e.getCode(), e.getMessage(), "AGGREGATE_INVARIANT_FAILED");
	}
		
	@ExceptionHandler(InvalidLogin.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleInvalidLoginError(InvalidLogin e) throws Exception {
		return handleException(e, e.getCode(), e.getMessage(), "LOGIN");
	}
	
	@ExceptionHandler(InvalidRefreshToken.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleInvalidRefreshTokenError(InvalidRefreshToken e) throws Exception {
		return handleException(e, e.getCode(), e.getMessage(), "LOGIN");
	}
	
	@ExceptionHandler(InvalidUserStatus.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleInvalidUserStatusError(InvalidUserStatus e) throws Exception {
		return handleException(e, e.getCode(), e.getMessage(), "LOGIN");
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public ErrorResponse handleGenericException(Exception e) throws Exception {

		logger.error("Unexpected exception, {}", e.getMessage(), e);

		String code = "INTERNAL_SERVER_ERROR";
		String message = "Generic error";
		String type = "INTERNAL_ERROR";

		return handleException(e, code, message, type);

	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Throwable.class)
	public ErrorResponse handleThrowable(Throwable e) throws Exception {

		logger.error("Fatal error, {}", e.getMessage(), e);

		String code = "INTERNAL_SERVER_ERROR";
		String message = "Fatal Error";
		String type = "INTERNAL_ERROR";

		return new ErrorResponse(code, message, type, "book-service", Instant.now());

	}

	private ErrorResponse handleException(Exception e, String code, String message, String type) throws Exception {

		if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
			throw e;
		}

		return new ErrorResponse(code, message, type, "user-service", Instant.now());

	}

}
