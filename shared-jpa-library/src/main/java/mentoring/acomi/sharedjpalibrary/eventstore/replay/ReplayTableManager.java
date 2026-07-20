package mentoring.acomi.sharedjpalibrary.eventstore.replay;

import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.transaction.Transactional;

public class ReplayTableManager {

    private final JdbcTemplate jdbcTemplate;

    public ReplayTableManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void swapTables(String mainTable, String tempTable) {

        String oldTable = "%s_old".formatted(mainTable);

        jdbcTemplate.execute("ALTER TABLE %s RENAME TO %s".formatted(mainTable, oldTable));

        jdbcTemplate.execute("ALTER TABLE %s RENAME TO %s".formatted(tempTable, mainTable));

        jdbcTemplate.execute("DROP TABLE %s".formatted(oldTable));
    }

    public void dropTempTable(String tempTable) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS %s".formatted(tempTable));
    }
    
    @Transactional
    public void createTempTable(String tableMain, String tableTmp) {
    	jdbcTemplate.execute("DROP TABLE IF EXISTS %s".formatted(tableTmp));
        jdbcTemplate.execute("CREATE TABLE %s LIKE %s".formatted(tableTmp, tableMain));

    }
}