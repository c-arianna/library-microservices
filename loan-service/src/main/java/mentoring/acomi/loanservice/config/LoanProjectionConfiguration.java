package mentoring.acomi.loanservice.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mentoring.acomi.loanservice.application.projection.DefaultProjectionDispatcher;
import mentoring.acomi.loanservice.application.projection.EventProjector;
import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.repositories.PopularBookViewRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.infrastructure.projection.BookProjection;
import mentoring.acomi.loanservice.infrastructure.projection.DailyLoanStatisticProjection;
import mentoring.acomi.loanservice.infrastructure.projection.LoanProjection;
import mentoring.acomi.loanservice.infrastructure.projection.PopularBookProjection;
import mentoring.acomi.loanservice.infrastructure.projection.UserLoanStatisticProjection;
import mentoring.acomi.loanservice.infrastructure.projection.UserProjection;

@Configuration
public class LoanProjectionConfiguration {

    @Bean("liveLoanProjector")
    LoanProjection liveLoanProjector(LoanViewRepository repository) {
        return new LoanProjection(repository);
    }

    @Bean("liveUserProjector")
    UserProjection liveUserProjector(UserViewRepository repository) {
        return new UserProjection(repository);
    }

    @Bean("liveUserLoanStatisticProjector")
    UserLoanStatisticProjection liveUserLoanStatisticProjector(UserLoanStatisticRepository statisticRepository, 
    		LoanViewQueryRepository loanQueryRepository) {
        return new UserLoanStatisticProjection(statisticRepository, loanQueryRepository);
    }

    @Bean("liveDailyLoanStatisticProjector")
    DailyLoanStatisticProjection liveDailyLoanStatisticProjector(DailyLoanStatisticRepository statisticRepository) {
        return new DailyLoanStatisticProjection(statisticRepository);
    }
    
    @Bean("livePopularBookProjector")
    PopularBookProjection livePopularBookProjector(BookViewRepository bookRepository, PopularBookViewRepository repository) {
    	return new PopularBookProjection(bookRepository, repository);
    }
    
    @Bean("liveBookProjector")
    BookProjection liveBookProjector(BookViewRepository repository) {
    	return new BookProjection(repository);
    }
    
    @Bean("replayLoanProjector")
    LoanProjection replayLoanProjector(@Qualifier("replayRepo") LoanViewRepository repository) {
        return new LoanProjection(repository);
    }

    @Bean("replayUserProjector")
    UserProjection replayUserProjector(@Qualifier("userReplayRepo") UserViewRepository repository) {
        return new UserProjection(repository);
    }

    @Bean("replayUserLoanStatisticProjector")
    UserLoanStatisticProjection replayUserLoanStatisticProjector(@Qualifier("replayStatisticRepo") UserLoanStatisticRepository 
    		statisticRepository, LoanViewQueryRepository loanQueryRepository) {
        return new UserLoanStatisticProjection(statisticRepository, loanQueryRepository);
    }

    @Bean("replayDailyLoanStatisticProjector")
    DailyLoanStatisticProjection replayDailyLoanStatisticProjector(@Qualifier("replayDailyLoanStatisticRepo")
            DailyLoanStatisticRepository statisticRepository) {
        return new DailyLoanStatisticProjection(statisticRepository);
    }

    @Bean("replayPopularBookProjector")
    PopularBookProjection replayPopularBookProjector(@Qualifier("replayBookViewRepo") BookViewRepository bookRepository, 
    		@Qualifier("replayPopularBookViewRepo") PopularBookViewRepository repository) {
    	return new PopularBookProjection(bookRepository, repository);
    }
    
    @Bean("replayBookProjector")
    BookProjection replayBookProjector(@Qualifier("replayBookViewRepo") BookViewRepository repository) {
    	return new BookProjection(repository);
    }
    
    @Bean("liveProjectors")
    List<EventProjector> liveProjectors(@Qualifier("liveLoanProjector") LoanProjection loan, 
    		@Qualifier("liveUserProjector") UserProjection user, 
    		@Qualifier("liveUserLoanStatisticProjector") UserLoanStatisticProjection userStatistics,
            @Qualifier("liveDailyLoanStatisticProjector") DailyLoanStatisticProjection dailyStatistics,
            @Qualifier("livePopularBookProjector") PopularBookProjection popularBookProjection,
            @Qualifier("liveBookProjector") BookProjection bookProjection) {
        return List.of(loan, user, userStatistics, dailyStatistics, popularBookProjection, bookProjection);
    }

    @Bean("replayProjectors")
    List<EventProjector> replayProjectors(@Qualifier("replayLoanProjector") LoanProjection loan,
            @Qualifier("replayUserProjector") UserProjection user, 
            @Qualifier("replayUserLoanStatisticProjector") UserLoanStatisticProjection userStatistics,
            @Qualifier("replayDailyLoanStatisticProjector") DailyLoanStatisticProjection dailyStatistics,
            @Qualifier("replayPopularBookProjector") PopularBookProjection popularBookProjection,
            @Qualifier("replayBookProjector") BookProjection bookProjection) {
        return List.of(loan, user, userStatistics, dailyStatistics, popularBookProjection, bookProjection);
    }

    @Bean("liveDispatcher")
    ProjectionDispatcher liveDispatcher(@Qualifier("liveProjectors") List<EventProjector> projectors) {
        return new DefaultProjectionDispatcher(projectors);
    }

    @Bean("replayDispatcher")
    ProjectionDispatcher replayDispatcher(@Qualifier("replayProjectors") List<EventProjector> projectors) {
        return new DefaultProjectionDispatcher(projectors);
    }
}