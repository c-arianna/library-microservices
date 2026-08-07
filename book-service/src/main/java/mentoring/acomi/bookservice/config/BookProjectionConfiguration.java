package mentoring.acomi.bookservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mentoring.acomi.bookservice.application.projection.*;
import mentoring.acomi.bookservice.application.repositories.BookRequestViewRepository;
import mentoring.acomi.bookservice.application.repositories.BookRequestVoteViewRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.repositories.UserViewRepository;

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
    
    @Bean("liveBookRequestProjection")
    BookRequestProjectionOperations liveBookRequestProjection(BookRequestViewRepository repository) {
    	return new BookRequestProjection(repository);
    }
    
    @Bean("replayBookRequestProjection")
    BookRequestProjectionOperations replayBookRequestProjection(@Qualifier("replayBookRequestRepo") BookRequestViewRepository repository) {
    	return new BookRequestProjection(repository);
    }
    
    @Bean("liveBookRequestVoteProjection")
    BookRequestVoteProjectionOperations liveBookRequestVoteProjection(BookRequestVoteViewRepository repository) {
    	return new BookRequestVoteProjection(repository);
    }
    
    @Bean("replayBookRequestVoteProjection")
    BookRequestVoteProjectionOperations replayBookRequestVoteProjection(@Qualifier("replayBookRequestVoteRepo") BookRequestVoteViewRepository repository) {
    	return new BookRequestVoteProjection(repository);
    }
    
    @Bean("liveUserProjection")
    UserProjectionOperations liveUserProjection(UserViewRepository repository) {
        return new UserProjection(repository);
    }

    @Bean("replayUserProjection")
    UserProjectionOperations replayUserProjection(@Qualifier("userReplayRepo") UserViewRepository repository) {
        return new UserProjection(repository);
    }

}
