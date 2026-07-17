package mentoring.acomi.bookservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.application.projection.BookProjectionOperations;
import mentoring.acomi.bookservice.application.repositories.BookViewRepository;

@Configuration
public class BookProjectionConfiguration {

    @Bean("liveBookProjection")
    BookProjectionOperations liveBookProjection(BookViewRepository repository) {
        return new BookProjection(repository);
    }

    @Bean("replayBookProjection")
    BookProjectionOperations replayBookProjection(@Qualifier("replayRepo") BookViewRepository repository) {
        return new BookProjection(repository);
    }

}
