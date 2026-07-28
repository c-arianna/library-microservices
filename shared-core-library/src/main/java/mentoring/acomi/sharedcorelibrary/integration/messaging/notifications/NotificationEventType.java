package mentoring.acomi.sharedcorelibrary.integration.messaging.notifications;

public enum NotificationEventType {

    BOOK_UPDATED("notifications.book.updated"), LOAN_UPDATED("notifications.loan.updated"), USER_UPDATED("notifications.user.updated"),
    BOOK_SUBSCRIPTION_REQUESTED("notifications.book.subscription.requested"), 
    BOOK_SUBSCRIPTION_NOTIFIED("notifications.book.subscription.notified");

    private final String routingKey;

    NotificationEventType(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getRoutingKey() {
        return routingKey;
    }

}