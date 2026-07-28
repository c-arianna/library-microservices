package mentoring.acomi.notificationservice.application.sender;

public interface SmsSender {
	 void send(String phoneNumber, String message);
}
