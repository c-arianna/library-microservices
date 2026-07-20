package mentoring.acomi.sharedcodelibrary.eventstore.replay;

public interface ReplayProjection {
    void createTempTable();
    void swapTables();
    void dropTempTable();
}