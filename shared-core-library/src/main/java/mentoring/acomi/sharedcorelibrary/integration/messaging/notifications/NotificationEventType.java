package mentoring.acomi.sharedcorelibrary.integration.messaging.notifications;

public enum NotificationEventType {

    BOOK_UPDATED("notifications.book.updated"), LOAN_UPDATED("notifications.loan.updated"), USER_UPDATED("notifications.user.updated");

    private final String routingKey;

    NotificationEventType(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getRoutingKey() {
        return routingKey;
    }

}