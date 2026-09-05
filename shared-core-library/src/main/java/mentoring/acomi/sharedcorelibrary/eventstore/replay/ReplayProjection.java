package mentoring.acomi.sharedcorelibrary.eventstore.replay;

public interface ReplayProjection {
    void createTempTable();
    void swapTables();
    void dropTempTable();
}