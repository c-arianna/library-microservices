package mentoring.acomi.userservice.infrastructure.persistence.repositories;

import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CardNumberSequenceRepository {

    private final JdbcTemplate jdbcTemplate;

    public CardNumberSequenceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long nextValue() {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> connection.prepareStatement(
                            """
                            INSERT INTO card_number_sequence VALUES ()
                            """,
                            Statement.RETURN_GENERATED_KEYS), keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("Unable to generate card number");
        }

        return key.longValue();
    }
}